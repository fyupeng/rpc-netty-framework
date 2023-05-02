package cn.fyupeng.net.netty.server;

import cn.fyupeng.config.AbstractRedisConfiguration;
import cn.fyupeng.factory.ThreadPoolFactory;
import cn.fyupeng.handler.JdkRequestHandler;
import cn.fyupeng.protocol.RpcRequest;
import cn.fyupeng.protocol.RpcResponse;
import cn.fyupeng.serializer.CommonSerializer;
import cn.fyupeng.util.JsonUtils;
import cn.fyupeng.constant.PropertiesConstants;
import com.alibaba.nacos.common.utils.StringUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @Auther: fyp
 * @Date: 2023/1/7
 * @Description: Netty Channel分发器
 * @Package: cn.fyupeng.net.netty.server
 * @Version: 1.0
 */
@Slf4j
public class NettyChannelDispatcher {

    private static ExecutorService operationExecutorService = ThreadPoolFactory.createThreadPool(ThreadPoolFactory.CACHE_THREAD_POOL, "operation-executor-pool", 0);
    /**
     * Lettuce 分布式缓存采用 HESSIAN 序列化方式
     */
    private static CommonSerializer serializer = CommonSerializer.getByCode(CommonSerializer.HESSIAN_SERIALIZER);
    /**
     * 请求处理器
     */
    private static JdkRequestHandler requestHandler;
    /**
     * redisServerWay: 超时重试 Redis 服务端 api 方式
     * redisServerAsync: 超时重试 Redis 服务端 异步开关
     */
    private static String redisServerWay = "";
    private static String redisServerAsync = "";

    static {
        // 使用InPutStream流读取properties文件
        String currentWorkPath = System.getProperty("user.dir");
        PropertyResourceBundle configResource = null;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(currentWorkPath + "/config/resource.properties"));) {

            configResource = new PropertyResourceBundle(bufferedReader);
            redisServerWay = configResource.getString(PropertiesConstants.REDIS_SERVER_WAY);

            if ("jedis".equals(redisServerWay) || "default".equals(redisServerWay) || StringUtils.isBlank(redisServerWay)) {
                log.info("find redis client way attribute is jedis");
            } else if ("lettuce".equals(redisServerWay)) {
                log.info("find redis client way attribute is lettuce");
                try {
                    redisServerAsync = configResource.getString(PropertiesConstants.REDIS_SERVER_ASYNC);

                    if ("false".equals(redisServerAsync) || "default".equals(redisServerAsync) || StringUtils.isBlank(redisServerAsync)) {
                        log.info("find redis server async attribute is false");
                    } else if ("true".equals(redisServerAsync)) {
                        log.info("find redis server async attribute is lettuce");
                    } else {
                        throw new RuntimeException("redis server async attribute is illegal!");
                    }

                } catch (MissingResourceException redisServerAsyncException) {
                    log.warn("redis server async attribute is missing");
                    log.info("use default redis server default async: false");
                    redisServerAsync = "false";
                }
            } else {
                throw new RuntimeException("redis server async attribute is illegal!");
            }

        } catch (MissingResourceException redisServerWayException) {
            log.warn("redis client way attribute is missing");
            log.info("use default redis client default way: jedis");
            redisServerWay = "jedis";
        } catch (IOException ioException) {
            log.info("not found resource from resource path: {}", currentWorkPath + "/config/resource.properties");
            try {
                ResourceBundle resource = ResourceBundle.getBundle("resource");
                redisServerWay = resource.getString(PropertiesConstants.REDIS_SERVER_WAY);
                if ("jedis".equals(redisServerWay) || "default".equals(redisServerWay) || StringUtils.isBlank(redisServerWay)) {
                    log.info("find redis server way attribute is jedis");
                } else if ("lettuce".equals(redisServerWay)) {
                    log.info("find redis server way attribute is lettuce");
                    try {
                        redisServerAsync = resource.getString(PropertiesConstants.REDIS_SERVER_ASYNC);

                        if ("false".equals(redisServerAsync) || "default".equals(redisServerAsync) || StringUtils.isBlank(redisServerAsync)) {
                            log.info("find redis server async attribute is false");
                        } else if ("true".equals(redisServerAsync)) {
                            log.info("find redis server async attribute is lettuce");
                        } else {
                            throw new RuntimeException("redis server async attribute is illegal!");
                        }

                    } catch (MissingResourceException redisServerAsyncException) {
                        log.warn("redis server async attribute is missing");
                        log.info("use default redis server default async: false");
                        redisServerAsync = "false";
                    }
                } else {
                    throw new RuntimeException("redis client way attribute is illegal!");
                }

            } catch (MissingResourceException resourceException) {
                log.info("not found resource from resource path: {}", "resource.properties");
                log.info("use default redis server way: jedis");
                redisServerWay = "jedis";
            }
            log.info("read resource from resource path: {}", "resource.properties");

        }
        requestHandler = new JdkRequestHandler();
    }

    public static void init() {
        log.info("netty channel dispatcher initialize successfully!");
    }


    public static void dispatch(ChannelHandlerContext ctx, RpcRequest msg) {
        operationExecutorService.submit(() -> {
            log.info("server has received request package: {}", msg);
            // 到了这一步，如果请求包在上一次已经被 服务器成功执行，接下来要做幂等性处理，也就是客户端设置超时重试处理
            /**
             * 改良 2023.1.9
             * 使用 Redis 实现分布式缓存
             * 改良 2023.3.19
             * 抛弃 Redis 检验 重发包
             * 采用 客户端  请求包 标志位，减少一次 判断 Redis IO 操作
             */
            Object result = null;
            AbstractRedisConfiguration redisServerConfig = AbstractRedisConfiguration.getServerConfig();
            //if (!redisServerConfig.existsRetryResult(msg.getRequestId())) {
            if (!msg.getReSend()) {
                log.info("[requestId: {}, reSend: {}] does not exist, store the result in the distributed cache", msg.getRequestId(), msg.getReSend());
                result = requestHandler.handler(msg);
                if (result != null) {
                    writeResultToChannel(ctx, msg, result);

                    String redisServerWay = AbstractRedisConfiguration.getRedisServerWay();
                    if ("jedis".equals(redisServerWay))
                        redisServerConfig.setRetryRequestResultByString(msg.getRequestId(), JsonUtils.objectToJson(result));
                    else {
                        String redisServerAsync = AbstractRedisConfiguration.getRedisServerAsync();
                        if ("true".equals(redisServerAsync)) {
                            redisServerConfig.asyncSetRetryRequestResult(msg.getRequestId(), serializer.serialize(result));
                        } else {
                            redisServerConfig.setRetryRequestResultByBytes(msg.getRequestId(), serializer.serialize(result));
                        }
                    }
                } else {
                    String redisServerAsync = AbstractRedisConfiguration.getRedisServerAsync();
                    if ("true".equals(redisServerAsync)) {
                        redisServerConfig.asyncSetRetryRequestResult(msg.getRequestId(), null);
                    } else {
                        redisServerConfig.setRetryRequestResultByBytes(msg.getRequestId(), null);
                    }
                }
            } else {
                String redisServerWay = AbstractRedisConfiguration.getRedisServerWay();
                if ("jedis".equals(redisServerWay)) {
                    result = redisServerConfig.getResultForRetryRequestId2String(msg.getRequestId());
                    if (result != null) {
                        result = JsonUtils.jsonToPojo((String) result,  msg.getReturnType());
                    }
                } else {
                    result = redisServerConfig.getResultForRetryRequestId2Bytes(msg.getRequestId());
                    if (result != null) {
                        result = serializer.deserialize((byte[]) result, msg.getReturnType());
                    }
                }
            }
            log.debug("Previous results:{} ", result);
            log.info(" >>> Capture reSend package [requestId: {} [method: {}, returnType: {}] <<< ", msg.getRequestId(), msg.getMethodName(), msg.getReturnType());

            writeResultToChannel(ctx, msg, result);
        });
    }

    private static void writeResultToChannel(ChannelHandlerContext ctx, RpcRequest msg, Object result) {
        // 生成 校验码，客户端收到后 会 对 数据包 进行校验
        if (ctx.channel().isActive() && ctx.channel().isWritable()) {
            /**
             * 这里要分两种情况：
             * 1. 当数据无返回值时，保证 checkCode 与 result 可以检验，客户端 也要判断 result 为 null 时 checkCode 是否也为 null，才能认为非他人修改
             * 2. 当数据有返回值时，校验 checkCode 与 result 的 md5 码 是否相同
             */
            String checkCode = "";
            // 这里做了 当 data为 null checkCode 为 null，checkCode可作为 客户端的判断 返回值 依据
            if(result != null) {
                try {
                    checkCode = new String(DigestUtils.md5(result.toString().getBytes("UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    log.error("binary stream conversion failure: ", e);
                    //e.printStackTrace();
                }
            } else {
                checkCode = null;
            }
            RpcResponse rpcResponse = RpcResponse.success(result, msg.getRequestId(), checkCode);
            log.info(String.format("server send back response package {requestId: %s, message: %s, statusCode: %s ]}", rpcResponse.getRequestId(), rpcResponse.getMessage(), rpcResponse.getStatusCode()));
            ChannelFuture future = ctx.writeAndFlush(rpcResponse);


        } else {
            log.info("channel status [active: {}, writable: {}]", ctx.channel().isActive(), ctx.channel().isWritable() );
            log.error("channel is not writable");
        }
    }

    public static void shutdownAll() {
        ThreadPoolFactory.shutdownAll();
    }

}
