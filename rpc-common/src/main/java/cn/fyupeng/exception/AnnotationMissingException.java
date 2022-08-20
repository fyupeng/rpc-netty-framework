package cn.fyupeng.exception;

/**
 * @Auther: fyp
 * @Date: 2022/3/31
 * @Description:
 * @Package: cn.fyupeng.exception
 * @Version: 1.0
 */
public class AnnotationMissingException extends RpcException {

    public AnnotationMissingException() {
        super();
    }

    public AnnotationMissingException(String message) {
        super(message);
    }
}
