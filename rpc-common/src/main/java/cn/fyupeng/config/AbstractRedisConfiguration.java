package cn.fyupeng.config;

import cn.fyupeng.constant.PathConstants;
import cn.fyupeng.constant.PropertiesConstants;
import cn.fyupeng.factory.SingleFactory;
import com.alibaba.nacos.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * @Auther: fyp
 * @Date: 2023/3/17
 * @Description: Redis配置类
 * @Package: cn.fyupeng.config
 * @Version: 1.0
 */

@Slf4j
public abstract class AbstractRedisConfiguration implements Configuration {
    private static String defaultServerAddr = "127.0.0.1:6379";
    private static String defaultServerWay = "lettuce";
    private static String defaultClientWay = "lettuce";
    private static String defaultServerAsync = "false";

    protected static final String workerIds = "worker-ids";
    protected static final String workerIdsSet = "worker-ids-set";
    protected static final String retryReqIds = "retry-req-ids";

    private static String redisServerAddr;
    protected static String redisServerHost;
    protected static Integer redisServerPort;
    protected static String redisServerAuth;
    protected static String redisServerPwd = "";
    protected static String redisServerWay;
    protected static String redisClientWay;

    public static String getRedisServerAddr() {
        return redisServerAddr;
    }

    public static String getRedisServerHost() {
        return redisServerHost;
    }

    public static Integer getRedisServerPort() {
        return redisServerPort;
    }

    public static String getRedisServerAuth() {
        return redisServerAuth;
    }

    public static String getRedisServerPwd() {
        return redisServerPwd;
    }

    public static String getRedisServerWay() {
        return redisServerWay;
    }

    public static String getRedisClientWay() {
        return redisClientWay;
    }

    public static String getRedisServerAsync() {
        return redisServerAsync;
    }

    protected static String redisServerAsync;

    private static AbstractRedisConfiguration lRedisConfiguration;
    private static AbstractRedisConfiguration jRedisConfiguration;


    static {
        InputStream is = null;
        PropertyResourceBundle configResource = null;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(PathConstants.ABSOLUTE_CONFIG_PATH))) {

            configResource = new PropertyResourceBundle(bufferedReader);
            redisServerAuth = configResource.getString(PropertiesConstants.REDIS_SERVER_AUTH);
            // 读取到 config 资源配置文件 且 不需要密码验证
            if ("false".equals(redisServerAuth) || "default".equals(redisServerAuth) || StringUtils.isBlank(redisServerAuth)) {
                redisServerAuth = "false";
                log.info("--- no redis auth ---");
                try {
                    redisServerAddr = configResource.getString(PropertiesConstants.REDIS_SERVER_ADDR);
                    String[] hostAndPort = redisServerAddr.split(":");
                    redisServerHost = hostAndPort[0];
                    redisServerPort = Integer.parseInt(hostAndPort[1]);
                    //jedis = new Jedis(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
                } catch (MissingResourceException redisServerAddressException) {
                    String[] hostAndPort = defaultServerAddr.split(":");
                    redisServerHost = hostAndPort[0];
                    redisServerPort = Integer.parseInt(hostAndPort[1]);
                    //jedis = new Jedis(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
                    log.warn("redis server address attribute is missing");
                    log.info("use default redis server address : " + defaultServerAddr);
                }
                // 读取到 config 资源配置文件 且需要 密码验证
            } else if ("true".equals(redisServerAuth)) {
                log.info("redis auth attribute is true and start with auth");
                try {
                    redisServerAddr = configResource.getString(PropertiesConstants.REDIS_SERVER_ADDR);
                } catch (MissingResourceException redisServerAddressException) {
                    log.info("redis server address property attribute is missing: {}", redisServerAddressException.getMessage());
                    log.info("use default redis server address : " + defaultServerAddr);
                    String[] hostAndPort = defaultServerAddr.split(":");
                    redisServerHost = hostAndPort[0];
                    redisServerPort = Integer.parseInt(hostAndPort[1]);
                }
                try {
                    redisServerPwd = configResource.getString(PropertiesConstants.REDIS_SERVER_PWD);
                    String[] hostAndPort = redisServerAddr.split(":");
                    redisServerHost = hostAndPort[0];
                    redisServerPort = Integer.parseInt(hostAndPort[1]);
                } catch (MissingResourceException redisPasswordException) {
                    log.error("redis password attribute is missing: ", redisPasswordException);
                    throw new RuntimeException("redis password attribute is missing!");
                }
                // 读取到 config 资源配置文件 且 密码验证规则 不合法
            } else {
                throw new RuntimeException("redis auth attribute is illegal!");
            }
            // 服务端方式 配置，缺省 约定优于配置
            try {
                redisServerWay = configResource.getString(PropertiesConstants.REDIS_SERVER_WAY);
            } catch (MissingResourceException redisServerWayException) {
                redisServerWay = defaultServerWay;
                log.warn("redis server way attribute is missing");
                log.info("use default redis server way : " + defaultServerAddr);
            }
            //  客户端方式 配置，缺省 约定优于配置
            try {
                redisClientWay = configResource.getString(PropertiesConstants.REDIS_CLIENT_WAY);
            } catch (MissingResourceException redisClientWayException) {
                redisClientWay = defaultClientWay;
                log.warn("redis client way attribute is missing");
                log.info("use default redis client way : " + defaultClientWay);
            }
            //  lettuce 阻塞配置，缺省 约定优于配置
            try {
                redisServerAsync = configResource.getString(PropertiesConstants.REDIS_SERVER_ASYNC);
            } catch (MissingResourceException redisServerAsyncException) {
                redisServerAsync = defaultServerAsync;
                log.warn("redis server async attribute is missing");
                log.info("use default redis server async : " + defaultServerAsync);
            }
            log.info("read resource from resource path: {}", PathConstants.ABSOLUTE_CONFIG_PATH);
            // 读取到 config 资源配置文件 且没有读取到 密码 配置
        } catch (MissingResourceException redisServerAuthException) {
            redisServerAuth = "false";
            log.warn("redis auth attribute is missing and start with no auth");
            // 地址 配置，缺省 约定优于配置
            try {
                String redisServerAddress = configResource.getString(PropertiesConstants.REDIS_SERVER_ADDR);
                redisServerAddr = StringUtils.isBlank(redisServerAddress) ? defaultServerAddr : redisServerAddress;
                String[] hostAndPort = redisServerAddr.split(":");
                redisServerHost = hostAndPort[0];
                redisServerPort = Integer.parseInt(hostAndPort[1]);
            } catch (MissingResourceException redisServerAddressException) {
                String[] hostAndPort = defaultServerAddr.split(":");
                redisServerHost = hostAndPort[0];
                redisServerPort = Integer.parseInt(hostAndPort[1]);
                log.warn("redis server address attribute is missing");
                log.info("use default redis server address : " + defaultServerAddr);
            }
            // 服务端方式 配置，缺省 约定优于配置
            try {
                redisServerWay = configResource.getString(PropertiesConstants.REDIS_SERVER_WAY);
            } catch (MissingResourceException redisServerWayException) {
                redisServerWay = defaultServerWay;
                log.warn("redis server way attribute is missing");
                log.info("use default redis server way : " + defaultServerAddr);
            }
            //  客户端方式 配置，缺省 约定优于配置
            try {
                redisClientWay = configResource.getString(PropertiesConstants.REDIS_CLIENT_WAY);
            } catch (MissingResourceException redisClientWayException) {
                redisClientWay = defaultClientWay;
                log.warn("redis client way attribute is missing");
                log.info("use default redis client way : " + defaultClientWay);
            }
            //  lettuce 阻塞配置，缺省 约定优于配置
            try {
                redisServerAsync = configResource.getString(PropertiesConstants.REDIS_SERVER_ASYNC);
            } catch (MissingResourceException redisServerAsyncException) {
                redisServerAsync = defaultServerAsync;
                log.warn("redis server async attribute is missing");
                log.info("use default redis server async : " + defaultServerAsync);
            }
            // 没有读取到 config 资源配置文件
        } catch (IOException ioException) {
            log.info("not found resource from resource path: {}", PathConstants.ABSOLUTE_CONFIG_PATH);
            try {
                ResourceBundle resource = ResourceBundle.getBundle(PathConstants.RESOURCE_CONFIG_PATH);

                try {
                    redisServerAuth = resource.getString(PropertiesConstants.REDIS_SERVER_AUTH);
                    // 读取到 resource 资源配置文件 且 无 密码验证
                    if ("false".equals(redisServerAuth) || "default".equals(redisServerAuth) || StringUtils.isBlank(redisServerAuth)) {
                        redisServerAuth = "false";
                        log.info("--- no redis auth ---");
                        // 地址 配置，缺省 约定优于配置
                        try {
                            redisServerAddr = resource.getString(PropertiesConstants.REDIS_SERVER_ADDR);
                            String[] hostAndPort = redisServerAddr.split(":");
                            redisServerHost = hostAndPort[0];
                            redisServerPort = Integer.parseInt(hostAndPort[1]);
                        } catch (MissingResourceException redisServerAddressException) {
                            String[] hostAndPort = defaultServerAddr.split(":");
                            redisServerHost = hostAndPort[0];
                            redisServerPort = Integer.parseInt(hostAndPort[1]);
                            log.warn("redis server address attribute is missing");
                            log.info("use default redis server address : " + defaultServerAddr);
                        }
                        // 读取到 resource 资源配置文件 且 有 密码验证
                    } else if ("true".equals(redisServerAuth)) {
                        log.info("redis auth attribute is true and start with auth");
                        // 地址 配置，缺省 约定优于配置
                        try {
                            redisServerAddr = resource.getString(PropertiesConstants.REDIS_SERVER_ADDR);
                        } catch (MissingResourceException redisServerAddressException) {
                            log.info("redis server address property attribute is missing: {}", redisServerAddressException.getMessage());
                            log.info("use default redis server address : " + defaultServerAddr);
                            String[] hostAndPort = defaultServerAddr.split(":");
                            redisServerHost = hostAndPort[0];
                            redisServerPort = Integer.parseInt(hostAndPort[1]);
                        }
                        // 密码 配置，缺省 抛异常
                        try {
                            redisServerPwd = resource.getString(PropertiesConstants.REDIS_SERVER_PWD);
                            String[] hostAndPort = redisServerAddr.split(":");
                            redisServerHost = hostAndPort[0];
                            redisServerPort = Integer.parseInt(hostAndPort[1]);
                        } catch (MissingResourceException redisPasswordException) {
                            log.error("redis password attribute is missing: ", redisPasswordException);
                            throw new RuntimeException("redis password attribute is missing!");
                        }
                        // 读取到 config 资源配置文件 且 密码验证规则 不合法
                    } else {
                        throw new RuntimeException("redis auth attribute is illegal!");
                    }
                    // 读取到 resource 资源配置文件 且没有读取到 密码 配置
                } catch (MissingResourceException redisServerAuthException) {
                    redisServerAuth = "false";
                    log.warn("redis auth attribute is missing and start with no auth");
                    // 地址 配置，缺省 约定优于配置
                    try {
                        String redisServerAddress = configResource.getString(PropertiesConstants.REDIS_SERVER_ADDR);
                        redisServerAddr = StringUtils.isBlank(redisServerAddress) ? defaultServerAddr : redisServerAddress;
                        String[] hostAndPort = redisServerAddr.split(":");
                        redisServerHost = hostAndPort[0];
                        redisServerPort = Integer.parseInt(hostAndPort[1]);
                    } catch (MissingResourceException redisServerAddressException) {
                        String[] hostAndPort = defaultServerAddr.split(":");
                        redisServerHost = hostAndPort[0];
                        redisServerPort = Integer.parseInt(hostAndPort[1]);
                        //jedis = new Jedis(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
                        log.warn("redis server address attribute is missing");
                        log.info("use default redis server address : " + defaultServerAddr);
                    }
                }
                // 服务端方式 配置，缺省 约定优于配置
                try {
                    redisServerWay = configResource.getString(PropertiesConstants.REDIS_SERVER_WAY);
                } catch (MissingResourceException redisServerWayException) {
                    redisServerWay = defaultServerWay;
                    log.warn("redis server way attribute is missing");
                    log.info("use default redis server way : " + defaultServerAddr);
                }
                //  客户端方式 配置，缺省 约定优于配置
                try {
                    redisClientWay = configResource.getString(PropertiesConstants.REDIS_CLIENT_WAY);
                } catch (MissingResourceException redisClientWayException) {
                    redisClientWay = defaultClientWay;
                    log.warn("redis client way attribute is missing");
                    log.info("use default redis client way : " + defaultClientWay);
                }
                //  lettuce 阻塞配置，缺省 约定优于配置
                try {
                    redisServerAsync = configResource.getString(PropertiesConstants.REDIS_SERVER_ASYNC);
                } catch (MissingResourceException redisServerAsyncException) {
                    redisServerAsync = defaultServerAsync;
                    log.warn("redis server async attribute is missing");
                    log.info("use default redis server async : " + defaultServerAsync);
                }
                // 找不到所有资源，采用默认配置
            } catch (MissingResourceException resourceException) {
                log.info("not found resource from resource path: {}", "resource.properties");
                log.info("Connect to default address {}", defaultServerAddr);
                String[] hostAndPort = defaultServerAddr.split(":");
                redisServerHost = hostAndPort[0];
                redisServerPort = Integer.parseInt(hostAndPort[1]);
                //  服务端方式 配置，缺省 约定优于配置
                redisServerWay = defaultServerWay;
                log.info("use default redis server way : " + defaultServerAddr);
                //  客户端方式 配置，缺省 约定优于配置
                redisClientWay = defaultClientWay;
                log.info("use default redis client way : " + defaultClientWay);
                //  lettuce 阻塞配置，缺省 约定优于配置
                redisServerAsync = defaultServerAsync;
                log.info("use default redis server async : " + defaultServerAsync);
            }
            log.info("read resource from resource path: {}", "resource.properties");
        }
        log.info("------------ redis Configuration 【 begin 】 ------------");
        log.info("redisServerHost: [{}]", redisServerHost);
        log.info("redisServerPort: [{}]", redisServerPort);
        log.info("redisServerAuth: [{}]", redisServerAuth);
        log.info("redisServerPwd: [{}]", redisServerPwd);
        log.info("redisServerWay: [{}]", redisServerWay);
        log.info("redisClientWay: [{}]", redisClientWay);
        log.info("redisServerAsync: [{}]", redisServerAsync);
        log.info("------------ redis Configuration 【 end 】 ------------");
    }
    
    public static AbstractRedisConfiguration getServerConfig() {
        switch (redisClientWay) {
            case "jedis": {
                if (jRedisConfiguration == null)
                    jRedisConfiguration = SingleFactory.getInstance(JRedisConfiguration.class).configure();
                return jRedisConfiguration;
            }
            case "lettuce": {
                if (lRedisConfiguration == null)
                    lRedisConfiguration = SingleFactory.getInstance(LRedisConfiguration.class).configure();
                return lRedisConfiguration;
            }
            default: {
                if (jRedisConfiguration == null)
                    jRedisConfiguration = SingleFactory.getInstance(JRedisConfiguration.class).configure();
                return jRedisConfiguration;
            }
        }
    }

    public static AbstractRedisConfiguration getClientConfig() {
        switch (redisClientWay) {
            case "jedis": {
                if (jRedisConfiguration == null)
                    jRedisConfiguration = SingleFactory.getInstance(JRedisConfiguration.class).configure();
                return jRedisConfiguration;
            }
            case "lettuce": {
                if (lRedisConfiguration == null)
                    lRedisConfiguration = SingleFactory.getInstance(LRedisConfiguration.class).configure();
                return lRedisConfiguration;
            }
            default: {
                if (jRedisConfiguration == null)
                    jRedisConfiguration = SingleFactory.getInstance(JRedisConfiguration.class).configure();
                return jRedisConfiguration;
            }
        }
    }
    
    public abstract AbstractRedisConfiguration configure();

    public abstract boolean exists(String key);

    public abstract void set(String key, String value);

    public abstract String get(String key);

    public abstract boolean existsWorkerId(String hostName) ;

    public abstract String getWorkerIdForHostName(String hostName) ;

    public abstract void setWorkerId(String hostName, long workerId) ;

    public abstract void remWorkerId(String hostName);

    public abstract void asyncRemWorkerId(String hostName);

    public abstract boolean existsWorkerIdSet(long workerId);

    public abstract void setWorkerIdSet(long workerId) ;

    public abstract boolean existsRetryResult(String retryRequestId) ;

    public abstract String getResultForRetryRequestId2String(String retryRequestId);

    public abstract byte[] getResultForRetryRequestId2Bytes(String retryRequestId);

    public abstract void setRetryRequestResultByString(String retryRequestId, String result);

    public abstract void setRetryRequestResultByBytes(String retryRequestId, byte[] result);

    public abstract void asyncSet(String key, String value) ;
    public abstract void asyncSetWorkerIdSet(long workerId) ;

    public abstract void asyncSetWorkerId(String hostName, long workerId) ;

    public abstract void asyncSetRetryRequestResult(String retryRequestId, byte[] result) ;

}
