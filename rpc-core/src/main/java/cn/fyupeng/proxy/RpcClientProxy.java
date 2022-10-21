package cn.fyupeng.proxy;


import cn.fyupeng.anotion.Reference;
import cn.fyupeng.anotion.Service;
import cn.fyupeng.exception.RetryTimeoutException;
import cn.fyupeng.net.RpcClient;
import cn.fyupeng.net.netty.client.NettyClient;
import cn.fyupeng.net.socket.client.SocketClient;
import cn.fyupeng.protocol.RpcRequest;
import cn.fyupeng.protocol.RpcResponse;
import cn.fyupeng.util.RpcMessageChecker;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Auther: fyp
 * @Date: 2022/3/22
 * @Description:
 * @Package: cn.fyupeng.proxy
 * @Version: 1.0
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

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

   private AtomicInteger sucRes = new AtomicInteger(0);
   private AtomicInteger errRes = new AtomicInteger(0);
   private AtomicInteger timeoutRes = new AtomicInteger(0);

   /**
    * @param rpcClient
    */
   public RpcClientProxy(RpcClient rpcClient) {
      this.rpcClient = rpcClient;
   }


   /**
    * 用于可 超时重试 的动态代理，需要配合 @Reference使用
    * @param pareClazz
    * @param <T>
    * @return
    */
   public <T> T getProxy(Class<T> clazz, Class<?> pareClazz){
      this.pareClazz = pareClazz;
      return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[] {clazz}, this);
   }

   /**
    用于普通动态代理，@Reference 将失效，已过时，不推荐使用
    */
   @Deprecated
   public <T> T getProxy(Class<T> clazz){
      return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[] {clazz}, this);
   }

   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      log.info("invoke method:{}#{}", method.getDeclaringClass().getName(), method.getName());


      RpcRequest rpcRequest = new RpcRequest.Builder()
              .requestId(UUID.randomUUID().toString())
              .interfaceName(method.getDeclaringClass().getName())
              .methodName(method.getName())
              .parameters(args)
              .paramTypes(method.getParameterTypes())
              /**这里心跳指定为false，一般由另外其他专门的心跳 handler 来发送
               * 如果发送 并且 hearBeat 为 true，说明超时
               */
              .heartBeat(false)
              .build();

      RpcResponse rpcResponse = null;

      if (pareClazz == null) {
         if (rpcClient instanceof NettyClient) {
            CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) rpcClient.sendRequest(rpcRequest);
            rpcResponse = completableFuture.get();
         }
         if (rpcClient instanceof SocketClient) {
            rpcResponse = (RpcResponse) rpcClient.sendRequest(rpcRequest);
         }
         RpcMessageChecker.checkAndThrow(rpcRequest, rpcResponse);
         return rpcResponse.getData();
      }

      if (rpcClient instanceof NettyClient) {
         /**
          * 重试机制实现
          */

         Field[] fields = pareClazz.getDeclaredFields();
         long timeout = 0L;
         long asyncTime = 0L;
         int retries = 0;
         boolean useRetry = false;

         /**
          * 匹配该代理方法所调用 的服务实例，是则获取相关注解信息 并跳出循环
          */
         for (Field field : fields) {
            if (field.isAnnotationPresent(Reference.class) && method.getDeclaringClass().getName().equals(field.getType().getName())) {
               retries = field.getAnnotation(Reference.class).retries();
               timeout = field.getAnnotation(Reference.class).timeout();
               asyncTime =field.getAnnotation(Reference.class).asyncTime();
               useRetry = true;
               break;
            }
         }

         if (!useRetry) {
            CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) rpcClient.sendRequest(rpcRequest);
            rpcResponse = completableFuture.get();
            RpcMessageChecker.checkAndThrow(rpcRequest, rpcResponse);
         } else {

            for (int i = 0; i <= retries; i++) {
               long startTime = System.currentTimeMillis();

               CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) rpcClient.sendRequest(rpcRequest);
               try {
                  rpcResponse = completableFuture.get(asyncTime, TimeUnit.MILLISECONDS);
               } catch (TimeoutException e) {
                  // 忽视 超时引发的异常，自行处理，防止程序中断
                  timeoutRes.incrementAndGet();
                  if (timeout >= asyncTime) {
                     log.warn("asyncTime [ {} ] should be greater than timeout [ {} ]", asyncTime, timeout);
                  }
                  continue;
               }

               long endTime = System.currentTimeMillis();
               long handleTime = endTime - startTime;
               if (handleTime >= timeout) {
                  // 超时重试
                  log.warn("invoke service timeout and retry to invoke [ rms: {}, tms: {} ]", handleTime, timeout);
                  log.info("client call timeout counts {}", timeoutRes.incrementAndGet());
               } else {
                  // 没有超时不用再重试
                  // 进一步校验包
                  if (RpcMessageChecker.check(rpcRequest, rpcResponse)) {
                     log.info("client call success counts {}", sucRes.incrementAndGet());
                     return rpcResponse.getData();
                  }
               }
            }
            log.info("client call failed counts {}", errRes.incrementAndGet());
            throw new RetryTimeoutException("重试调用超时超过阈值，通道关闭，该线程中断，强制抛出异常！");
         }

      }

      if (rpcClient instanceof SocketClient) {
         rpcResponse = (RpcResponse) rpcClient.sendRequest(rpcRequest);
         RpcMessageChecker.checkAndThrow(rpcRequest, rpcResponse);
      }
      return rpcResponse.getData();
   }
}
