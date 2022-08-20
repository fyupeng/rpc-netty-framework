package cn.fyupeng.exception;

/**
 * @Auther: fyp
 * @Date: 2022/3/23
 * @Description:
 * @Package: cn.fyupeng.exception
 * @Version: 1.0
 */
public class RpcException extends Exception {

    public RpcException() {
        super();
    }

    public RpcException(String message) {
        super(message);
    }

}
