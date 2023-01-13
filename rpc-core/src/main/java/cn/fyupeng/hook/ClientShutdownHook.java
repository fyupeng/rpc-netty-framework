package cn.fyupeng.hook;

import cn.fyupeng.factory.ThreadPoolFactory;
import cn.fyupeng.idworker.utils.JRedisHelper;
import cn.fyupeng.net.netty.client.ChannelProvider;
import cn.fyupeng.net.netty.server.NettyServer;
import cn.fyupeng.util.IpUtils;
import cn.fyupeng.util.NacosUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @Auther: fyp
 * @Date: 2023/1/13
 * @Description: 客户端清除钩子
 * @Package: cn.fyupeng.hook
 * @Version: 1.0
 */

@Slf4j
public class ClientShutdownHook {

    private static final ClientShutdownHook shutdownHook = new ClientShutdownHook();

    public static ClientShutdownHook getShutdownHook() {
        return shutdownHook;
    }

    /**
     * 添加清除钩子
     */
    public void addClearAllHook() {
        log.info("All services will be cancel after shutdown");
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            ChannelProvider.shutdownAll();
            ThreadPoolFactory.shutdownAll();
        }));
    }
}

