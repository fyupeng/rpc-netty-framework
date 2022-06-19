package com.zhkucst.exception;

/**
 * @Auther: fyp
 * @Date: 2022/3/26
 * @Description:
 * @Package: com.zhkucst.exception
 * @Version: 1.0
 */
public class ConnectFailedException extends RpcException {

    public ConnectFailedException() {
        super();
    }

    public ConnectFailedException(String message) {
        super(message);
    }
}
