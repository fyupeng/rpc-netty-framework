package cn.fyupeng.loadbalancer;

import cn.fyupeng.exception.RpcException;
import cn.fyupeng.exception.ServiceNotFoundException;
import com.alibaba.nacos.api.naming.pojo.Instance;

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
   public Instance selectService(List<Instance> instances) throws RpcException {
      if(instances.size() == 0 ) {
         throw new ServiceNotFoundException("service instances size is zero, can't provide service! please start server first!");
      }
      index++;
      return instances.get(index %= instances.size());
   }

   @Override
   public String selectNode(String[] nodes) throws RpcException {
      if(nodes.length == 0) {
         throw new ServiceNotFoundException("service instances size is zero, can't provide service! please start server first!");
      }
      index++;
      return nodes[(index %= nodes.length)];
   }

}
