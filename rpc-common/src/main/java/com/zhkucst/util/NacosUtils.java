package com.zhkucst.util;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zhkucst.exception.ConnectFailedException;
import lombok.extern.slf4j.Slf4j;

import javax.naming.NamingException;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * @Auther: fyp
 * @Date: 2022/3/26
 * @Description:
 * @Package: com.zhkucst.util
 * @Version: 1.0
 */
@Slf4j
public class NacosUtils {

    private static final String SERVER_ADDR = "127.0.0.1:8848";
    private static final NamingService namingService;
    private static final Set<String> serviceNames = new HashSet<>();
    private static InetSocketAddress inetSocketAddress;

    static {
        namingService = getNacosNamingService();
    }

    public static NamingService getNacosNamingService() {
        try {
            return NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            log.error("error occurred when connecting to nacos server: ",e);
            return null;
        }
    }

    public static List<Instance> getAllInstance(String serviceName) throws  NacosException {
        return namingService.getAllInstances(serviceName);
    }

    public static void registerService(String serviceName, InetSocketAddress address) throws NacosException {
        namingService.registerInstance(serviceName, address.getHostName(), address.getPort());
        inetSocketAddress = address;
        serviceNames.add(serviceName);
    }

    public static void clearRegistry() {
        if (!serviceNames.isEmpty() && inetSocketAddress != null) {
            String hostname = inetSocketAddress.getHostName();
            int port = inetSocketAddress.getPort();
            Iterator<String> iterator = serviceNames.iterator();
            while (iterator.hasNext()) {
                String serviceName = iterator.next();
                try {
                    namingService.deregisterInstance(serviceName, hostname,  port);
                } catch (NacosException e) {
                    log.error("Failed to cancel service:{}, info:{}",serviceName, e);
                }
            }
            log.info("All services on the nacos service have been cleaned up successfully");
        }
    }


}
