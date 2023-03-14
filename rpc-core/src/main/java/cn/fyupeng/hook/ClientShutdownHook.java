package cn.fyupeng.hook;

import cn.fyupeng.net.RpcClient;
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
    private RpcClient rpcClient;

    public static ClientShutdownHook getShutdownHook() {
        return shutdownHook;
    }

    public ClientShutdownHook addClient(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
        return this;
    }

    /**
     * 添加清除钩子
     */
    public void addClearAllHook() {
        log.info("All services will be cancel after shutdown");
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            //NettyChannelProvider.shutdownAll();
            if(rpcClient != null) {
                rpcClient.shutdown();
            }
        }));
    }
}

