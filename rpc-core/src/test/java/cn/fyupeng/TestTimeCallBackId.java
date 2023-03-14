package cn.fyupeng;

import cn.fyupeng.factory.ThreadPoolFactory;
import cn.fyupeng.idworker.Sid;
import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import lombok.extern.slf4j.Slf4j;
import sun.nio.ch.ThreadPool;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Auther: fyp
 * @Date: 2023/3/13
 * @Description:
 * @Package: cn.fyupeng.idworker.utils
 * @Version: 1.0
 */

@Slf4j
public class TestTimeCallBackId {
   static AtomicInteger error = new AtomicInteger(0);
   static AtomicInteger success = new AtomicInteger(0);
   public static void main(String[] args) throws InterruptedException {
      ConcurrentHashSet set = new ConcurrentHashSet<String>();
      ExecutorService threadPool = ThreadPoolFactory.createDefaultThreadPool("Test-id-pool");


      for(int i = 0; i < 200000; i++) {
         //Thread.sleep(1);
         threadPool.submit(() -> {
            String id = Sid.next();
            if (!set.add(id)) {
               log.error("occur error id " + id);
               error.incrementAndGet();
            } else {
               success.incrementAndGet();
            }
         });
      }

      Thread.sleep(32000);

      System.out.println("success " + success);
      System.out.println("error " + error);
   }
}
