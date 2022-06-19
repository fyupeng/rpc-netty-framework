package com.zhkucst.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.Random;

/**
 * @Auther: fyp
 * @Date: 2022/3/28
 * @Description: 随机选择
 * @Package: com.zhkucst.loadbalancer
 * @Version: 1.0
 */
public class RandomLoadBalancer implements LoadBalancer{

    @Override
    public Instance select(List<Instance> instances) {
        return instances.get(new Random().nextInt(instances.size()));
    }

}
