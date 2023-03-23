package cn.fyupeng.config;

import cn.fyupeng.enums.LoadBalancerCode;
import cn.fyupeng.exception.RpcException;
import cn.fyupeng.loadbalancer.LoadBalancer;
import cn.fyupeng.util.IpUtils;
import cn.fyupeng.constant.PropertiesConstants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.utils.StringUtils;
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
public class NacosConfiguration implements Configuration {

    private static final Set<String> serviceNames = new HashSet<>();
    private static LoadBalancer loadBalancer;
    private static String nacosServerAddr = "127.0.0.1:8848";
    private static NamingService namingService;
    private static InetSocketAddress inetSocketAddress;
    private static String nacosUseCluster = "";
    // 增加 容灾切换
    private static String[] nacosNodes = null;
    private static String nacosBalancer = "round";


    //2. 加载配置文件，只需加载一次
    static {
        //2.1 创建Properties对象
        Properties p = new Properties();
        //2.2 调用p对象中的load方法进行配置文件的加载
        // 使用InPutStream流读取properties文件
        String currentWorkPath = System.getProperty("user.dir");
        InputStream is = null;
        PropertyResourceBundle configResource = null;
        
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(currentWorkPath + "/config/resource.properties"));) {

            configResource = new PropertyResourceBundle(bufferedReader);
            nacosUseCluster = configResource.getString(PropertiesConstants.NACOS_CLUSTER_USE);

            if ("false".equals(nacosUseCluster) || "default".equals(nacosUseCluster) || StringUtils.isBlank(nacosUseCluster)) {
                log.info("begin start with single mode");
                nacosNodes = new String[1];
                try {
                    nacosNodes[0] = configResource.getString(PropertiesConstants.NACOS_REGISTER_ADDR);
                } catch (MissingResourceException registerAddException) {
                    nacosNodes[0] = nacosServerAddr;
                    log.warn("nacos register address attribute is missing");
                    log.info("use default register address : " + nacosServerAddr);
                }
            } else if ("true".equals(nacosUseCluster)) {
                log.info("cluster mode attribute is true and start with cluster mode");
                try {
                    nacosBalancer = configResource.getString(PropertiesConstants.NACOS_LOAD_BALANCER);
                } catch (MissingResourceException loadBalancerException) {
                    log.info("nacos property attribute is missing: {}", loadBalancerException.getMessage());
                    log.info("use default loadBalancer : " + nacosBalancer);
                }
                try {
                    nacosNodes = configResource.getString(PropertiesConstants.NACOS_CLUSTER_NODES).split("[;,|]");
                } catch (MissingResourceException clusternacosNodesException) {
                    log.error("nacos cluster nacosNodes attribute is missing: ", clusternacosNodesException);
                    throw new RuntimeException("nacos cluster nacosNodes attribute is missing!");
                }
            } else {
                throw new RuntimeException("nacos cluster mode attribute is illegal!");
            }
            log.info("read resource from resource path: {}", currentWorkPath + "/config/resource.properties");
        } catch (MissingResourceException clusterUseException) {
            log.warn("nacos cluster use attribute is missing");
            log.info("begin start with default single mode");
            nacosNodes = new String[1];
            try {
                String nacosRegisterAddr = configResource.getString(PropertiesConstants.NACOS_REGISTER_ADDR);
                nacosNodes[0] = StringUtils.isBlank(nacosRegisterAddr) ? nacosServerAddr : nacosRegisterAddr;
            } catch (MissingResourceException registerAddException) {
                nacosNodes[0] = nacosServerAddr;
                log.warn("nacos register address attribute is missing");
                log.info("use default register address : " + nacosServerAddr);
            }
        } catch (IOException ioException) {
            log.info("not found resource from resource path: {}", currentWorkPath + "/config/resource.properties");
            try {
                ResourceBundle resource = ResourceBundle.getBundle("resource");

                try {
                    nacosUseCluster = resource.getString(PropertiesConstants.NACOS_CLUSTER_USE);

                    if ("false".equals(nacosUseCluster) || "default".equals(nacosUseCluster) || StringUtils.isBlank(nacosUseCluster)) {
                        log.info("begin start with default single mode");
                        nacosNodes = new String[1];
                        try {
                            String nacosRegisterAddr = resource.getString(PropertiesConstants.NACOS_REGISTER_ADDR);
                            nacosNodes[0] = StringUtils.isBlank(nacosRegisterAddr) ? nacosServerAddr : nacosRegisterAddr;
                        } catch (MissingResourceException registerAddException) {
                            nacosNodes[0] = nacosServerAddr;
                            log.warn("nacos register address attribute is missing");
                            log.info("use default register address : " + nacosServerAddr);
                        }
                    } else if ("true".equals(nacosUseCluster)) {
                        log.info("cluster mode attribute is true and start with cluster mode");
                        try {
                            nacosBalancer = resource.getString(PropertiesConstants.NACOS_LOAD_BALANCER);
                        } catch (MissingResourceException loadBalancerException) {
                            log.info("nacos property attribute is missing: {}", loadBalancerException.getMessage());
                            log.info("use default loadBalancer : " + nacosBalancer);
                        }
                        try {
                            nacosNodes = resource.getString(PropertiesConstants.NACOS_CLUSTER_NODES).split("[;,|]");
                        } catch (MissingResourceException clusternacosNodesException) {
                            log.error("nacos cluster nacosNodes attribute is missing: ", clusternacosNodesException);
                            throw new RuntimeException("nacos cluster nacosNodes attribute is missing!");
                    }
                    } else {
                        throw new RuntimeException("nacos cluster mode attribute is illegal!");
                    }
                } catch (MissingResourceException clusterUseException) {
                    log.info("cluster mode attribute is missing and start with single mode");
                    nacosNodes = new String[1];
                    nacosNodes[0] = resource.getString(PropertiesConstants.NACOS_REGISTER_ADDR);
                }

            } catch (MissingResourceException resourceException) {
                log.info("not found resource from resource path: {}", "resource.properties");
                log.info("Register center bind with default address {}", nacosServerAddr);
            }
        }
        log.info("read resource from resource path: {}", "resource.properties");
        log.info("------------ nacos Configuration 【 begin 】 ------------");
        log.info("nacosUseCluster: [{}]", nacosUseCluster);
        log.info("nacosNodes: [{}]", nacosNodes);
        log.info("nacosBalancer: [{}]", nacosBalancer);
        log.info("------------ nacos Configuration 【 end 】 ------------");
        int pre = -1;
        String host = "";
        Integer port = 0;

        String node = null;
        if ("random".equals(nacosBalancer)) {
            loadBalancer = LoadBalancer.getByCode(LoadBalancerCode.RANDOM.getCode());
            log.info("use { {} } loadBalancer", loadBalancer.getClass().getName());
        }
        else if ("round".equals(nacosBalancer)) {
            loadBalancer = LoadBalancer.getByCode(LoadBalancerCode.ROUNDROBIN.getCode());
            log.info("use { {} } loadBalancer", loadBalancer.getClass().getName());
        } else {
            log.error("naocs cluster loadBalancer attribute is illegal!");
            throw new RuntimeException("naocs cluster loadBalancer attribute is illegal!");
        }
        do {
            try {
                node = loadBalancer.selectNode(nacosNodes);
                log.info("waiting for connection to the registration center...");

                if ((pre = node.indexOf(":")) > 0 && pre == node.lastIndexOf(":")) {
                    boolean valid = IpUtils.valid(host = node.substring(0, pre));
                    if (valid) {
                        host = node.substring(0, pre);
                        port = Integer.parseInt(node.substring(pre + 1));
                        if (host.equals("localhost")) {
                            nacosServerAddr = "127.0.0.1:" + port;
                        } else {
                            nacosServerAddr = node;
                        }
                    } else {
                        log.error("wrong ip address: {}", node);
                    }
                } else if (!node.equals("")) {
                    log.error("wrong ip address: {}", node);
                }
            } catch (RpcException e) {
                e.printStackTrace();
            }
            // 初始化 Nacos 注册中心服务接口
            namingService = getNacosNamingService(null);
        } while (namingService.getServerStatus() == "DOWN");
        if (namingService.getServerStatus() == "UP")
            log.info("Register center bind with address {}", node);
        else if (nacosNodes != null && nacosNodes.length == 1)
            log.error("SingleTon Register Center is down from {}", nacosServerAddr);
        else if (nacosNodes != null && nacosNodes.length != 1) {
            log.error("Cluster Register Center is down from ");
            log.error("---");
            for (int i = 0; i < nacosNodes.length; i++) {
                log.error("{}", nacosNodes[i]);
            }
            log.error("---");
        } else
            log.error("Service occupy Internal Errors");
    }

    /**
     * 获取绑定的 Nacos 服务
     * @return Nacos 服务
     * 服务注册到 注册中心 后，由注册中心集群 做 服务同步
     */
    private static NamingService getNacosNamingService(String newAddress) {
        try {
            return NamingFactory.createNamingService(newAddress != null ? newAddress: nacosServerAddr);
        } catch (NacosException e) {
            log.error("error occurred when connecting to nacos server: ", e);
            return null;
        }
    }

    /**
     * 获取注册中心中与服务名匹配的所有实例，可以通过使用负载均衡选择其中一个实例
     * 注册中心自动容灾切换
     * @param serviceName 服务名
     * @return 实例列表
     * @throws NacosException
     */
    public static List<Instance> getAllInstance(String serviceName) throws RpcException, NacosException {
        if (nacosNodes.length != 1 && namingService.getServerStatus() == "DOWN") {
            String node = loadBalancer.selectNode(nacosNodes);
            log.info("disaster recovery switch occurred in registration center[{}]", nacosServerAddr);
            namingService = getNacosNamingService(node);
            if (namingService.getServerStatus() == "UP") {
                nacosServerAddr = node;
                log.info("The registry node switches to {}", nacosServerAddr);
                return namingService.getAllInstances(serviceName);
            }
            log.warn("naocs server [{}] is unavalable", nacosNodes);
        }
        return namingService.getAllInstances(serviceName);
    }

    /**
     * 获取注册中心中与服务名匹配的所有实例，可以通过使用负载均衡选择其中一个实例
     * 注册中心自动容灾切换
     * @param serviceName 服务名
     * @param groupName 组名
     * @return 实例列表
     * @throws NacosException
     */
    public static List<Instance> getAllInstance(String serviceName, String groupName) throws NacosException, RpcException {
        if (nacosNodes.length != 1 && namingService.getServerStatus() == "DOWN") {
            String node = loadBalancer.selectNode(nacosNodes);
            log.info("disaster recovery switch occurred in registration center[{}]", nacosServerAddr);
            namingService = getNacosNamingService(node);
            if (namingService.getServerStatus() == "UP") {
                nacosServerAddr = node;
                log.info("The registry node switches to {}", nacosServerAddr);
                return namingService.getAllInstances(serviceName, groupName);
            }
            log.warn("naocs server [{}] is unavalable", nacosNodes);
        }
        return namingService.getAllInstances(serviceName, groupName);
    }

    /**
     * 将服务名与对应服务所在的地址注册到注册中心
     * @param serviceName 服务名
     * @param address 服务所在机器地址
     * @throws NacosException
     */
    public static void registerService(String serviceName, InetSocketAddress address) throws NacosException {
        namingService.registerInstance(serviceName, address.getHostName(), address.getPort());
        log.info("host[{}], service[{}] has been registered on Register Center", address.getHostName(), serviceName);
        inetSocketAddress = address;
        serviceNames.add(serviceName);
    }

    /**
     * 将服务名与对应服务所在的地址注册到注册中心
     * @param serviceName 服务名
     * @param address 服务所在机器地址
     * @throws NacosException
     */
    public static void registerService(String serviceName, String groupName, InetSocketAddress address) throws NacosException {
        namingService.registerInstance(serviceName, groupName, address.getHostName(), address.getPort());
        log.info("host[{}], service[{}] has been registered on Register Center", address.getHostName(), serviceName);
        inetSocketAddress = address;
        serviceNames.add(serviceName);
    }

    /**
     * 清除服务启动所在地址下注册表所有服务项
     */
    public static void clearRegistry() {
        if (!serviceNames.isEmpty() && inetSocketAddress != null) {
            String hostname = inetSocketAddress.getHostName();
            int port = inetSocketAddress.getPort();
            Iterator<String> iterator = serviceNames.iterator();
            while (iterator.hasNext()) {
                String serviceName = iterator.next();
                try {
                    serviceNames.remove(serviceName);
                    namingService.deregisterInstance(serviceName, hostname,  port);
                } catch (NacosException e) {
                    log.error("Failed to cancel service:{}, info:{}",serviceName, e);
                }
            }
            log.info("All services on the nacos service have been cleaned up successfully");
        }
    }

}

