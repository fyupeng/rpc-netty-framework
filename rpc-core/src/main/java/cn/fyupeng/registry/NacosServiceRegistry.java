package cn.fyupeng.registry;

import com.alibaba.nacos.api.exception.NacosException;
import cn.fyupeng.exception.RegisterFailedException;
import cn.fyupeng.exception.RpcException;
import cn.fyupeng.util.NacosUtils;
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

   @Override
   public void register(String serviceName, InetSocketAddress inetSocketAddress) throws RpcException {
      try {
         NacosUtils.registerService(serviceName, inetSocketAddress);
      } catch (NacosException e) {
         log.error("Failed to register service", e.getMessage());
         throw new RegisterFailedException("Failed to register service Exception");
      }
   }

}
