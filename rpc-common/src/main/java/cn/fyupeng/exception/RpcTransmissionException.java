package cn.fyupeng.exception;

/**
 * @Auther: fyp
 * @Date: 2023/1/7
 * @Description: 数据传输异常
 * @Package: cn.fyupeng.exception
 * @Version: 1.0
 */
public class RpcTransmissionException extends RpcException {

    public RpcTransmissionException() {
        super();
    }

    public RpcTransmissionException(String message) {
        super(message);
    }
}
