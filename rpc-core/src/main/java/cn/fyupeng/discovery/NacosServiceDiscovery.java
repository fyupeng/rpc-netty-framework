package cn.fyupeng.discovery;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import cn.fyupeng.exception.ObtainServiceException;
import cn.fyupeng.exception.RpcException;
import cn.fyupeng.loadbalancer.LoadBalancer;
import cn.fyupeng.util.NacosUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @Auther: fyp
 * @Date: 2022/3/28
 * @Description: nacos 服务发现
 * @Package: cn.fyupeng.discovery
 * @Version: 1.0
 */
@Slf4j
public class NacosServiceDiscovery implements ServiceDiscovery {

    private final LoadBalancer loadBalancer;

    public NacosServiceDiscovery(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) throws RpcException {
        try {
            List<Instance> instances = NacosUtils.getAllInstance(serviceName);
            Instance instance = loadBalancer.select(instances);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            log.error("error occurred while fetching the service:{}",e.getMessage());
            throw new ObtainServiceException("error occurred while fetching the service Exception");
        } catch (RpcException e) {
            log.error("service instances size is zero, can't provide service! please start server first! Exception: {}",e.getMessage());
            throw e;
        }
    }
}