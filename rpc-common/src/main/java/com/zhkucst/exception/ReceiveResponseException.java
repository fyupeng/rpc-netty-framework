package com.zhkucst.exception;

/**
 * @Auther: fyp
 * @Date: 2022/3/27
 * @Description:
 * @Package: com.zhkucst.exception
 * @Version: 1.0
 */
public class ReceiveResponseException extends RpcException {

    public ReceiveResponseException() {
        super();
    }

    public ReceiveResponseException(String message) {
        super(message);
    }
}
