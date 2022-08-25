## 介绍
一个分布式微服务RPC框架 | [英文说明文档](README.md)
### 1. 服务提供
- 负载均衡策略
- 序列化策略
- 自动发现和注销服务
- 注册中心
### 2. 安全策略
- 心跳机制
- 信息摘要
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
### 3. IO 异步非阻塞

IO 异步非阻塞 能够让客户端在请求数据时处于阻塞状态，而且能够在请求数据返回时间段里去处理自己感兴趣的事情。

- 原理

使用 java8 出世的 `CompletableFuture` 并发工具类，能够异步处理数据，并在将来需要时获取。

- 实现

数据在服务端与客户端之间的通道 `channel` 中传输，客户端向通道发出请求包，需要等待服务端返回，这时可使用 `CompletableFuture` 作为返回结果，只需让客户端读取到数据后，将结果通过 `complete()`方法将值放进去后，在将来时通过`get()`方法获取结果。

## 快速开始
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
    <version>1.0.4.RELEASE</version>
</dependency>
```

### 2. 启动 Nacos

`-m:模式`，`standalone:单机`

命令使用:

```ruby
startup -m standalone
```

> 注意：开源RPC 默认使用 nacos 指定的本地端口号 8848 

官方文档：https://nacos.io/zh-cn/docs/quick-start.html

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
```

#### 5.2 日志配置

在 `resources` 中加入 `logback.xml`
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

### 7. 异常解决
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

### 8. 版本追踪

#### 1.0版本

[ [#1.0.1](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.1/pom) ]：解决真实分布式场景下出现的注册服务找不到的逻辑问题；

[ [#1.0.2](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.2/pom) ]：解耦注册中心的地址绑定，可到启动器所在工程项目的资源下配置`resource.properties`文件；

[ [#1.0.4.RELEASE](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.4.RELEASE/pom) ]：修`Jar`方式部署项目后注册到注册中心的服务未能被发现的问题，解耦`Jar`包启动配置文件的注入，约束名相同会覆盖项目原有配置信息；

[ [#1.0.5](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.5/pom) ]：将心跳机制打印配置默认级别为`trace`，默认日志级别为`info`，需要开启到`logback.xml`启用。

### 9. 开发说明
有二次开发能力的，可直接对源码修改，最后在工程目录下使用命令`mvn clean package`，可将核心包和依赖包打包到`rpc-netty-framework\rpc-core\target`目录下，本项目为开源项目，如认为对本项目开发者采纳，请在开源后最后追加原创作者`GitHub`链接 https://github.com/fyupeng ，感谢配合！


