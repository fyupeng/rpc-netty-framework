package cn.fyupeng.provider;

import cn.fyupeng.exception.RpcException;

/**
 * @Auther: fyp
 * @Date: 2022/3/23
 * @Description: 服务提供者 公共接口
 * @Package: cn.fyupeng.provider
 * @Version: 1.0
 */
public interface ServiceProvider {
    // 泛型方法，故而才可以有参数类型 T
    <T> void addServiceProvider(T service, String ServiceName) throws RpcException;
    Object getServiceProvider(String serviceName) throws RpcException;

}
