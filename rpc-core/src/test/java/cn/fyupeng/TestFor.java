package cn.fyupeng;

/**
 * @Auther: fyp
 * @Date: 2023/3/21
 * @Description:
 * @Package: cn.fyupeng
 * @Version: 1.0
 */
public class TestFor {
   public static void main(String[] args) {

   }

   //private void TestFor(int retries, int timeout) {
   //   long handleTime = 0;
   //   boolean checkPass = false;
   //   for (int i = 0; i < retries; i++) {
   //      if (handleTime >= timeout) {
   //         // 超时重试
   //         log.warn("call service timeout and retry to call [ rms: {}, tms: {} ]", handleTime, timeout);
   //      }
   //      long startTime = System.currentTimeMillis();
   //      log.info("start calling remote service");
   //      Random random = new Random();
   //      CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) rpcClient.sendRequest(rpcRequest);
   //      try {
   //      } catch (TimeoutException e) {
   //         // 忽视 超时引发的异常，自行处理，防止程序中断
   //         log.warn("recommend that asyncTime [ {} ] should be greater than current task runeTime [ {} ]", , System.currentTimeMillis() - startTime);
   //         continue;
   //      }
   //
   //      long endTime = System.currentTimeMillis();
   //      handleTime = endTime - startTime;
   //
   //      if (handleTime < timeout) {
   //         // 没有超时不用再重试
   //         // 进一步校验包
   //         checkPass = RpcMessageChecker.check(rpcRequest, rpcResponse);
   //         if (checkPass) {
   //            log.info("client call success [ rms: {}, tms: {} ]", handleTime, timeout);
   //            return rpcResponse;
   //         }
   //         // 包被 劫持触发 超时重发机制 保护重发
   //      }
   //   }
   //   log.info("client call failed  [ rms: {}, tms: {} ]", handleTime, timeout);
   //}

}
