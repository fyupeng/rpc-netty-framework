package cn.fyupeng.discovery;

import cn.fyupeng.exception.RpcException;
import cn.fyupeng.loadbalancer.LoadBalancer;

import java.net.InetSocketAddress;

/**
 * @Auther: fyp
 * @Date: 2022/3/28
 * @Description: 服务发现 接口
 * @Package: cn.fyupeng.discovery
 * @Version: 1.0
 */
public abstract class ServiceDiscovery {

    protected LoadBalancer loadBalancer;

    public ServiceDiscovery() {
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public abstract InetSocketAddress lookupService(String serviceName) throws RpcException;

    public abstract InetSocketAddress lookupService(String serviceName, String groupName) throws RpcException;
}
