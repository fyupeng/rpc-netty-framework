package cn.fyupeng.exception;

/**
 * @Auther: fyp
 * @Date: 2022/10/17
 * @Description: 重试超时异常
 * @Package: cn.fyupeng.exception
 * @Version: 1.0
 */
public class RetryTimeoutException extends RpcException {

    public RetryTimeoutException() {
        super();
    }

    public RetryTimeoutException(String message) {
        super(message);
    }
}
