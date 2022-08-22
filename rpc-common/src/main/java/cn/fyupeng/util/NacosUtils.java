package cn.fyupeng.util;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * @Auther: fyp
 * @Date: 2022/3/26
 * @Description:
 * @Package: cn.fyupeng.util
 * @Version: 1.0
 */
@Slf4j
public class NacosUtils {

    private static String SERVER_ADDR = "127.0.0.1:8848";
    private static final NamingService namingService;
    private static final Set<String> serviceNames = new HashSet<>();
    private static InetSocketAddress inetSocketAddress;


    //2. 加载配置文件，只需加载一次
    static {
        // 初始化 Nacos 注册中心服务接口
        namingService = getNacosNamingService();
        //2.1 创建Properties对象
        Properties p = new Properties();
        //2.2 调用p对象中的load方法进行配置文件的加载
        try (InputStream is = NacosUtils.class.getClassLoader().getResourceAsStream("resource.properties");) {
            if(is != null) {
                p.load(is);
                // 自定义 指定的注册中心地址，将覆盖默认地址
                String propertyValue = p.getProperty("cn.fyupeng.nacos.register-addr");
                int pre = -1;
                String host = "";
                Integer port = 0;
                if ((pre = propertyValue.indexOf(":")) > 0 && pre == propertyValue.lastIndexOf(":")) {
                    boolean valid = IpValid.valid(host = propertyValue.substring(0, pre));
                    if (valid) {
                        port = Integer.parseInt(propertyValue.substring(pre + 1));
                        SERVER_ADDR = propertyValue;
                    } else {
                        log.error("wrong ip address: {}", propertyValue);
                    }
                } else {
                    log.error("wrong ip address: {}", propertyValue);
                }
                log.info("Register center bind with address {}", propertyValue);
            } else {
                log.info("Register center bind with default address {}", SERVER_ADDR);
            }
        } catch (IOException e) {
            log.error("load resource error: ", e);
        }
    }


    public static NamingService getNacosNamingService() {
        try {
            return NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            log.error("error occurred when connecting to nacos server: ", e);
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
