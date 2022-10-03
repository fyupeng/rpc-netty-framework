package cn.fyupeng.proxy;


import cn.fyupeng.net.RpcClient;
import cn.fyupeng.net.netty.client.NettyClient;
import cn.fyupeng.net.socket.client.SocketClient;
import cn.fyupeng.protocol.RpcRequest;
import cn.fyupeng.protocol.RpcResponse;
import cn.fyupeng.util.RpcMessageChecker;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

   public RpcClientProxy(RpcClient rpcClient) {
      this.rpcClient = rpcClient;
   }

   /**
    * public RpcClientProxy(String hostName, int port) {
      this.hostName = hostName;
      this.port = port;
   }
    */

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

      if (rpcClient instanceof NettyClient) {
         CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) rpcClient.sendRequest(rpcRequest);
         rpcResponse = completableFuture.get();
      }
      if (rpcClient instanceof SocketClient) {
         rpcResponse = (RpcResponse) rpcClient.sendRequest(rpcRequest);
      }
      RpcMessageChecker.check(rpcRequest, rpcResponse);
      return rpcResponse.getData();
   }
}
