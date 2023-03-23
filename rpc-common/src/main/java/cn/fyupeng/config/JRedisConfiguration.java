package cn.fyupeng.config;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

/**
 * @Auther: fyp
 * @Date: 2023/3/17
 * @Description: Jedis的Redis客户端配置类
 * @Package: cn.fyupeng.config
 * @Version: 1.0
 */
@Slf4j
public class JRedisConfiguration extends AbstractRedisConfiguration {

    private Object lock = new Object();
    private Jedis jedis;

    @Override
    public synchronized JRedisConfiguration configure() {
        if (jedis == null)
            if ("false".equals(redisServerAuth)) {
                jedis = new Jedis(redisServerHost, redisServerPort);
            } else {
                jedis = new Jedis(redisServerHost, redisServerPort);
                jedis.auth(redisServerPwd);
            }
        return this;
    }

    @Override
    public boolean exists(String key) {
        return jedis.exists(key);
    }

    @Override
    public void set(String key, String value) {
        jedis.set(key, value);
    }

    @Override
    public String get(String key) {
        return jedis.get(key);
    }

    @Override
    public boolean existsWorkerId(String hostName) {
        synchronized (lock) {
            return jedis.exists(workerIds + ":" + hostName);
        }
    }

    @Override
    public String getWorkerIdForHostName(String hostName) {
        synchronized (lock) {
            String value = jedis.get(workerIds + ":" + hostName);
            log.debug("getForHostName key[{}] - value[{}]",hostName, value);
            return value;
        }
    }

    @Override
    public void setWorkerId(String hostName, long workerId) {
        synchronized (lock) {
            log.debug("setWorkerId key[{}] - value[{}]",hostName, workerId);
            jedis.set(workerIds + ":" + hostName, String.valueOf(workerId));
        }
    }

    @Override
    public void remWorkerId(String hostName) {
        synchronized (lock) {
            log.debug("remWorkerId key[{}]",hostName);
            String workerId = getWorkerIdForHostName(hostName);
            jedis.del(workerIds + ":" + hostName);
            if (workerId != null)
                jedis.srem(workerIdsSet, workerId);
        }
    }

    @Override
    public void asyncRemWorkerId(String hostName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean existsWorkerIdSet(long workerId) {
        return jedis.sismember(workerIdsSet, String.valueOf(workerId));
    }

    @Override
    public void setWorkerIdSet(long workerId) {
        log.debug("setWorkerIdSet set[{}] - value[{}]",workerIdsSet, workerId);
        jedis.sadd(workerIdsSet, String.valueOf(workerId));
    }

    @Override
    public boolean existsRetryResult(String retryRequestId) {
        synchronized (lock) {
            return jedis.exists(retryReqIds + ":" + retryRequestId);
        }
    }

    @Override
    public String getResultForRetryRequestId2String(String retryRequestId) {
        synchronized (lock) {
            String value = jedis.get(retryReqIds + ":" + retryRequestId);
            log.debug("getForRetryRequestId key[{}] - value[{}]",retryRequestId, value);
            return value;
        }
    }

    @Override
    public byte[] getResultForRetryRequestId2Bytes(String retryRequestId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRetryRequestResultByString(String retryRequestId, String result) {
        synchronized (lock) {
            log.debug("setRetryRequestResult key[{}]- value[{}]",retryRequestId, result);
            jedis.set(retryReqIds + ":" + retryRequestId, result, "NX", "EX", 60);
        }
    }

    @Override
    public void setRetryRequestResultByBytes(String retryRequestId, byte[] result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void asyncSet(String key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void asyncSetWorkerIdSet(long workerId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void asyncSetWorkerId(String hostName, long workerId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void asyncSetRetryRequestResult(String retryRequestId, byte[] result) {
        throw new UnsupportedOperationException();
    }

}
