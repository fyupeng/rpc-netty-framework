package cn.fyupeng.registry;

import cn.fyupeng.config.NacosConfiguration;
import com.alibaba.nacos.api.exception.NacosException;
import cn.fyupeng.exception.RegisterFailedException;
import cn.fyupeng.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @Auther: fyp
 * @Date: 2022/3/26
 * @Description:
 * @Package: cn.fyupeng.registry
 * @Version: 1.0
 */
@Slf4j
public class NacosServiceRegistry implements ServiceRegistry {

   /**
    * 以默认组名”DEFAULT_GROUP“, 注册服务名对应的服务套接字地址
    * @param serviceName 服务名
    * @param inetSocketAddress 套接字地址
    * @throws RpcException
    */
   @Override
   public void register(String serviceName, InetSocketAddress inetSocketAddress) throws RpcException {
      try {
         NacosConfiguration.registerService(serviceName, inetSocketAddress);
      } catch (NacosException e) {
         log.error("Failed to register service", e.getMessage());
         throw new RegisterFailedException("Failed to register service Exception");
      }
   }

   /**
    * 注册组名下服务名对应的服务套接字地址
    * @param serviceName 服务名
    * @param groupName 组名
    * @param inetSocketAddress 套接字地址
    * @throws RpcException
    */
   @Override
   public void register(String serviceName, String groupName, InetSocketAddress inetSocketAddress) throws RpcException {
      try {
         NacosConfiguration.registerService(serviceName, groupName, inetSocketAddress);
      } catch (NacosException e) {
         log.error("Failed to register service", e.getMessage());
         throw new RegisterFailedException("Failed to register service Exception");
      }
   }

   /**
    * 服务关闭时自动调用，只需要实现该功能即可
    */
   public void clearRegistry() {
      NacosConfiguration.clearRegistry();
   }

}
