package cn.fyupeng.idworker;

import cn.fyupeng.config.AbstractRedisConfiguration;
import cn.fyupeng.idworker.exception.WorkerIdCantApplyException;
import cn.fyupeng.util.IpUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @Auther: fyp
 * @Date: 2022/11/19
 * @Description:
 * @Package: org.n3r.idworker.utils
 * @Version: 1.0
 */

@Slf4j
public class WorkerIdServer {

    private static long workerId = 0;

    private static String redisClientWay = "";

    private static AbstractRedisConfiguration redisClientConfig = AbstractRedisConfiguration.getClientConfig();

    static {
        if (workerId == 0) {
            //初始化为1
            workerId = 1;
            //得到服务器机器名称
            String hostName = IpUtils.getPubIpAddr();

            if (redisClientConfig.existsWorkerId(hostName) ) {
                // 如果redis中存在该服务器名称，则直接取得workerId
                workerId = Long.parseLong(redisClientConfig.getWorkerIdForHostName(hostName));
            } else {
                /**
                 * 采用 HashMap 哈希命中的 算法
                 * 对 为负数 的hash 值取正
                 */
                int h = hostName.hashCode() & 0x7fffffff; // = 0b0111 1111 1111 1111 1111 1111 1111 1111 = Integer.MAX_VALUE
                h = h ^ h >>> 16;
                int id = h % 1024;

                workerId = id;
                if (!redisClientConfig.existsWorkerId(hostName)) {
                    long oldWorkerId = workerId;
                    while (redisClientConfig.existsWorkerIdSet(workerId)) {
                        workerId = (workerId + 1L) % 1024;
                        if (workerId == oldWorkerId) {
                            log.error("machine code node cannot be applied, nodes number has reached its maximum value");
                            throw new WorkerIdCantApplyException(String
                                    .format("Machine code node cannot be applied, Nodes number has reached its maximum value"));
                        }
                    }
                    redisClientConfig.setWorkerId(hostName, workerId);
                    redisClientConfig.setWorkerIdSet(workerId);
                }
            }

        }
    }

    public static void preLoad() {

    }

    /**
     * 获取 机器 id
     * @param serverCode
     * @return
     */
    public static long getWorkerId(int serverCode){

        switch (serverCode) {
            case 0 : return Long.parseLong(redisClientConfig.getWorkerIdForHostName(IpUtils.getPubIpAddr()));
            case 1 :
            case 2 :
            case 3 :
            default: return Long.parseLong(redisClientConfig.getWorkerIdForHostName(IpUtils.getPubIpAddr()));
        }
    }

}
