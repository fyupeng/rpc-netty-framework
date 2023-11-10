package cn.fyupeng;

import cn.fyupeng.annotation.Reference;
import cn.fyupeng.factory.ThreadPoolFactory;
import cn.fyupeng.loadbalancer.RoundRobinLoadBalancer;
import cn.fyupeng.net.netty.client.NettyClient;
import cn.fyupeng.proxy.RpcClientProxy;
import cn.fyupeng.serializer.CommonSerializer;
import cn.fyupeng.service.HelloWorldService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

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
    //private static NettyClient nettyClient = new NettyClient(randomLoadBalancer, CommonSerializer.HESSIAN_SERIALIZER);
    private static NettyClient nettyClient = new NettyClient("192.168.123.191", 9527, CommonSerializer.CJSON_SERIALIZER);
    private static RpcClientProxy rpcClientProxy = new RpcClientProxy(nettyClient);
    @Reference(group = "1.0.1",timeout = 10000, asyncTime = -1)
    private static HelloWorldService service = rpcClientProxy.getProxy(HelloWorldService.class, Client.class);
    //private static HelloWorldService service = rpcClientProxy.getJavassistProxy(HelloWorldService.class, Client.class);

    private static ExecutorService pool = ThreadPoolFactory.createThreadPool(ThreadPoolFactory.CACHE_THREAD_POOL, "test_pool", 0);


    public static void main(String[] args) throws InterruptedException {


        //long begin = System.currentTimeMillis();
        service.sayHello("this java request");
        for (int i = 0; i < 1; i++) {
            //pool.execute(() -> {
            Thread.sleep(2000);
               service.sayHello("rpc-netty-framework -- cn.fyupeng");
             //});
        }
        //long end = System.currentTimeMillis();
        //log.info("run times: {}", end - begin);

    }
}
