package cn.fyupeng;

import cn.fyupeng.factory.ThreadPoolFactory;
import cn.fyupeng.idworker.Sid;
import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.esotericsoftware.minlog.Log;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;

/**
 * @Auther: fyp
 * @Date: 2023/3/18
 * @Description:
 * @Package: cn.fyupeng
 * @Version: 1.0
 */
@Slf4j
public class TestId {
   public static void main(String[] args) throws InterruptedException {
      //ExecutorService threadPool = ThreadPoolFactory.createDefaultThreadPool("Test-id-pool");
      ExecutorService threadPool = ThreadPoolFactory.createDefaultThreadPool("Test-id-pool");

      ConcurrentHashSet<String> idSets = new ConcurrentHashSet<>();

      log.info("loading");
      for (int i = 0; i < 20000; i++) {
         threadPool.submit(() -> {
            String next = Sid.next();
            if( !idSets.add(next) ) {
               log.info("id : {}", next);
            }
         });
         System.out.printf(".");
      }



   }
}
