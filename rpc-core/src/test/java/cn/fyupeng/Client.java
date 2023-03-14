package cn.fyupeng;

import cn.fyupeng.annotation.Reference;
import cn.fyupeng.factory.ThreadPoolFactory;
import cn.fyupeng.loadbalancer.RoundRobinLoadBalancer;
import cn.fyupeng.pojo.BlogJSONResult;
import cn.fyupeng.net.netty.client.NettyClient;
import cn.fyupeng.proxy.RpcClientProxy;
import cn.fyupeng.serializer.CommonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
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
    private static RoundRobinLoadBalancer randomLoadBalancer = new RoundRobinLoadBalancer();
    private static NettyClient nettyClient = new NettyClient(randomLoadBalancer, CommonSerializer.HESSIAN_SERIALIZER);
    private static RpcClientProxy rpcClientProxy = new RpcClientProxy(nettyClient);

    @Reference(name = "helloService", group = "1.0.0", retries = 2, timeout = 2000)
    private static HelloWorldService service = rpcClientProxy.getProxy(HelloWorldService.class, Client.class);

    private static AtomicLong res = new AtomicLong(0L);

    public static void main(String[] args) throws InterruptedException {


        //
        //Thread[] threads = new Thread[1];
        //
        ////for (int i = 0;  i < 1; i++)
        ////    service.sayHello("rpc-netty-framework -- cn.fyupeng");
        //
        //long mainStart = System.currentTimeMillis();
        //for (int i = 0; i < 10; i++) {
        //    Thread t = new Thread(() -> {
        //
        //        for (int j = 0; j < 100; j++) {
        //            BlogJSONResult result1 = service.sayHello("rpc-netty-framework -- cn.fyupeng");
        //        }
        //
        //        //res += mainEnd - mainStart;
        //
        //    }, "t" + i);
        //    t.start();
        //    threads[i] = t;
        //}
        //
        //
        //for (int i = 0; i < 10; i++) {
        //    threads[i].join();
        //}
        //long mainEnd = System.currentTimeMillis();
        //while (!res.compareAndSet(res.get(), res.get() + mainEnd - mainStart)) {
        //}
        //
        //log.info("总耗时为：{}ms", res.get());

        ExecutorService threadPool = ThreadPoolFactory.createDefaultThreadPool("Test-id-pool");

        for (int i = 0; i < 500; i++) {
            threadPool.submit(() -> {
                for (int j = 0; j < 1; j++) {
                    BlogJSONResult result1 = service.sayHello("rpc-netty-framework -- cn.fyupeng");
                }
            });
        }


    }
}
