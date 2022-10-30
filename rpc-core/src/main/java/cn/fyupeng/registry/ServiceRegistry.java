package cn.fyupeng.registry;


import cn.fyupeng.exception.RpcException;

import java.net.InetSocketAddress;

/**
 * @Auther: fyp
 * @Date: 2022/3/23
 * @Description: 注册表公共接口
 * @Package: cn.fyupeng.registry
 * @Version: 1.0
 */
public interface ServiceRegistry {
    void register(String serviceName, InetSocketAddress inetSocketAddress) throws RpcException;

    void register(String serviceName, String groupName, InetSocketAddress inetSocketAddress) throws RpcException;
}
