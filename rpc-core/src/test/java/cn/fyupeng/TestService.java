package cn.fyupeng;

import cn.fyupeng.protocol.RpcRequest;
import cn.fyupeng.serializer.JsonSerializer;
import cn.fyupeng.service.HelloWorldService;
import cn.fyupeng.service.HelloWorldServiceImpl;

/**
 * @Auther: fyp
 * @Date: 2023/3/27
 * @Description:
 * @Package: cn.fyupeng
 * @Version: 1.0
 */
public class TestService {
   public static void main(String[] args) {
      HelloWorldService helloWorldService = new HelloWorldServiceImpl();
      Class<? extends HelloWorldService> aClass = helloWorldService.getClass();
      System.out.println(aClass);
   }
}
