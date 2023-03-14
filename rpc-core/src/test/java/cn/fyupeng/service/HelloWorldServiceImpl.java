package cn.fyupeng.service;

import cn.fyupeng.HelloWorldService;
import cn.fyupeng.pojo.BlogJSONResult;
import cn.fyupeng.annotation.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Auther: fyp
 * @Date: 2022/8/14
 * @Description:
 * @Package: PACKAGE_NAME
 * @Version: 1.0
 */
@Service(name = "helloService", group = "1.0.0")
public class HelloWorldServiceImpl implements HelloWorldService {
    private AtomicInteger count = new AtomicInteger(0);
    @Override
    public BlogJSONResult sayHello(String message) {
        try {
            //int taskTime = new Random().nextInt(50);
            int taskTime = 50;
            Thread.sleep(taskTime);
            System.out.println("任务时间： " + taskTime);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("调用次数" + count.incrementAndGet());
        System.out.println("服务端接收到数据： " + message);
        return BlogJSONResult.ok("服务端响应数据： " + message + " 已经被我们收到了");
    }
}
