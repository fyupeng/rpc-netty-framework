package cn.fyupeng.exception;

/**
 * @Auther: fyp
 * @Date: 2023/1/3
 * @Description: 异步时间设置不合理异常
 * @Package: cn.fyupeng.exception
 * @Version: 1.0
 */
public class AsyncTimeUnreasonableException extends RpcException {

    public AsyncTimeUnreasonableException() {
        super();
    }

    public AsyncTimeUnreasonableException(String message) {
        super(message);
    }
}
