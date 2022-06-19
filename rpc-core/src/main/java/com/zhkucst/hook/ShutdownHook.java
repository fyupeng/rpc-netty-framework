package com.zhkucst.hook;


import com.zhkucst.factory.ThreadPoolFactory;
import com.zhkucst.util.NacosUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @Auther: fyp
 * @Date: 2022/3/26
 * @Description:
 * @Package: com.zhkucst.hook
 * @Version: 1.0
 */
@Slf4j
public class ShutdownHook {

    private static final ShutdownHook shutdownHook = new ShutdownHook();

    public static ShutdownHook getShutdownHook() {
        return shutdownHook;
    }

    public void addClearAllHook() {
        log.info("All services will be cancel after shutdown");
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            NacosUtils.clearRegistry();
            ThreadPoolFactory.shutdownAll();
        }));
    }


}
