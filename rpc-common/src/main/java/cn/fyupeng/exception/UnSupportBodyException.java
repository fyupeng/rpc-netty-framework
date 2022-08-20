package cn.fyupeng.exception;

/**
 * @Auther: fyp
 * @Date: 2022/3/27
 * @Description:
 * @Package: cn.fyupeng.exception
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
