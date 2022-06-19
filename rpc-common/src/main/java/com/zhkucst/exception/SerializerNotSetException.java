package com.zhkucst.exception;

/**
 * @Auther: fyp
 * @Date: 2022/3/26
 * @Description: \
 * @Package: com.zhkucst.exception
 * @Version: 1.0
 */
public class SerializerNotSetException extends RpcException {

    public SerializerNotSetException() {
        super();
    }

    public SerializerNotSetException(String message) {
        super(message);
    }
}
