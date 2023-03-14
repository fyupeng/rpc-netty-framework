package cn.fyupeng;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * @Auther: fyp
 * @Date: 2023/3/1
 * @Description:
 * @Package: cn.fyupeng
 * @Version: 1.0
 */
public class TestSerivceRegistry {

      public static void main(String[] args) throws NacosException {
         NamingService namingService = NamingFactory.createNamingService("****:***");
         namingService.registerInstance("testService", "localhost", 8081);
         List<Instance> testService = namingService.getAllInstances("testService");
         testService.forEach(enter -> {
            System.out.println(enter);
         });
      }

}
