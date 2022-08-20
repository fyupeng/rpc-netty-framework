package cn.fyupeng.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;
import cn.fyupeng.exception.RpcException;

import java.util.List;

/**
 * @Auther: fyp
 * @Date: 2022/3/28
 * @Description: 负载均衡 接口
 * @Package: cn.fyupeng.loadbalancer
 * @Version: 1.0
 */
public interface LoadBalancer {
   Instance select(List<Instance> instances) throws RpcException;
}
