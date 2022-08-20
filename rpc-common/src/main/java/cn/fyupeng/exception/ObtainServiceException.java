package cn.fyupeng.exception;

/**
 * @Auther: fyp
 * @Date: 2022/3/26
 * @Description:
 * @Package: cn.fyupeng.exception
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
