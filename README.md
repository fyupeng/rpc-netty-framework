## Introduction
### 1. server
- 负载均衡策略
- 序列化策略
- 自动发现和注销服务
- 注册中心
### 2. Security
- 心跳机制
- 信息摘要
### 3. Design Patterns
- 单例模式
- 动态代理
- 静态工厂
- 建造者
- 策略模式
- Future(观察者）
## Quick Start
```java
/**
     * 自定义对象头 协议 16 字节
     * 4 字节 魔数
     * 4 字节 协议包类型
     * 4 字节 序列化类型
     * 4 字节 数据长度
     *
     *       The transmission protocol is as follows :
     * +---------------+---------------+-----------------+-------------+
     * | Magic Number  | Package Type  | Serializer Type | Data Length |
     * | 4 bytes       | 4 bytes       | 4 bytes         | 4 bytes     |
     * +---------------+---------------+-----------------+-------------+
     * |                           Data Bytes                          |
     * |                       Length: ${Data Length}                  |
     * +---------------+---------------+-----------------+-------------+
     */
```
### 1.Dependences
首先引入两个jar包文件`rpc-core-1.0-SNAPSHOT.jar` 和 `rpc-core-1.0-SNAPSHOT-jar-with-dependencies.jar`

`jar`包中包括字节码文件和`java`源码，引入后会自动把`class`和`sources`一并引入，源码可作为参考

![依赖](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/依赖.png)

### 2. Start Nacos 

`-m:模式`，`standalone:单机`

命令使用:

```ruby
startup -m standalone
```

> 注意：开源RPC 默认使用 nacos 指定的本地端口号 8848 

官方文档：https://nacos.io/zh-cn/docs/quick-start.html

nacos 启动效果：

![效果](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/nacos.png)

### 3. Provide Interface
```java
public interface HelloService {
    String sayHello(String message);
}
```
### 4. Start Server
- 真实服务
```java
@Service
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String message) {
        return "hello, here is service！";
    }
}
```
- 服务启动器
```java
@ServiceScan
public class MyServer {
    public static void main(String[] args) {
        try {
            NettyServer nettyServer = new NettyServer("127.0.0.1", 5000, SerializerCode.KRYO.getCode());
            nettyServer.start();
        } catch (RpcException e) {
            e.printStackTrace();
        }
    }
}
```
> 注意：增加注解`com.zhkucst.anotion.Service`和`com.zhkucst.anotion.ServiceScan`才可被自动发现服务扫描并注册到 nacos 

### 5. Start Client
初始化客户端时连接服务端有两种方式：
- 直连
- 使用负载均衡
```java
public class MyClient {
    public static void main(String[] args) {
        RoundRobinLoadBalancer roundRobinLoadBalancer = new RoundRobinLoadBalancer();
        NettyClient nettyClient = new NettyClient(roundRobinLoadBalancer, CommonSerializer.KRYO_SERIALIZER);

        RpcClientProxy rpcClientProxy = new RpcClientProxy(nettyClient);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String result = helloService.sayHello("hello");
        System.out.println(result);
    }
}
```
### 5. extra setting

logback 重写使用

在 resources 中加入 logback.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <!--%date{HH:mm:ss.SSS} %c -->
            <pattern>%date{HH:mm:ss.SSS} %c [%t] - %m%n</pattern>
        </encoder>
    </appender>

    <root level="info">
        <appender-ref ref="console"/>
    </root>
</configuration>
```
除此之外，框架还提供了 Socket 方式的 Rpc 服务
