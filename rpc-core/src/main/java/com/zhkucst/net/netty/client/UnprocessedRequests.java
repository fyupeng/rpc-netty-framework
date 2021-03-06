package com.zhkucst.net.netty.client;

import com.zhkucst.protocol.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Auther: fyp
 * @Date: 2022/3/29
 * @Description: 未处理的请求
 * @Package: com.zhkucst.net.netty.client
 * @Version: 1.0
 */
public class UnprocessedRequests {

   /**
    * k - request id
    * v - 可将来获取 的 response
    */
   private static ConcurrentMap<String, CompletableFuture<RpcResponse>> unprocessedResponseFutures = new ConcurrentHashMap<>();

   /**
    * @param requestId 请求体的 requestId 字段
    * @param future 经过 CompletableFuture 包装过的 响应体
    */
   public void put(String requestId, CompletableFuture<RpcResponse> future) {
      System.out.println("put" + future);
      unprocessedResponseFutures.put(requestId, future);
   }

   /**
    * 移除 CompletableFuture<RpcResponse>
    * @param requestId 请求体的 requestId 字段
    */
   public void remove(String requestId) {
      unprocessedResponseFutures.remove(requestId);
   }

   public void complete(RpcResponse rpcResponse) {
      CompletableFuture<RpcResponse> completableFuture = unprocessedResponseFutures.remove(rpcResponse.getRequestId());
      completableFuture.complete(rpcResponse);
      System.out.println("remove" + completableFuture);
   }



}
