## 介绍

![Version](https://img.shields.io/static/v1?label=VERSION&message=2.0.4&color=brightgreen)
![Jdk](https://img.shields.io/static/v1?label=JDK&message=8.0&color=green)
![Nacos](https://img.shields.io/static/v1?label=NACOS&message=1.43&color=orange)
![Netty](https://img.shields.io/static/v1?label=NETTY&message=4.1.20.Final&color=blueviolet)
![Version](https://img.shields.io/static/v1?label=LICENCE&message=MIT&color=brightgreen)

一个分布式微服务RPC框架 | [英文说明文档](README.md) | [SpringBoot整合RPC](springboot整合rpc-netty-framework.md)

- [x] 基于`Socket`和`Netty`异步非阻塞通信的解决方案；
- [x] 支持`JDK`内置`SPI`机制，实现接口与实现解耦；
- [x] 注册中心高可用性，提供集群注册中心，所有注册节点宕机后仍能通过缓存为用户持续提供服务；
- [x] 提供个性化服务，推出个性化服务`name`、服务`group`，适合在测试、实验和正式环境的服务，以及为后期版本的兼容、维护和升级提供更好的服务；
- [ ] 提供集群注册中心宕机重启服务；
- [x] 提供服务无限制横向扩展；
- [x] 提供服务的两种负载均衡策略，如随机和轮询负载；
- [x] 提供请求超时重试，且保障业务执行的幂等性，超时重试能降低线程池任务的延迟，线程池保障了高并发场景下线程数创建数量的稳定，却因而带来延迟问题，处理该问题可以启用重试请求，且重试达到阈值将放弃请求，认为该服务暂时不可用，造成业务损耗，请慎用；
- [ ] 提供自定义注解扩展服务，使用代理扩展，能无侵入式扩展个性化服务；
- [x] 提供可扩展的序列化服务，目前提供`Kryo`和`Jackson`两种序列化方式；
- [x] 提供日志框架`Logback`；
- [x] 提供Netty可扩展的通信协议，通信协议头使用与Class一样的16位魔数`0xCAFEBABE`、包辨识id，用来辨识请求包和响应包、`res`长度，用来防止粘包，以及最后的`res`，内部加入检验码和唯一识别id，让服务器能高效地同时处理多个不同请求包或重发请求包，以及包校验；

架构图

- 重试机制架构图

![超时重试.png](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/超时重试.png)


- 服务发现与注册架构图

![服务发现与注册.png.png](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/服务发现与注册.png)

### 1. 服务提供
- 负载均衡策略
- 序列化策略
- 自动发现和注销服务
- 注册中心
- 单机与集群

### 2. 安全策略
- 心跳机制
- 信息摘要
- 超时重试机制
- 幂等性
### 3. 设计模式
- 单例模式
- 动态代理
- 静态工厂
- 建造者
- 策略模式
- Future(观察者）
## 亮点
### 1. 信息摘要算法的应用
对于信息摘要算法的使用，其实并不难，在数据包中添加 `String` 类型的成员变量 `checkCode` 用来防伪的就可以实现。
- 原理

发送端把原信息用`HASH`函数加密成摘要,然后把数字摘要和原信息一起发送到接收端,接收端也用HASH函数把原消息加密为摘要,看两个摘要是否相同,若相同,则表明信息的完整.否则不完整。
- 实现

客户端在发出 请求包，服务端会在该请求包在请求执行的结果的内容转成字节码后，使用 MD5 单向加密成为 唯一的信息摘要（128 比特，16 字节）存储到响应包对应的成员变量 `checkCode` 中，所以客户端拿到响应包后，最有利用价值的地方（请求要执行的结果被改动），那么 `checkCode` 将不能保证一致性，这就是信息摘要的原理应用。

**安全性再增强**

考虑到这只是针对客户需求的结果返回一致性，并不能确保请求包之间存在相同的请求内容，所以引入了请求 `id`。

每个包都会生成唯一的 `requestId`，发出请求包后，该包只能由该请求发出的客户端所接受，就算两处有一点被对方恶意改动了，客户端都会报错并丢弃收到的响应包，不会拆包后去返回给用户。

如果不是单单改动了返回结果，而是将结果跟信息摘要都修改了，对方很难保证修改的内容加密后与修改后的信息摘要一致，因为要保证一致的数据传输协议和数据编解码。
### 2. 心跳机制

心跳机制的 `RPC` 上应用的很广泛，本项目对心跳机制的实现很简单，而且应对措施是服务端强制断开连接，当然有些 `RPC` 框架实现了服务端去主动尝试重连。
- 原理

对于心跳机制的应用，其实是使用了 `Netty` 框架中的一个 `handler` 处理器，通过该 处理器，去定时发送心跳包，让服务端知道该客户端保持活性状态。

- 实现

利用了 `Netty` 框架中的 `IdleStateEvent` 事件监听器，重写`userEventTriggered()` 方法，在服务端监听读操作，读取客户端的 写操作，在客户端监听写操作，监听本身是否还在活动，即有没有向服务端发送请求。

如果客户端没有主动断开与服务端的连接，而继续保持连接着，那么客户端的写操作超时后，也就是客户端的监听器监听到客户端没有的规定时间内做出写操作事件，那么这时客户端该处理器主动发送心跳包给服务端，保证客户端让服务端确保自己保持着活性。

### 3. SPI 机制

资源目录`META-INF/services`下新建接口全限定名作为文件名，内容为实现类全限定名，支持`JDK`内置`SPI`。

本质通过反射来无参构造创建实例，如果构造函数涉及到通过参数来实现注入成员，那么可将接口转为抽象类，抽象类暴露set方法来让子类重写，从而间接实现注入。

该机制将注册中心逻辑层处理服务发现和注册的接口时实现分离到配置文件`META-INF/services`，从而更好地去支持其他插件，如`Zookeeper`、`Eureka`的扩展。

应用到的配置文件：
- `cn.fyupeng.discovery.ServiceDiscovery`

```properties
cn.fyupeng.discovery.NacosServiceDiscovery
```
- `cn.fyupeng.provider.ServiceProvider`

```properties
cn.fyupeng.provider.DefaultServiceProvider
```
- `cn.fyupeng.registry.ServiceRegistry`
```properties
cn.fyupeng.registry.NacosServiceRegistry
```
### 4. IO 异步非阻塞

IO 异步非阻塞 能够让客户端在请求数据时处于阻塞状态，而且能够在请求数据返回时间段里去处理自己感兴趣的事情。

- 原理

使用 java8 出世的 `CompletableFuture` 并发工具类，能够异步处理数据，并在将来需要时获取。

- 实现

数据在服务端与客户端之间的通道 `channel` 中传输，客户端向通道发出请求包，需要等待服务端返回，这时可使用 `CompletableFuture` 作为返回结果，只需让客户端读取到数据后，将结果通过 `complete()`方法将值放进去后，在将来时通过`get()`方法获取结果。



### 5. RNF 协议

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

## 快速开始

### 1.依赖

#### 1.1 直接引入

首先引入两个jar包文件`rpc-core-1.0.0.jar` 和 `rpc-core-1.0.0-jar-with-dependencies.jar`

`jar`包中包括字节码文件和`java`源码，引入后会自动把`class`和`sources`一并引入，源码可作为参考

![依赖](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/依赖.png)

#### 1.2 maven引入

引入以下`maven`，会一并引入`rpc-common`与默认使用的注册中心`nacos-client`相关依赖

```xml
<dependency>
    <groupId>cn.fyupeng</groupId>
    <artifactId>rpc-core</artifactId>
    <version>2.0.4</version>
</dependency>
```
阿里仓库10月份开始处于系统升级，有些版本还没同步过去，推荐另一个`maven`官方仓库：
```xml
<mirror>
  <id>repo1maven</id>
  <mirrorOf>*</mirrorOf>
  <name>maven公共仓库</name>
  <url>https://repo1.maven.org/maven2</url>
</mirror>
```
### 2. 启动 Nacos

`-m:模式`，`standalone:单机`

命令使用:

```ruby
startup -m standalone
```

> 注意：开源RPC 默认使用 nacos 指定的本地端口号 8848 

官方文档：https://nacos.io/zh-cn/docs/quick-start.html

优势：

选用`Nacos`作为注册中心，是因为有较高的可用性，可实现服务长期可靠
- 注册中心服务列表在本地保存一份
- 宕机节点在自动重启恢复期间，服务依旧可用

Nacos 启动效果：

![效果](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/nacos.png)

### 3. 提供接口
```java
public interface HelloService {
    String sayHello(String message);
}
```
### 4. 启动服务
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
> 注意：增加注解`cn.fyupeng.Service`和`cn.fyupeng.ServiceScan`才可被自动发现服务扫描并注册到 nacos 

### 5. 启动客户端
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
### 5. 额外配置

#### 5.1 配置文件

- 项目方式启动

在 resources 中加入 resource.properties

主机名使用`localhost`或`127.0.0.1`指定
```properties
cn.fyupeng.nacos.register-addr=localhost:8848
```

- `Jar`方式启动

，兼容`springboot`的外部启动配置文件注入，需要在`Jar`包同目录下新建`config`文件夹，在`config`中与`springboot`一样注入配置文件，只不过`springboot`注入的配置文件默认约束名为`application.properties`，而`rpc-netty-framework`默认约束名为`resource.properties`。

目前可注入的配置信息有：
```properties
cn.fyupeng.nacos.register-addr=localhost:8848
cn.fyupeng.nacos.cluster.use=true
cn.fyupeng.nacos.cluster.load-balancer=random
cn.fyupeng.nacos.cluster.nodes=192.168.10.1:8847,192.168.10.1:8848,192.168.10.1:8849
```

#### 5.2 日志配置

在 `resources` 中加入 `logback.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <!--%date{HH:mm:ss.SSS} %c -->
      <pattern>%date{HH:mm:ss.SSS} [%level] %c [%t] - %m%n</pattern>
    </encoder>
  </appender>

  <root level="info">
    <appender-ref ref="console"/>
  </root>
</configuration>
```
除此之外，框架还提供了 Socket 方式的 Rpc 服务

### 6. 场景应用

- 支持 springBoot 集成

为了支持`springBoot`集成`logback`日志，继承`rpc-netty-framework`使用同一套日志，抛弃`nacos-client`内置的`slf4j-api`与`commons-loging`原有`Jar`包，因为该框架会导致在整合`springboot`时，出现重复的日志绑定和日志打印方法的参数兼容问题，使用`jcl-over-slf4j-api`可解决该问题；

在`springboot1.0`和`2.0`版本中，不使用它默认版本的`spring-boot-starter-log4j`,推荐使用`1.3.8.RELEASE`；
springboot简单配置如下
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
        <exclusions>
            <!-- 排除 springboot 默认的 logback 日志框架 -->
            <exclusion>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-logging</artifactId>
            </exclusion>
            <!-- 排除 springboot 默认的 commons-logging 实现（版本低，出现方法找不到问题） -->
            <exclusion>
                <groupId>org.springframework</groupId>
                <artifactId>spring-jcl</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    
    <!-- 与 logback 整合（通过 @Slf4j 注解即可使用） -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.10</version>
    </dependency>
    <!--引入log4j日志依赖，目的是使用 jcl-over-slf4j 来重写 commons logging 的实现-->
    <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j</artifactId>
    <version>1.3.8.RELEASE</version>
    </dependency>
</dependencies>

```

### 7. 高可用集群

```properties
cn.fyupeng.nacos.cluster.use=true
cn.fyupeng.nacos.cluster.load-balancer=random
cn.fyupeng.nacos.cluster.nodes=192.168.10.1:8847,192.168.10.1:8848,192.168.10.1:8849
```
- 使用方法及注意要点

默认情况下，`cn.fyupeng.nacos.cluster.use`服从约定优于配置设定，且默认值为`false`，表示默认不打开集群；

`cn.fyupeng.nacos.cluster.load-balancer`与`cn.fyupeng.nacos.cluster.nodes`使用前必须先打开集群模式

- 集群节点负载策略
  - `random`：随机策略
  - `round`：轮询策略

集群节点理论上允许无限扩展，可使用分隔符`[;,|]`扩展配置

- 集群节点容错切换
  - 节点宕机：遇到节点宕机将重新从节点配置列表中选举新的正常节点，否则无限重试

### 8. 超时重试机制

默认不使用重试机制，为了保证服务的正确性，因为无法保证幂等性。

原因是客户端无法探测是客户端网络传输过程出现问题，或者是服务端正确接收后返回途中网络传输出现问题，因为如果是前者那么重试后能保证幂等性，如果为后者，可能将导致多次同个业务的执行，这对客户端来说结果是非一致的。

超时重试处理会导致出现幂等性问题，因此在服务器中利用`HashSet`添加请求`id`来做超时处理

- 超时重试：`cn.fyupeng.anotion.Reference`注解提供重试次数、超时时间和异步时间三个配置参数，其中：
  - 重试次数：服务端未能在超时时间内 响应，允许触发超时的次数
  - 超时时间：即客户端最长允许等待 服务端时长，超时即触发重试机制
  - 异步时间：即等待服务端异步响应的时间，且只能在超时重试机制使用，非超时重试情况下默认使用阻塞等待方式

> 示例：
```java
private static RandomLoadBalancer randomLoadBalancer = new RandomLoadBalancer();
    private static NettyClient nettyClient = new NettyClient(randomLoadBalancer, CommonSerializer.KRYO_SERIALIZER);
    private static RpcClientProxy rpcClientProxy = new RpcClientProxy(nettyClient);

    @Reference(retries = 2, timeout = 1000, asyncTime = 3000)
    private static HelloWorldService service = rpcClientProxy.getProxy(HelloWorldService.class, Client.class);
```
重试的实现也不难，采用`代理 + for + 参数`来实现即可。
> 核心代码实现：
```java
for (int i = 0; i <= retries; i++) {
    long startTime = System.currentTimeMillis();

    CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) rpcClient.sendRequest(rpcRequest);
    try {
        rpcResponse = completableFuture.get(asyncTime, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
        // 忽视 超时引发的异常，自行处理，防止程序中断
        timeoutRes.incrementAndGet();
        if (timeout >= asyncTime) {
            log.warn("asyncTime [ {} ] should be greater than timeout [ {} ]", asyncTime, timeout);
        }
        continue;
    }

    long endTime = System.currentTimeMillis();
    long handleTime = endTime - startTime;
    if (handleTime >= timeout) {
        // 超时重试
        log.warn("invoke service timeout and retry to invoke");
    } else {
        // 没有超时不用再重试
        // 进一步校验包
        if (RpcMessageChecker.check(rpcRequest, rpcResponse)) {
            res.incrementAndGet();
            return rpcResponse.getData();
        }
    }
}
```

- 幂等性

重试机制服务端要保证重试包的一次执行原则，即要实现幂等性

实现思路：需要借助`HashSet`和`HashMap`来存放超时重试请求包的请求`id`和上一次请求执行结果，如何判定是否重试即可通过`Set`集合的`add`方法添加，添加失败即为重试包，将请求`id`对应上一次请求应返回结果返回给客户端即可，最后一步是做垃圾清理。

由于考虑并发性，垃圾处理使用双重校验锁，即通过`if`判断`Set`阈值和`synchronized`关键字配合使用来实现。


### 9. 异常解决
- ServiceNotFoundException

抛出异常`ServiceNotFoundException`

堆栈信息：`service instances size is zero, can't provide service! please start server first!`

正常情况下，一般的错误从报错中可以引导解决。

解决真实服务不存在的情况，导致负载均衡中使用的策略出现异常的情况，修复后会强制抛出`ServiceNotFoundException`，或许大部分情况是服务未启动。

当然，推荐真实服务应该在服务启动器的内层包中，同层可能会不起作用。

除非使用注解注明包名`@ServiceScan("com.fyupeng")`

其他情况下，如出现服务端无反应，而且服务已经成功注册到注册中心，那么你就得检查下服务端与客户端中接口命名的包名是否一致，如不一致，也是无法被自动发现服务从注册中心发现的，这样最常见的报错也是`service instances size is zero`。



- ReceiveResponseException

抛出异常`data in package is modified Exception`

信息摘要算法的实现，使用的是`String`类型的`equals`方法，所以客户端在编写`Service`接口时，如果返回类型不是八大基本类型 + String 类型，也就是复杂对象类型，那么要重写`toString`方法。

不使用`Object`默认的`toString`方法，因为它默认打印信息为`16`位的内存地址，在做校验中，发送的包和请求获取的包是需要重新实例化的，说白了就是深克隆，**必须** 重写`Object`原有`toString`方法。

为了避免该情况发生，建议所有`PoJo`、`VO`类必须重写`toString`方法，其实就是所有真实业务方法返回类型的实体，必须重写`toString`方法。

如返回体有嵌套复杂对象，所有复杂对象均要重写`toString`只要满足不同对象但内容相同的`toString`方法打印信息一致，数据完整性检测才不会误报。

- RegisterFailedException

抛出异常`Failed to register service Exception`

原因是注册中心没有启动或者注册中心地址端口指定不明，或者因为防火墙问题，导致`Nacos`所在服务器的端口访问失败。

使用该框架时，需注意以下两点：

(1) 支持注册本地地址，如 localhost或127.0.0.1，则注册地址会解析成公网地址；

(2) 支持注册内网地址和外网地址，则地址为对应内网地址或外网地址，不会将其解析；

- NotSuchMethodException
抛出异常`java.lang.NoSuchMethodError:  org.slf4j.spi.LocationAwareLogger.log`

出现该异常的原因依赖包依赖了`jcl-over-slf4j`的`jar`包，与`springboot-starter-log4j`中提供的`jcl-over-slf4j`重复了，建议手动删除`rpc-core-1.0.0-jar-with-dependenceies.jar`中`org.apache.commons`包


- DecoderException

抛出异常：`com.esotericsoftware.kryo.KryoException: Class cannot be created (missing no-arg constructor): java.lang.StackTraceElement`

主要是因为`Kryo`序列化和反序列化是通过无参构造反射创建的，所以使用到`Pojo`类，首先必须对其创建无参构造函数，否则将抛出该异常，并且无法正常执行。

- InvocationTargetException

抛出异常：`Serialization trace:stackTrace (java.lang.reflect.InvocationTargetException)`

主要也是反射调用失败，主要原因还是反射执行目标函数失败，缺少相关函数，可能是构造函数或者其他方法参数问题。

- AnnotationMissingException

抛出异常：`cn.fyupeng.exception.AnnotationMissingException`

由打印信息中可知，追踪`AbstractRpcServer`类信息打印
```ruby
cn.fyupeng.net.AbstractRpcServer [main] - mainClassName: jdk.internal.reflect.DirectMethodHandleAccessor
```
如果`mainClassName`不为`@ServiceScan`注解标记所在类名，则需要到包`cn.fyupeng.util.ReflectUtil`下修改或重写`getStackTrace`方法，将没有过滤的包名加进过滤列表即可，这可能与`JDK`的版本有关。

- OutOfMemoryError

抛出异常`java.lang.OutOfMemoryError: Requested array size exceeds VM limit`

基本不可能会抛出该错误，由于考虑到并发请求，可能导致，如果请求包分包，会出现很多问题，所以每次请求只发送一个请求包，如在应用场景需要发送大数据，比如发表文章等等，需要手动去重写使用的序列化类的`serialize`方法

例如：KryoSerializer可以重写`serialize`方法中写缓存的大小，默认为`4096`，超出该大小会很容易报数组越界异常问题。
```java
/**
 * bufferSize: 缓存大小
 */
Output output = new Output(byteArrayOutputStream,100000))
```

- RetryTimeoutExcepton

抛出异常`cn.fyupeng.exception.AnnotationMissingException`

在启用重试机制后，客户端超过重试次数仍未能成功调用服务，即可认为服务不可用，并抛出超时重试异常。

抛出该异常后，将中断该线程，其线程还未执行的任务将终止，默认不会开启重试机制，则不会抛出该异常。

### 10. 版本追踪

#### 1.0版本

- [ [#1.0.1](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.1/pom) ]：解决真实分布式场景下出现的注册服务找不到的逻辑问题；

- [ [#1.0.2](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.2/pom) ]：解耦注册中心的地址绑定，可到启动器所在工程项目的资源下配置`resource.properties`文件；

- [ [#1.0.4.RELEASE](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.4.RELEASE/pom) ]：修复`Jar`方式部署项目后注册到注册中心的服务未能被发现的问题，解耦`Jar`包启动配置文件的注入，约束名相同会覆盖项目原有配置信息；

- [ [#1.0.5](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.5/pom) ]：将心跳机制打印配置默认级别为`trace`，默认日志级别为`info`，需要开启到`logback.xml`启用。

- [ [#1.0.6](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.6/pom) ]：默认请求包大小为`4096`字节，扩容为`100000`字节，满足日常的`100000`字的数据包，不推荐发送大数据包，如有需求看异常`OutOfMemoryError`说明。

- [ [#1.0.10](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.10/pom) ]: 修复负载均衡出现`select`失败问题，提供配置中心高可用集群节点注入配置、负载均衡配置、容错自动切换

#### 2.0版本

- [ [#2.0.0](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.0/pom) ]：优化1.0版本，2.0版本问世超时重试机制，使用到幂等性来解决业务损失问题，提高业务可靠性。

- [ [#2.0.1](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.1/pom) ]：版本维护

- [ [#2.0.2](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.2/pom) ]：修复集群配置中心宕机重试和负载问题

- [ [#2.0.3](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.3/pom) ]：提供个性化服务版本号，支持各种场景，如测试和正式场景，让服务具有更好的兼容性，支持版本维护和升级。

- [ [#2.0.4](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.4/pom) ]：支持SPI机制，接口与实现解耦。

### 11. 开发说明
有二次开发能力的，可直接对源码修改，最后在工程目录下使用命令`mvn clean package`，可将核心包和依赖包打包到`rpc-netty-framework\rpc-core\target`目录下，本项目为开源项目，如认为对本项目开发者采纳，请在开源后最后追加原创作者`GitHub`链接 https://github.com/fyupeng ，感谢配合！


