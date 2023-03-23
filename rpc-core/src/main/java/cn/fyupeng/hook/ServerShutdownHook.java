package cn.fyupeng.hook;

import cn.fyupeng.config.AbstractRedisConfiguration;
import cn.fyupeng.net.RpcServer;
import cn.fyupeng.registry.ServiceRegistry;
import cn.fyupeng.util.IpUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @Auther: fyp
 * @Date: 2022/3/26
 * @Description:
 * @Package: cn.fyupeng.hook
 * @Version: 1.0
 */
@Slf4j
public class ServerShutdownHook {

    private static final ServerShutdownHook shutdownHook = new ServerShutdownHook();
    private ServiceRegistry serviceRegistry;
    private RpcServer rpcServer;

    public ServerShutdownHook addRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        return this;
    }

    public ServerShutdownHook addServer(RpcServer rpcServer) {
        this.rpcServer = rpcServer;
        return this;
    }

    public static ServerShutdownHook getShutdownHook() {
        return shutdownHook;
    }
    /**
     * 添加清除钩子
     * 开启 子线程的方式 帮助 gc
     */
    public void addClearAllHook() {
        log.info("All services will be cancel after shutdown");
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            AbstractRedisConfiguration redisServerConfig = AbstractRedisConfiguration.getServerConfig();
            redisServerConfig.remWorkerId(IpUtils.getPubIpAddr());
            log.info("the cache for workId has bean cleared successfully");
            //NacosUtils.clearRegistry();
            if (serviceRegistry != null) {
                serviceRegistry.clearRegistry();
            }
            //NettyServer.shutdownAll();
            // 开启子线程（非守护线程） 的方式能够 避免因服务器 关闭导致 关闭钩子 未能正常执行完毕（守护线程）
            if(rpcServer != null) {
                rpcServer.shutdown();
            }
        }));
    }
}
