package cn.fyupeng.loadbalancer;

import cn.fyupeng.exception.RpcException;
import cn.fyupeng.exception.ServiceNotFoundException;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.Random;

/**
 * @Auther: fyp
 * @Date: 2022/3/28
 * @Description: 随机选择
 * @Package: cn.fyupeng.loadbalancer
 * @Version: 1.0
 */
public class RandomLoadBalancer implements LoadBalancer{

    @Override
    public Instance selectService(List<Instance> instances) throws RpcException {
        if(instances.size() == 0 ) {
            throw new ServiceNotFoundException("service instances size is zero, can't provide service! please start server first!");
        }
        return instances.get(new Random().nextInt(instances.size()));
    }

    @Override
    public String selectNode(String[] nodes) throws RpcException {
        if(nodes.length == 0) {
            throw new ServiceNotFoundException("service instances size is zero, can't provide service! please start server first!");
        }
        return nodes[new Random().nextInt(nodes.length)];
    }

}
