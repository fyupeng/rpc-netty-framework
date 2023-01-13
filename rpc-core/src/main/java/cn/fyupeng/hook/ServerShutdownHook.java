package cn.fyupeng.hook;


import cn.fyupeng.factory.ThreadPoolFactory;
import cn.fyupeng.idworker.utils.JRedisHelper;
import cn.fyupeng.net.netty.server.NettyServer;
import cn.fyupeng.util.IpUtils;
import cn.fyupeng.util.NacosUtils;
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

    public static ServerShutdownHook getShutdownHook() {
        return shutdownHook;
    }

    /**
     * 添加清除钩子
     */
    public void addClearAllHook() {
        log.info("All services will be cancel after shutdown");
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            JRedisHelper.remWorkerId(IpUtils.getPubIpAddr());
            log.info("the cache for workId has bean cleared successfully");
            NacosUtils.clearRegistry();
            NettyServer.shutdownAll();
            ThreadPoolFactory.shutdownAll();
        }));
    }


}
