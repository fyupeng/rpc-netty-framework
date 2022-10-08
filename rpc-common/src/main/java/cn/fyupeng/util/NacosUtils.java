package cn.fyupeng.util;

import cn.fyupeng.enums.LoadBalancerCode;
import cn.fyupeng.exception.RpcException;
import cn.fyupeng.loadbalancer.LoadBalancer;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
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

    private static final Set<String> serviceNames = new HashSet<>();
    private static LoadBalancer loadBalancer;
    private static String SERVER_ADDR = "127.0.0.1:8848";
    private static NamingService namingService;
    private static InetSocketAddress inetSocketAddress;


    //2. 加载配置文件，只需加载一次
    static {
        //2.1 创建Properties对象
        Properties p = new Properties();
        //2.2 调用p对象中的load方法进行配置文件的加载
        // 使用InPutStream流读取properties文件
        String currentWorkPath = System.getProperty("user.dir");
        InputStream is = null;
        String[] nodes = null;
        String useCluster = "";
        String balancer = "round";
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(currentWorkPath + "/config/resource.properties"));) {
            p.load(bufferedReader);
            useCluster = p.getProperty("cn.fyupeng.nacos.cluster.use");
            if (useCluster == null || "false".equals(useCluster)) {
                log.info("cluster attribute is false and start with single mode");
                nodes = new String[1];
                nodes[0] = p.getProperty("cn.fyupeng.nacos.register-addr");
            } else if ("true".equals(useCluster)) {
                log.info("cluster attribute is true and start with cluster mode");
                nodes = p.getProperty("cn.fyupeng.nacos.cluster.nodes").split("[;,|]");
            }
            log.info("read resource from resource path: {}", currentWorkPath + "/config/resource.properties");
        } catch (IOException e) {
            log.info("not found resource from resource path: {}", currentWorkPath + "/config/resource.properties");
            is = NacosUtils.class.getClassLoader().getResourceAsStream("resource.properties");
            if(is != null) {
                log.info("read resource from resource path: {}", NacosUtils.class.getClassLoader().getResource("resource.properties").getPath());
                try {
                    p.load(is);
                    // 自定义 指定的注册中心地址，将覆盖默认地址
                    useCluster = p.getProperty("cn.fyupeng.nacos.cluster.use");
                    if (useCluster == null || "false".equals(useCluster)) {
                        log.info("cluster attribute is false and start with single mode");
                        nodes = new String[1];
                        nodes[0] = p.getProperty("cn.fyupeng.nacos.register-addr");
                    } else if ("true".equals(useCluster)) {
                        log.info("cluster attribute is true and start with cluster mode");
                        balancer = p.getProperty("cn.fyupeng.nacos.cluster.load-balancer");
                        nodes = p.getProperty("cn.fyupeng.nacos.cluster.nodes").split("[;,|]");
                    }
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

        String node = null;
        do {
            if ("random".equals(balancer)) {
                loadBalancer = LoadBalancer.getByCode(LoadBalancerCode.RANDOM.getCode());
                log.info("use { {} } loadBalancer for select cluster nodes", loadBalancer.getClass().getName());
            }
            else if ("round".equals(balancer)) {
                loadBalancer = LoadBalancer.getByCode(LoadBalancerCode.ROUNDROBIN.getCode());
                log.info("use { {} } loadBalancer for select cluster nodes", loadBalancer.getClass().getName());
            }
            try {
                node = loadBalancer.selectNode(nodes);

                if ((pre = node.indexOf(":")) > 0 && pre == node.lastIndexOf(":")) {
                    boolean valid = IpUtils.valid(host = node.substring(0, pre));
                    if (valid) {
                        host = node.substring(0, pre);
                        port = Integer.parseInt(node.substring(pre + 1));
                        if (host.equals("localhost")) {
                            SERVER_ADDR = "127.0.0.1:" + port;
                        } else {
                            SERVER_ADDR = node;
                        }

                    } else {
                        log.error("wrong ip address: {}", node);
                    }
                } else if (!node.equals("")) {
                    log.error("wrong ip address: {}", node);
                }

            } catch (RpcException e) {

            }
            // 初始化 Nacos 注册中心服务接口
            namingService = getNacosNamingService();
        } while (namingService.getServerStatus() == "DOWN");
        if (namingService.getServerStatus() == "UP")
            log.info("Register center bind with address {}", node);
        else if (nodes != null && nodes.length == 1)
            log.error("SingleTon Register Center is down from {}", SERVER_ADDR);
        else if (nodes != null && nodes.length != 1) {
            log.error("Cluster Register Center is down from ");
            log.error("---");
            for (int i = 0; i < nodes.length; i++) {
                log.error("{}", nodes[i]);
            }
            log.error("---");
        } else
            log.error("Service occupy Internal Errors");
    }


    /**
     * 获取绑定的 Nacos 服务
     * @return Nacos 服务
     */
    public static NamingService getNacosNamingService() {
        try {
            return NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            log.error("error occurred when connecting to nacos server: ", e);
            return null;
        }
    }

    /**
     * 获取配置中心中与服务名匹配的所有实例，可以通过使用负载均衡选择其中一个实例
     * @param serviceName 服务名
     * @return 实例列表
     * @throws NacosException
     */
    public static List<Instance> getAllInstance(String serviceName) throws  NacosException {
        return namingService.getAllInstances(serviceName);
    }

    /**
     * 将服务名与对应服务所在的地址注册到注册中心
     * @param serviceName 服务名
     * @param address 服务所在机器地址
     * @throws NacosException
     */
    public static void registerService(String serviceName, InetSocketAddress address) throws NacosException {
        namingService.registerInstance(serviceName, address.getHostName(), address.getPort());
        log.info("host: {} has been registered on Register Center", address.getHostName());
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
                    namingService.deregisterInstance(serviceName, hostname,  port);
                } catch (NacosException e) {
                    log.error("Failed to cancel service:{}, info:{}",serviceName, e);
                }
            }
            log.info("All services on the nacos service have been cleaned up successfully");
        }
    }


}
