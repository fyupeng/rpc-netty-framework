package com.zhkucst.provider;

import com.zhkucst.exception.RpcException;

/**
 * @Auther: fyp
 * @Date: 2022/3/23
 * @Description: 服务提供者 公共接口
 * @Package: com.zhkucst.provider
 * @Version: 1.0
 */
public interface ServiceProvider {
    // 泛型方法，故而才可以有参数类型 T
    <T> void addServiceProvider(T service, String ServiceName) throws RpcException;
    Object getServiceProvider(String serviceName) throws RpcException;

}
