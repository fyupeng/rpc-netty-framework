package cn.fyupeng;

import cn.fyupeng.anotion.Reference;
import cn.fyupeng.loadbalancer.RandomLoadBalancer;
import cn.fyupeng.pojo.BlogJSONResult;
import cn.fyupeng.net.netty.client.NettyClient;
import cn.fyupeng.proxy.RpcClientProxy;
import cn.fyupeng.serializer.CommonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.awt.print.PrinterAbortException;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Auther: fyp
 * @Date: 2022/8/14
 * @Description:
 * @Package: PACKAGE_NAME
 * @Version: 1.0
 */

@Slf4j
public class Client {
    private static RandomLoadBalancer randomLoadBalancer = new RandomLoadBalancer();
    private static NettyClient nettyClient = new NettyClient(randomLoadBalancer, CommonSerializer.KRYO_SERIALIZER);
    private static RpcClientProxy rpcClientProxy = new RpcClientProxy(nettyClient);

    @Reference(retries = 5, timeout = 600, asyncTime = 3000)
    private static HelloWorldService service = rpcClientProxy.getProxy(HelloWorldService.class, Client.class);

    private static AtomicLong res = new AtomicLong(0L);

    public static void main(String[] args) throws InterruptedException {

        Thread[] threads = new Thread[12];

        for (int i = 0; i < 12; i++) {
            Thread t = new Thread(() -> {
                long mainStart = System.currentTimeMillis();
                for (int j = 0; j < 20; j++) {
                    BlogJSONResult result1 = service.sayHello("rpc-netty-framework -- cn.fyupeng");
                }
                long mainEnd = System.currentTimeMillis();
                log.info("耗时为：{}", mainEnd - mainStart);
                //res += mainEnd - mainStart;
                while (!res.compareAndSet(res.get(), res.get() + mainEnd - mainStart)) {
                }
            }, "t" + i);
            t.start();
            threads[i] = t;
        }

        for (int i = 0; i < 12; i++) {
            threads[i].join();
        }

        System.out.println("总耗时为" + res);
    }
}
