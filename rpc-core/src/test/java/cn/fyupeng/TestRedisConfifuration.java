package cn.fyupeng;

import cn.fyupeng.config.AbstractRedisConfiguration;
import cn.fyupeng.config.Configuration;

/**
 * @Auther: fyp
 * @Date: 2023/3/17
 * @Description:
 * @Package: cn.fyupeng
 * @Version: 1.0
 */
public class TestRedisConfifuration{
   public static void main(String[] args) {
      AbstractRedisConfiguration clientConfig = AbstractRedisConfiguration.getClientConfig();
      String res = clientConfig.get("1231");
      System.out.println(res);


   }

}
