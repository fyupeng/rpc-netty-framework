package cn.fyupeng.enums;

/**
 * @Auther: fyp
 * @Date: 2022/10/3
 * @Description:
 * @Package: cn.fyupeng.enums
 * @Version: 1.0
 */
public enum LoadBalancerCode {
    /**
     * 随机负载策略
      */
    RANDOM(0),
    /**
     * 轮询负载策略
     */
    ROUNDROBIN(1);
    private final int code;

    LoadBalancerCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
