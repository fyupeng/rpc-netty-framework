package cn.fyupeng.net.netty.client;

import cn.fyupeng.protocol.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Auther: fyp
 * @Date: 2022/3/29
 * @Description: 未处理的请求
 * @Package: cn.fyupeng.net.netty.client
 * @Version: 1.0
 */
@Slf4j
public class UnprocessedResults<T> {

   /**
    * k - request id
    * v - 可将来获取 的 response
    */
   private ConcurrentMap<String, CompletableFuture<T>> unprocessedResponseFutures = new ConcurrentHashMap<>();
   private static ConcurrentMap<String, Integer> unprocessedResponseReentrantCounts = new ConcurrentHashMap<>();

   /**
    * 订阅
    * @param id key
    * @param future 经过 CompletableFuture 包装过的 响应体
    */
   public void put(String id, CompletableFuture<T> future) {
      Integer reentrantCount = unprocessedResponseReentrantCounts.get(id);
      if (unprocessedResponseFutures.containsKey(id)) {
         unprocessedResponseReentrantCounts.put(id, reentrantCount + 1);
         return;
      }
      unprocessedResponseFutures.put(id, future);
      unprocessedResponseReentrantCounts.put(id, 0);
   }

   /**
    * 移除 CompletableFuture<T>
    * @param id key
    */
   public void remove(String id) {
      Integer reentrantCount = unprocessedResponseReentrantCounts.get(id);
      if (unprocessedResponseFutures.containsKey(id) && reentrantCount > 0) {
         unprocessedResponseReentrantCounts.put(id, reentrantCount - 1);
         return;
      }
      unprocessedResponseFutures.remove(id);
   }

   /**
    * 通知
    * @param result 结果
    */
   public void complete(String id, T result) {
      Integer reentrantCount = unprocessedResponseReentrantCounts.get(id);
      if (unprocessedResponseFutures.containsKey(id) && reentrantCount > 0) {
         unprocessedResponseReentrantCounts.put(id, reentrantCount - 1);
         CompletableFuture<T> completableFuture = unprocessedResponseFutures.get(id);
         completableFuture.complete(result);
         return;
      }
      CompletableFuture<T> completableFuture = unprocessedResponseFutures.remove(id);
      completableFuture.complete(result);
   }

}
