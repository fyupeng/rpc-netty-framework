package com.zhkucst.exception;

/**
 * @Auther: fyp
 * @Date: 2022/3/26
 * @Description:
 * @Package: com.zhkucst.exception
 * @Version: 1.0
 */
public class RegisterFailedException extends RpcException {

    public RegisterFailedException() {
        super();
    }

    public RegisterFailedException(String message) {
        super(message);
    }
}
