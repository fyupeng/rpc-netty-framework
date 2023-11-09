package cn.fyupeng.proxy;


import cn.fyupeng.annotation.Reference;
import cn.fyupeng.config.AbstractRedisConfiguration;
import cn.fyupeng.config.Configuration;
import cn.fyupeng.exception.AsyncTimeUnreasonableException;
import cn.fyupeng.exception.RetryTimeoutException;
import cn.fyupeng.factory.SingleFactory;
import cn.fyupeng.hook.ClientShutdownHook;
import cn.fyupeng.idworker.Sid;
import cn.fyupeng.idworker.WorkerIdServer;
import cn.fyupeng.net.RpcClient;
import cn.fyupeng.net.netty.client.NettyClient;
import cn.fyupeng.net.netty.client.UnprocessedResults;
import cn.fyupeng.net.socket.client.SocketClient;
import cn.fyupeng.protocol.RpcRequest;
import cn.fyupeng.protocol.RpcResponse;
import cn.fyupeng.proxy.factory.javassist.JavassistProxyFactory;
import cn.fyupeng.proxy.factory.jdk.JdkProxyFactory;
import cn.fyupeng.protocol.RpcMessageChecker;
import cn.fyupeng.serializer.HessianSerializer;
import lombok.extern.slf4j.Slf4j;

import javax.print.attribute.standard.DateTimeAtCompleted;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ServiceLoader;
import java.util.concurrent.*;

/**
 * @Auther: fyp
 * @Date: 2022/3/22
 * @Description:
 * @Package: cn.fyupeng.proxy
 * @Version: 1.0
 */
@Slf4j
public class RpcClientProxy {

   /**
    *  在调用 invoke 返回数据给 服务器时 连同 主机号和端口号 一起发送

   private String hostName;
   private int port;
   */
   private RpcClient rpcClient;

   /**
    * 需要获取的成员所在类
    */
   private Class<?> pareClazz = null;

   /**
    * 未处理请求，主要处理失败请求，多线程共享
    */
   private static UnprocessedResults unprocessedRequests = SingleFactory.getInstance(UnprocessedResults.class);

   private static JdkProxyFactory jdkProxyFactory = new JdkProxyFactory();
   private static JavassistProxyFactory javassistProxyFactory = new JavassistProxyFactory();
   static {
      /**
       * 配置 Redis 预加载
       */
      AbstractRedisConfiguration.getClientConfig();
      /**
       *  配置 WorkerId 预加载
       */
      configWorkerServer();
      /**
       * 使用 SPI 机制进行预加载
       * 解决 静态代码块 无法兼容多态扩展的方式
       */
      ServiceLoader.load(Configuration.class).iterator().next();
   }

   /**
    * @param rpcClient
    */
   public RpcClientProxy(RpcClient rpcClient) {
      this.rpcClient = rpcClient;
      ClientShutdownHook.getShutdownHook()
              .addClient(rpcClient)
              .addClearAllHook();
   }


   /**
    * 用于可 超时重试 的动态代理，需要配合 @Reference使用
    * 兼容 阻塞模式
    * asyncTime 字段 缺省 或者 <= 0 将启用 阻塞模式
    * 注意，此时 timeout 、 retries 字段将失效
    * @param clazz 获取的服务类
    * @param pareClazz 使用 @Reference 所在类
    * @param <T>
    * @return
    */
   public <T> T getProxy(Class<T> clazz, Class<?> pareClazz){
      this.pareClazz = pareClazz;
      return jdkProxyFactory.getProxy(clazz, (proxy, method, args) -> invoke0(proxy, method, args));
   }

   /**
    * 支持 Javassist 动态代理
    * 兼容 阻塞模式
    * asyncTime 字段 缺省 或者 <= 0 将启用 阻塞模式
    * 注意，此时 timeout 、 retries 字段将失效
    * @param clazz 获取的服务类
    * @param pareClazz 使用 @Reference 所在类
    * @param <T>
    * @return
    */
    public <T> T getJavassistProxy(Class<T> clazz, Class<?> pareClazz) {
       this.pareClazz = pareClazz;
       // 使用 Javassist 动态生成代理类
       T javassistProxy = null;
       try {
          javassistProxy = javassistProxyFactory.getProxy(clazz, (proxy, method, args) -> invoke0(proxy, method, args));
       } catch (Throwable e) {
          log.error("javassist dynamic proxy exception: ", e);
       }
       return javassistProxy;
    }

   /**
    * 用于普通动态代理，@Reference 将失效，已过时，不推荐使用
    * 原因：无法识别到 @Reference, 服务名 和 版本号 不可用
    * @param clazz 获取的服务类
    * @param <T>
    * @return
    */
   @Deprecated
   public <T> T getProxy(Class<T> clazz){
      return jdkProxyFactory.getProxy(clazz, (proxy, method, args) -> invoke0(proxy, method, args));
   }

    public Object invoke0(Object proxy, Method method, Object[] args) throws Throwable {
        return invoke(proxy, method, args);
    }


   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      RpcRequest rpcRequest = new RpcRequest.Builder()
              /**
               * 使用雪花算法 解决分布式 RPC 各节点生成请求号 id 一致性问题
               */
              .requestId(Sid.next())
              /**
               * 没有处理时间戳一致问题，可通过 synchronized 锁阻塞来获取
               */
              //.requestId(UUID.randomUUID().toString())
              .interfaceName(method.getDeclaringClass().getName())
              .methodName(method.getName())
              .parameters(args)
              //.paramTypes(method.getParameterTypes())
              .paramTypes(Arrays.stream(method.getParameterTypes()).map(Class::getName).toArray(String[]::new))
              .returnType(method.getReturnType().getCanonicalName())
              /**这里心跳指定为false，一般由另外其他专门的心跳 handler 来发送
               * 如果发送 并且 hearBeat 为 true，说明触发发送心跳包
               */
              .heartBeat(Boolean.FALSE)
              .reSend(Boolean.FALSE)
              .build();

      HessianSerializer hessianSerializer = new HessianSerializer();
      byte[] data = hessianSerializer.serialize(rpcRequest);
      Object obj = hessianSerializer.deserialize(data, RpcRequest.class);
      System.out.println(obj);

      System.out.println(rpcRequest);

      RpcResponse rpcResponse = null;

      if (rpcClient instanceof SocketClient) {
         rpcResponse = revokeSocketClient(rpcRequest, method);
      } else if (rpcClient instanceof NettyClient){
         rpcResponse = revokeNettyClient(rpcRequest, method);
      }

      return rpcResponse == null ? null : rpcResponse.getData();
   }

   private RpcResponse revokeNettyClient(RpcRequest rpcRequest, Method method)  throws Throwable {
      RpcResponse rpcResponse = null;
      if (pareClazz == null) {
         CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) rpcClient.sendRequest(rpcRequest);
         rpcResponse = completableFuture.get();

         RpcMessageChecker.checkAndThrow(rpcRequest, rpcResponse);
         return rpcResponse;
      }

      /**
       * 服务组名、重试机制实现
       */
      long timeout = 0L;
      long asyncTime = 0L;
      int retries = 0;
      int giveTime = 0;
      boolean useRetry = false;

      Field[] fields = pareClazz.getDeclaredFields();
      for (Field field : fields) {
         if (field.isAnnotationPresent(Reference.class) &&
                 method.getDeclaringClass().getName().equals(field.getType().getName())) {
            retries = field.getAnnotation(Reference.class).retries();
            giveTime =field.getAnnotation(Reference.class).giveTime();
            timeout = field.getAnnotation(Reference.class).timeout();
            asyncTime =field.getAnnotation(Reference.class).asyncTime();
            useRetry = true;
            String name = field.getAnnotation(Reference.class).name();
            String group = field.getAnnotation(Reference.class).group();
            if (!"".equals(name)) {
               rpcRequest.setInterfaceName(name);
            }
            if (!"".equals(group)) {
               rpcRequest.setGroup(group);
            }
            break;
         }
      }


      /**
       * 1、识别不到 @Reference 注解执行
       * 2、识别到 @Reference 且 asyncTime 缺省 或 asyncTime <= 0
       */
      if (!useRetry || asyncTime <= 0 || giveTime <= 0) {
         log.debug("discover @Reference or asyncTime <= 0, will use blocking mode");
         long startTime = System.currentTimeMillis();
         log.info("start calling remote service [requestId: {}, serviceMethod: {}]", rpcRequest.getRequestId(), rpcRequest.getMethodName());
         CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) rpcClient.sendRequest(rpcRequest);
         rpcResponse = completableFuture.get();
         long endTime = System.currentTimeMillis();

         log.info("handling the task takes time {} ms", endTime - startTime);

         RpcMessageChecker.checkAndThrow(rpcRequest, rpcResponse);

      } else {
         /**
          * 识别到 @Reference 注解 且 asyncTime > 0 执行
          */
         log.debug("discover @Reference and asyncTime > 0, will use blocking mode");
         if (timeout >= asyncTime) {
            log.error("asyncTime [ {} ] should be greater than timeout [ {} ]", asyncTime, timeout);
            throw new AsyncTimeUnreasonableException("Asynchronous time is unreasonable, it should greater than timeout");
         }
         long handleTime = 0;
         boolean checkPass = false;
         for (int i = 0; i < retries; i++) {
            if (handleTime >= timeout) {
               // 超时重试
               TimeUnit.SECONDS.sleep(giveTime);
               rpcRequest.setReSend(Boolean.TRUE);
               log.warn("call service timeout and retry to call [ rms: {}, tms: {} ]", handleTime, timeout);
            }
            long startTime = System.currentTimeMillis();
            log.info("start calling remote service [requestId: {}, serviceMethod: {}]", rpcRequest.getRequestId(), rpcRequest.getMethodName());
            CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) rpcClient.sendRequest(rpcRequest);
            try {
               rpcResponse = completableFuture.get(asyncTime, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
               // 忽视 超时引发的异常，自行处理，防止程序中断
               log.warn("recommend that asyncTime [ {} ] should be greater than current task runeTime [ {} ]", asyncTime, System.currentTimeMillis() - startTime);
               continue;
            }

            long endTime = System.currentTimeMillis();
            handleTime = endTime - startTime;

            if (handleTime < timeout) {
               // 没有超时不用再重试
               // 进一步校验包
               checkPass = RpcMessageChecker.check(rpcRequest, rpcResponse);
               if (checkPass) {
                  log.info("client call success [ rms: {}, tms: {} ]", handleTime, timeout);
                  return rpcResponse;
               }
               // 包被 劫持触发 超时重发机制 保护重发
            }
         }
         log.info("client call failed  [ rms: {}, tms: {} ]", handleTime, timeout);
         // 客户端在这里无法探知是否成功收到服务器响应，只能确定该请求包 客户端已经抛弃了
         unprocessedRequests.remove(rpcRequest.getRequestId());
         throw new RetryTimeoutException("The retry call timeout exceeds the threshold, the channel is closed, the thread is interrupted, and an exception is forced to be thrown!");
      }

      return rpcResponse;
   }

   private RpcResponse revokeSocketClient(RpcRequest rpcRequest, Method method) throws Throwable {
      RpcResponse rpcResponse = null;
      if (pareClazz != null) {
         Field[] fields = pareClazz.getDeclaredFields();
         for (Field field : fields) {
            if (field.isAnnotationPresent(Reference.class) &&
                    method.getDeclaringClass().getName().equals(field.getType().getName())) {
               String name = field.getAnnotation(Reference.class).name();
               String group = field.getAnnotation(Reference.class).group();
               if (!"".equals(name)) {
                  rpcRequest.setInterfaceName(name);
               }
               if (!"".equals(group)) {
                  rpcRequest.setGroup(group);
               }
               break;
            }
         }
      }
      rpcResponse = (RpcResponse) rpcClient.sendRequest(rpcRequest);
      RpcMessageChecker.checkAndThrow(rpcRequest, rpcResponse);

      return rpcResponse;
   }


   private static void configWorkerServer() {
      WorkerIdServer.preLoad();
   }

}
