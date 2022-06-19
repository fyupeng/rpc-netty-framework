package com.zhkucst.exception;

/**
 * @Auther: fyp
 * @Date: 2022/3/24
 * @Description: 无法识别错误
 * @Package: com.zhkucst.exception
 * @Version: 1.0
 */
public class UnrecognizedException extends RpcException {

    public UnrecognizedException() {
        super();
    }

    public UnrecognizedException(String message) {
        super(message);
    }
}
