package com.zhkucst.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zhkucst.exception.ObtainServiceException;
import com.zhkucst.exception.RegisterFailedException;
import com.zhkucst.exception.RpcException;
import com.zhkucst.util.NacosUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @Auther: fyp
 * @Date: 2022/3/26
 * @Description:
 * @Package: com.zhkucst.registry
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
