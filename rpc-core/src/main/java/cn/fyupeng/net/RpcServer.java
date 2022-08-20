package cn.fyupeng.net;

import cn.fyupeng.exception.RpcException;

/**
 * @Auther: fyp
 * @Date: 2022/3/23
 * @Description:
 * @Package: cn.fyupeng.net
 * @Version: 1.0
 */
public interface RpcServer {
    void start();
    <T> void publishService(T service, String serviceClass) throws RpcException;
}
