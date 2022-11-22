package cn.fyupeng.idworker.utils;


import cn.fyupeng.util.PropertiesConstants;
import com.alibaba.nacos.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * @Auther: fyp
 * @Date: 2022/11/19
 * @Description: Redis分布式锁 api
 * @Package: org.n3r.idworker.utils
 * @Version: 1.0
 */

@Slf4j
public class JRedisHelper {

    private static Object lock = new Object();
    private static final String workerIds = "worker-ids";
    private static final String workerIdsSet = "worker-ids-set";
    private static final String retryReqIds = "retry-req-ids";
    private static Jedis jedis;
    private final static String DEFAULT_ADDRESS = "127.0.0.1:6379";


    //2. 加载配置文件，只需加载一次
    static {
        // 使用InPutStream流读取properties文件
        String currentWorkPath = System.getProperty("user.dir");
        InputStream is = null;
        PropertyResourceBundle configResource = null;
        String redisAuth = "";
        String redisPwd = "";
        String redisAddr = "";
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(currentWorkPath + "/config/resource.properties"));) {

            configResource = new PropertyResourceBundle(bufferedReader);
            redisAuth = configResource.getString(PropertiesConstants.REDIS_SERVER_AUTH);

            if ("false".equals(redisAuth) || "default".equals(redisAuth) || StringUtils.isBlank(redisAuth)) {
                log.info("--- no redis auth ---");
                try {
                    redisAddr = configResource.getString(PropertiesConstants.REDIS_SERVER_ADDR);
                    String[] hostAndPort = redisAddr.split(":");
                    jedis = new Jedis(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
                } catch (MissingResourceException redisServerAddressException) {
                    String[] hostAndPort = DEFAULT_ADDRESS.split(":");
                    jedis = new Jedis(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
                    log.warn("redis server address attribute is missing");
                    log.info("use default redis server address : " + DEFAULT_ADDRESS);
                }
            } else if ("true".equals(redisAuth)) {
                log.info("redis auth attribute is true and start with auth");
                try {
                    redisAddr = configResource.getString(PropertiesConstants.REDIS_SERVER_ADDR);
                } catch (MissingResourceException redisServerAddressException) {
                    log.info("redis server address property attribute is missing: {}", redisServerAddressException.getMessage());
                    log.info("use default redis server address : " + DEFAULT_ADDRESS);
                    String[] hostAndPort = DEFAULT_ADDRESS.split(":");
                    jedis = new Jedis(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
                }
                try {
                    redisPwd = configResource.getString(PropertiesConstants.REDIS_SERVER_PWD);
                    String[] hostAndPort = redisAddr.split(":");
                    jedis = new Jedis(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
                    jedis.auth(redisPwd);
                } catch (MissingResourceException redisPasswordException) {
                    log.error("redis password attribute is missing: ", redisPasswordException);
                    throw new RuntimeException("redis password attribute is missing!");
                }
            } else {
                throw new RuntimeException("redis auth attribute is illegal!");
            }
            log.info("read resource from resource path: {}", currentWorkPath + "/config/resource.properties");
        } catch (MissingResourceException redisAuthException) {
            log.warn("redis auth attribute is missing and start with no auth");
            try {
                String redisAddress = configResource.getString(PropertiesConstants.REDIS_SERVER_ADDR);
                redisAddr = StringUtils.isBlank(redisAddress) ? DEFAULT_ADDRESS : redisAddress;
                String[] hostAndPort = redisAddr.split(":");
                jedis = new Jedis(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
            } catch (MissingResourceException redisServerAddressException) {
                String[] hostAndPort = DEFAULT_ADDRESS.split(":");
                jedis = new Jedis(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
                log.warn("redis server address attribute is missing");
                log.info("use default redis server address : " + DEFAULT_ADDRESS);
            }
        } catch (IOException ioException) {
            log.info("not found resource from resource path: {}", currentWorkPath + "/config/resource.properties");
            try {
                ResourceBundle resource = ResourceBundle.getBundle("resource");

                try {
                    redisAuth = resource.getString(PropertiesConstants.REDIS_SERVER_AUTH);

                    if ("false".equals(redisAuth) || "default".equals(redisAuth) || StringUtils.isBlank(redisAuth)) {
                        log.info("--- no redis auth ---");
                        try {
                            redisAddr = resource.getString(PropertiesConstants.REDIS_SERVER_ADDR);
                            String[] hostAndPort = redisAddr.split(":");
                            jedis = new Jedis(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
                        } catch (MissingResourceException redisServerAddressException) {
                            String[] hostAndPort = DEFAULT_ADDRESS.split(":");
                            jedis = new Jedis(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
                            log.warn("redis server address attribute is missing");
                            log.info("use default redis server address : " + DEFAULT_ADDRESS);
                        }
                    } else if ("true".equals(redisAuth)) {
                        log.info("redis auth attribute is true and start with auth");
                        try {
                            redisAddr = resource.getString(PropertiesConstants.REDIS_SERVER_ADDR);
                        } catch (MissingResourceException redisServerAddressException) {
                            log.info("redis server address property attribute is missing: {}", redisServerAddressException.getMessage());
                            log.info("use default redis server address : " + DEFAULT_ADDRESS);
                            String[] hostAndPort = DEFAULT_ADDRESS.split(":");
                            jedis = new Jedis(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
                        }
                        try {
                            redisPwd = resource.getString(PropertiesConstants.REDIS_SERVER_PWD);
                            String[] hostAndPort = redisAddr.split(":");
                            jedis = new Jedis(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
                            jedis.auth(redisPwd);
                        } catch (MissingResourceException redisPasswordException) {
                            log.error("redis password attribute is missing: ", redisPasswordException);
                            throw new RuntimeException("redis password attribute is missing!");
                        }
                    } else {
                        throw new RuntimeException("redis auth attribute is illegal!");
                    }
                } catch (MissingResourceException clusterUseException) {
                    log.info("redis auth attribute is missing and start with no auth");
                    redisAddr = resource.getString(PropertiesConstants.REDIS_SERVER_ADDR);
                    String[] hostAndPort = redisAddr.split(":");
                    jedis = new Jedis(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
                }

            } catch (MissingResourceException resourceException) {
                log.info("not found resource from resource path: {}", "resource.properties");
                log.info("Connect to default address {}", DEFAULT_ADDRESS);
                String[] hostAndPort = DEFAULT_ADDRESS.split(":");
                jedis = new Jedis(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
            }
            log.info("read resource from resource path: {}", "resource.properties");
        }
    }

    public static boolean exists(String key) {
        return jedis.exists(key);
    }

    public static void set(String key, String value) {
        jedis.set(key, value);
    }

    public static String get(String key) {
        return jedis.get(key);
    }


    public static boolean existsWorkerId(String hostName) {
        synchronized (lock) {
            return jedis.exists(workerIds + ":" + hostName);
        }
    }

    public static String getForHostName(String hostName) {
        synchronized (lock) {
            log.trace("getForHostName key[{}]",hostName);
            return jedis.get(workerIds + ":" + hostName);
        }
    }

    public static void setWorkerId(String hostName, long workerId) {
        synchronized (lock) {
            log.trace("setWorkerId key[{}]",hostName);
            jedis.set(workerIds + ":" + hostName, String.valueOf(workerId));
        }
    }

    public static void remWorkerId(String hostName) {
        synchronized (lock) {
            log.trace("remWorkerId key[{}]",hostName);
            String workerId = getForHostName(hostName);
            jedis.del(workerIds + ":" + hostName);
            if (workerId != null)
                jedis.srem(workerIdsSet, workerId);
        }
    }

    public static boolean existsWorkerIdSet(long workerId) {
        return jedis.sismember(workerIdsSet, String.valueOf(workerId));
    }

    public static void setWorkerIdSet(long workerId) {
        jedis.sadd(workerIdsSet, String.valueOf(workerId));
    }

    public static boolean existsRetryResult(String retryRequestId) {
        synchronized (lock) {
            return jedis.exists(retryReqIds + ":" + retryRequestId);
        }
    }

    public static String getForRetryRequestId(String retryRequestId) {
        synchronized (lock) {
            log.trace("getForRetryRequestId key[{}]",retryRequestId);
            return jedis.get(retryReqIds + ":" + retryRequestId);
        }
    }

    public static void setRetryRequestResult(String retryRequestId, String result) {
        synchronized (lock) {
            log.trace("setRetryRequestResult key[{}]- value[{}]",retryRequestId, result);
            jedis.set(retryReqIds + ":" + retryRequestId, result, "NX", "EX", 60);
        }
    }

    public static void main(String[] args) {

    }


}
