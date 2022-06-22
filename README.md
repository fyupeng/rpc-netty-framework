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
## Highlights
### 1. Application of Information Digest Algorithm
对于信息摘要算法的使用，其实并不难，在数据包中添加 `String` 类型的成员变量 `checkCode` 用来防伪的就可以实现。
- 原理

发送端把原信息用`HASH`函数加密成摘要,然后把数字摘要和原信息一起发送到接收端,接收端也用HASH函数把原消息加密为摘要,看两个摘要是否相同,若相同,则表明信息的完整.否则不完整。
- 实现

客户端在发出 请求包，服务端会在该请求包在请求执行的结果的内容转成字节码后，使用 MD5 单向加密成为 唯一的信息摘要（128 比特，16 字节）存储到响应包对应的成员变量 `checkCode` 中，所以客户端拿到响应包后，最有利用价值的地方（请求要执行的结果被改动），那么 `checkCode` 将不能保证一致性，这就是信息摘要的原理应用。

**安全性再增强**

考虑到这只是针对客户需求的结果返回一致性，并不能确保请求包之间存在相同的请求内容，所以引入了请求 id，每个包都会生成唯一的 requestId，发出请求包后，该包只能由该请求发出的客户端所接受，就算两处有一点被对方恶意改动了，客户端都会报错并丢弃收到的响应包，不会拆包后去返回给用户，如果不是单单改动了 返回结果，而是将结果跟信息摘要都修改了，对方很难保证修改的内容加密后与修改后的信息摘要一致，因为要保证一致的数据传输协议和数据编解码
### 2. Heartbeat mechanism

心跳机制的 `RPC` 上应用的很广泛，本项目对心跳机制的实现很简单，而且应对措施是服务端强制断开连接，当然有些 `RPC` 框架实现了服务端去主动尝试重连。
- 原理

对于心跳机制的应用，其实是使用了 `Netty` 框架中的一个 `handler` 处理器，通过该 处理器，去定时发送心跳包，让服务端知道该客户端保持活性状态。

- 实现

利用了 `Netty` 框架中的 `IdleStateEvent` 事件监听器，重写`userEventTriggered()` 方法，在服务端监听读操作，读取客户端的 写操作，在客户端监听写操作，监听本身是否还在活动，即有没有向服务端发送请求。
如果客户端没有主动断开与服务端的连接，而继续保持连接着，那么客户端的写操作超时后，也就是客户端的监听器监听到客户端没有的规定时间内做出写操作事件，那么这时客户端该处理器主动发送心跳包给服务端，保证客户端让服务端确保自己保持着活性。
### 3. IO asynchronous non-blocking

IO 异步非阻塞 能够让客户端在请求数据时处于阻塞状态，而且能够在请求数据返回时间段里去处理自己感兴趣的事情。

- 原理

使用 java8 出世的 `CompletableFuture` 并发工具类，能够异步处理数据，并在将来需要时获取。

- 实现

数据在服务端与客户端之间的通道 `channel` 中传输，客户端向通道发出请求包，需要等待服务端返回，这时可使用 `CompletableFuture` 作为返回结果，只需让客户端读取到数据后，将结果通过 `complete()`方法将值放进去后，在将来时通过`get()`方法获取结果。

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
