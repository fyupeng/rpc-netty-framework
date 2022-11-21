package cn.fyupeng.idworker.enums;

/**
 * @Auther: fyp
 * @Date: 2022/11/19
 * @Description: 锁常量
 * @Package: org.n3r.idworker.constant
 * @Version: 1.0
 */
public enum ServerSelector {
    // Redis 服务
    REDIS_SERVER(0),
    ZOOKEEPER_SERVER(1),
    CACHE_SERVER(2);

    private final Integer code;

     ServerSelector(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
