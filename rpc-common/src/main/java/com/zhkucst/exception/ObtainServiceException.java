package com.zhkucst.exception;

/**
 * @Auther: fyp
 * @Date: 2022/3/26
 * @Description:
 * @Package: com.zhkucst.exception
 * @Version: 1.0
 */
public class ObtainServiceException extends RpcException{

    public ObtainServiceException() {
        super();
    }

    public ObtainServiceException(String message) {
        super(message);
    }
}
