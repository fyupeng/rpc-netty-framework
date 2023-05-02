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
    @Override
    public String sayHello(String message) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "test say hello" + message;
    }
}
