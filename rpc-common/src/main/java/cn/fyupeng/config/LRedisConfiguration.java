package cn.fyupeng.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.time.Duration;

/**
 * @Auther: fyp
 * @Date: 2023/3/17
 * @Description: Lettuce的Redis客户端配置类
 * @Package: cn.fyupeng.config
 * @Version: 1.0
 */
@Slf4j
public class LRedisConfiguration extends AbstractRedisConfiguration {

    private RedisURI.Builder uriBuilder;

    private StatefulRedisConnection<String, byte[]> strToByteConn;
    private StatefulRedisConnection<String, String> strToStrConn;
    private RedisAsyncCommands<String, byte[]> strToByteAsyncCommand;
    private RedisAsyncCommands<String, String> strToStrAsyncCommand;
    private RedisCommands<String, byte[]> strToByteSyncCommand;
    private RedisCommands<String, String> strToStrSyncCommand;

    @Override
    public synchronized AbstractRedisConfiguration configure() {
        if (uriBuilder == null) {
            uriBuilder = RedisURI.builder();
            if ("false".equals(redisServerAuth)) {
                uriBuilder.withHost(redisServerHost)
                        .withPort(redisServerPort);
            } else {
                uriBuilder.withHost(redisServerHost)
                        .withPort(redisServerPort)
                        .withAuthentication("default", redisServerPwd);
            }
            RedisURI uri = uriBuilder.build();

            ClientResources resources = DefaultClientResources.builder()
                    .ioThreadPoolSize(4)	//设置I/O线程池大小（默认cpu数）仅在没有提供eventLoopGroupProvider时有效
                    .computationThreadPoolSize(4)	//设置用于计算的任务线程数（默认cpu数）仅在没有提供eventExecutorGroup时有效
//                .reconnectDelay(Delay.constant(Duration.ofSeconds(10)))	//设置无状态尝试重连延迟，默认延迟上限30s
                    .build();
            RedisClient client = RedisClient.create(resources, uri);
            ClientOptions options = ClientOptions.builder()
                    .autoReconnect(true)	//设置自动重连
                    .pingBeforeActivateConnection(true)	//激活连接前执行PING命令
//				.timeoutOptions(TimeoutOptions.enabled(Duration.ofSeconds(5)))	//命令超时
                    .build();
            client.setOptions(options);
            client.setDefaultTimeout(Duration.ofSeconds(5));	//为客户端创建的连接设置默认超时时间，适用于尝试连接和非阻塞命令
            strToByteConn = client.connect(RedisCodec.of(new StringCodec(), new ByteArrayCodec()));
            strToStrConn = client.connect();
            strToByteAsyncCommand = strToByteConn.async();
            strToStrAsyncCommand = strToStrConn.async();
            strToByteSyncCommand = strToByteConn.sync();
            strToStrSyncCommand = strToStrConn.sync();
        }
        return this;
    }

    public boolean exists(String key) {
        return strToStrSyncCommand.exists(key) != 0L;
    }

    public void set(String key, String value) {
        log.debug("syncSet key[{}] - value[{}]",key, value);
        strToStrSyncCommand.set(key, value);
    }

    public void asyncSet(String key, String value) {
        log.debug("asyncSet key[{}] - value[{}]",key, value);
        strToStrAsyncCommand.set(key, value);
    }

    public String get(String key) {
        String value = strToStrSyncCommand.get(key);
        log.debug("get key[{}] - value[{}]",key, value);
        return value;
    }

    public boolean existsWorkerIdSet(long workerId) {
        return strToStrSyncCommand.sismember(workerIdsSet, String.valueOf(workerId));
    }

    @Override
    public void setWorkerIdSet(long workerId) {
        log.debug("setWorkerIdSet set[{}] - value[{}]",workerIdsSet, workerId);
        strToStrSyncCommand.sadd(workerIdsSet, String.valueOf(workerId));
    }

    public void setWorkerIdSet(String workerId) {
        strToStrSyncCommand.sadd(workerIdsSet, workerId);
    }

    public void asyncSetWorkerIdSet(long workerId) {
        log.debug("asyncSetWorkerIdSet set[{}] - value[{}]",workerIdsSet, workerId);
        strToStrAsyncCommand.sadd(workerIdsSet, String.valueOf(workerId));
    }

    public boolean existsWorkerId(String hostName) {
        return strToStrSyncCommand.exists(workerIds + ":" + hostName) != 0L;
    }

    public String getWorkerIdForHostName(String hostName) {
        String value = strToStrSyncCommand.get(workerIds + ":" + hostName);
        log.debug("getForHostName key[{}] - value[{}]",hostName, value);
        return value;
    }

    public void asyncSetWorkerId(String hostName, long workerId) {
        log.debug("asyncSetWorkerId key[{}] - value[{}]",hostName, workerId);
        strToStrAsyncCommand.set(workerIds + ":" + hostName, String.valueOf(workerId));
    }

    public void setWorkerId(String hostName, long workerId) {
        log.debug("SetWorkerId key[{}] - value[{}]",hostName, workerId);
        strToStrSyncCommand.set(workerIds + ":" + hostName, String.valueOf(workerId));
    }

    @Override
    public void remWorkerId(String hostName) {
        log.debug("remWorkerId key[{}]",hostName);
        String workerId = getWorkerIdForHostName(hostName);
        strToStrSyncCommand.del(workerIds + ":" + hostName);
        if (workerId != null)
            strToStrSyncCommand.srem(workerIdsSet, workerId);
    }

    @Override
    public void asyncRemWorkerId(String hostName) {
        log.debug("remWorkerId key[{}]",hostName);
        String workerId = getWorkerIdForHostName(hostName);
        strToStrAsyncCommand.del(workerIds + ":" + hostName);
        if (workerId != null)
            strToStrAsyncCommand.srem(workerIdsSet, workerId);
    }

    public boolean existsRetryResult(String retryRequestId) {
        return strToStrSyncCommand.exists(retryReqIds + ":" + retryRequestId) != 0L;
    }

    @Override
    public String getResultForRetryRequestId2String(String retryRequestId) {
        throw new UnsupportedOperationException();
    }

    public byte[] getResultForRetryRequestId2Bytes(String retryRequestId) {
        byte[] value = strToByteSyncCommand.get(retryReqIds + ":" + retryRequestId);
        log.debug("getForRetryRequestId key[{}] - value[{}]",retryRequestId, value);
        return value;
    }

    @Override
    public void setRetryRequestResultByString(String retryRequestId, String result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRetryRequestResultByBytes(String retryRequestId, byte[] result) {
        log.debug("syncSetRetryRequestResult key[{}] - value[{}]",retryRequestId, result);
        strToByteSyncCommand.set(retryReqIds + ":" + retryRequestId, result, new SetArgs().nx().ex(60));
    }

    public void asyncSetRetryRequestResult(String retryRequestId, byte[] result) {
        log.debug("aSetRetryRequestResult key[{}] - value[{}]",retryRequestId, result);
        strToByteAsyncCommand.set(retryReqIds + ":" + retryRequestId, result, new SetArgs().nx().ex(60));
    }

}
