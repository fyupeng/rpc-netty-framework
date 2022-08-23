package cn.fyupeng.util;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.sun.media.jfxmedia.events.NewFrameEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
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
        //2.1 创建Properties对象
        Properties p = new Properties();
        //2.2 调用p对象中的load方法进行配置文件的加载
        // 使用InPutStream流读取properties文件
        String currentWorkPath = System.getProperty("user.dir");
        InputStream is = null;
        String propertyValue = "";
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(currentWorkPath + "/config/resource.properties"));) {
            p.load(bufferedReader);
            propertyValue = p.getProperty("cn.fyupeng.nacos.register-addr");
            log.info("read resource from resource path: {}", currentWorkPath + "/config/resource.properties");
        } catch (IOException e) {
            log.info("not found resource from resource path: {}", currentWorkPath + "/config/resource.properties");
            is = NacosUtils.class.getClassLoader().getResourceAsStream("resource.properties");
            if(is != null) {
                log.info("read resource from resource path: {}", NacosUtils.class.getClassLoader().getResource("resource.properties").getPath());
                try {
                    p.load(is);
                    // 自定义 指定的注册中心地址，将覆盖默认地址
                    propertyValue = p.getProperty("cn.fyupeng.nacos.register-addr");
                } catch (IOException ex) {
                    log.error("load resource error: ", ex);
                }
            } else {
                log.info("Register center bind with default address {}", SERVER_ADDR);
            }
        }
        int pre = -1;
        String host = "";
        Integer port = 0;
        if ((pre = propertyValue.indexOf(":")) > 0 && pre == propertyValue.lastIndexOf(":")) {
            boolean valid = IpUtils.valid(host = propertyValue.substring(0, pre));
            if (valid) {
                host = propertyValue.substring(0, pre);
                port = Integer.parseInt(propertyValue.substring(pre + 1));
                if (host.equals("localhost")) {
                    SERVER_ADDR = "127.0.0.1:" + port;
                } else {
                    SERVER_ADDR = propertyValue;
                }
                log.info("Register center bind with address {}", propertyValue);
            } else {
                log.error("wrong ip address: {}", propertyValue);
            }
        } else if (!propertyValue.equals("")) {
            log.error("wrong ip address: {}", propertyValue);
        }
        // 初始化 Nacos 注册中心服务接口
        namingService = getNacosNamingService();
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
        log.info("host: {} has been registered on Register Center", address.getHostName());
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
