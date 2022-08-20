package cn.fyupeng.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;
import cn.fyupeng.exception.RpcException;
import cn.fyupeng.exception.ServiceNotFoundException;

import java.util.List;

/**
 * @Auther: fyp
 * @Date: 2022/3/28
 * @Description: 轮询选择
 * @Package: cn.fyupeng.loadbalancer
 * @Version: 1.0
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

   private int index = 0;

   @Override
   public Instance select(List<Instance> instances) throws RpcException {
      if(instances.size() == 0 ) {
         throw new ServiceNotFoundException("service instances size is zero, can't provide service! please start server first!");
      }
      return instances.get(index %= instances.size());
   }

}