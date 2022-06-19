package com.zhkucst.exception;

/**
 * @Auther: fyp
 * @Date: 2022/3/27
 * @Description:
 * @Package: com.zhkucst.exception
 * @Version: 1.0
 */
public class UnSupportBodyException extends RpcException {

    public UnSupportBodyException() {
        super();
    }

    public UnSupportBodyException(String message) {
        super(message);
    }
}
