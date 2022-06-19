package com.zhkucst.exception;

/**
 * @Auther: fyp
 * @Date: 2022/3/23
 * @Description: 服务未实现一个接口错误
 * @Package: com.zhkucst.exception
 * @Version: 1.0
 */
public class ServiceNotImplException extends RpcException {

    public ServiceNotImplException() {
        super();
    }

    public ServiceNotImplException(String message) {
        super(message);
    }

}
