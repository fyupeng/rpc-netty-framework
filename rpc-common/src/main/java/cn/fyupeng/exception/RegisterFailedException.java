package cn.fyupeng.exception;

/**
 * @Auther: fyp
 * @Date: 2022/3/26
 * @Description:
 * @Package: cn.fyupeng.exception
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
