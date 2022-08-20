package cn.fyupeng.exception;

/**
 * @Auther: fyp
 * @Date: 2022/3/26
 * @Description: \
 * @Package: cn.fyupeng.exception
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
