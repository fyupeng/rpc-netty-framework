package com.zhkucst.net;

import com.zhkucst.exception.RpcException;

/**
 * @Auther: fyp
 * @Date: 2022/3/23
 * @Description:
 * @Package: com.zhkucst.net
 * @Version: 1.0
 */
public interface RpcServer {
    void start();
    <T> void publishService(T service, String serviceClass) throws RpcException;
}
