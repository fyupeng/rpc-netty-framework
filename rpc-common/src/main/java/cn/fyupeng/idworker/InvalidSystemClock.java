package cn.fyupeng.idworker;

/**
 * 时钟回拨异常
 */
public class InvalidSystemClock extends RuntimeException {
    public InvalidSystemClock(String message) {
        super(message);
    }
}
