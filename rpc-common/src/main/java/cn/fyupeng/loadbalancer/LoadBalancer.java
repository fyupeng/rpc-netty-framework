package cn.fyupeng.loadbalancer;

import cn.fyupeng.exception.RpcException;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * @Auther: fyp
 * @Date: 2022/3/28
 * @Description: 负载均衡 接口
 * @Package: cn.fyupeng.loadbalancer
 * @Version: 1.0
 */
public interface LoadBalancer {
   Instance selectService(List<Instance> instances) throws RpcException;

   String selectNode(String[] nodes) throws RpcException;

   static LoadBalancer getByCode(int code) {
      switch (code) {
         case 0:
            return new RandomLoadBalancer();
         case 1:
            return new RoundRobinLoadBalancer();
         default:
            return null;
      }
   }
}
