package cn.fyupeng.discovery;

import cn.fyupeng.exception.RpcException;

import java.net.InetSocketAddress;

/**
 * @Auther: fyp
 * @Date: 2022/3/28
 * @Description: 服务发现 接口
 * @Package: cn.fyupeng.discovery
 * @Version: 1.0
 */
public interface ServiceDiscovery {
    InetSocketAddress lookupService(String serviceName) throws RpcException;
}
