package com.zhkucst.exception;

/**
 * @Auther: fyp
 * @Date: 2022/3/23
 * @Description: 服务未找到错误
 * @Package: com.zhkucst.exception
 * @Version: 1.0
 */
public class ServiceNotFoundException extends RpcException {

    public ServiceNotFoundException() {
    }

    public ServiceNotFoundException(String message) {
        super(message);
    }
}
