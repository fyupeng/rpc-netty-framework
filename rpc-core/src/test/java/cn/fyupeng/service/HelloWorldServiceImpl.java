package cn.fyupeng.service;

import cn.fyupeng.annotation.Service;

/**
 * @Auther: fyp
 * @Date: 2022/8/14
 * @Description:
 * @Package: PACKAGE_NAME
 * @Version: 1.0
 */
@Service(group = "1.0.1")
public class HelloWorldServiceImpl implements HelloWorldService {
    @Override
    public String sayHello(String message) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "这是java的sayHello远程服务，返回你发起的消息内容:" + message;
    }
}
