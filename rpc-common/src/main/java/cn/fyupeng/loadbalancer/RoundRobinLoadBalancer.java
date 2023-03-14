package cn.fyupeng.loadbalancer;

import cn.fyupeng.exception.RpcException;
import cn.fyupeng.exception.ServiceNotFoundException;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Auther: fyp
 * @Date: 2022/3/28
 * @Description: 轮询选择
 * @Package: cn.fyupeng.loadbalancer
 * @Version: 1.0
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

   private final AtomicLong idx = new AtomicLong();

   @Override
   public <T> T selectService(List<T> services) throws RpcException {
      if(services.size() == 0 ) {
         throw new ServiceNotFoundException("service instances size is zero, can't provide service! please start server first!");
      }
      int num = (int) (idx.getAndIncrement() % (long) services.size());
      return services.get(num < 0 ? - num : num);
   }

   @Override
   public String selectNode(String[] nodes) throws RpcException {
      if(nodes.length == 0) {
         throw new ServiceNotFoundException("service instances size is zero, can't provide service! please start server first!");
      }
      int num = (int) (idx.getAndIncrement() % (long) nodes.length);
      return nodes[num < 0 ? - num : num];
   }
}