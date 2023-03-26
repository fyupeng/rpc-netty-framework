## 介绍

![Version](https://img.shields.io/static/v1?label=Version&message=2.2.3&color=brightgreen)
![Jdk](https://img.shields.io/static/v1?label=JDK&message=8.0&color=green)
![Nacos](https://img.shields.io/static/v1?label=Nacos&message=1.43&color=orange)
![Netty](https://img.shields.io/static/v1?label=Netty&message=4.1.75.Final&color=blueviolet)
![License](https://img.shields.io/static/v1?label=License&message=Apache2&color=blue)

一个分布式微服务RPC框架 | [英文说明文档](/README.md) | [SpringBoot整合RPC](/document/springboot整合rpc-netty-framework.md)

- [x] 基于`Socket`和`Netty`异步非阻塞通信的解决方案；
- [x] 适用基于`Netty`的`IO`密集型场景应用，性能虽不如`CPU`密集型场景应用，但并发性是最好的；
- [x] 支持分布式超时重试机制、幂等历史结果淘汰策略、异步缓存实现高效通信；
- [x] 实现采用`Jedis/Lettuce`两种基于雪花算法的`id`生成器;
- [x] 支持`JDK`内置`SPI`机制，在扩展动态配置、注册器、服务发现和服务提供时，实现无侵入编程；
- [x] 注册中心高可用性，提供集群注册中心，所有注册节点宕机后仍能通过缓存为用户持续提供服务；
- [x] 提供个性化服务，推出个性化服务`name`、服务`group`，适合在测试、实验和正式环境的服务，以及为后期版本的兼容、维护和升级提供更好的服务；
- [ ] 提供集群注册中心宕机重启服务；
- [x] 提供服务无限制横向扩展；
- [x] 提供服务的两种负载均衡策略，如随机和轮询负载；
- [x] 提供请求超时重试，且保障业务执行的幂等性，超时重试能降低线程池任务的延迟，线程池保障了高并发场景下线程数创建数量的稳定，却因而带来延迟问题，处理该问题可以启用重试请求，且重试达到阈值将放弃请求，认为该服务暂时不可用，造成业务损耗，请慎用；
- [x] 提供自定义注解扩展服务，使用代理扩展，能无侵入式扩展个性化服务；
- [x] 提供可扩展的序列化服务，目前提供`Kryo`和`Jackson`两种序列化方式；
- [x] 提供日志框架`Logback`；
- [x] 提供Netty可扩展的通信协议，通信协议头使用与Class一样的16位魔数`0xCAFEBABE`、包辨识id，用来辨识请求包和响应包、`res`长度，用来防止粘包，以及最后的`res`，内部加入检验码和唯一识别id，让服务器能高效地同时处理多个不同请求包或重发请求包，以及包校验；
- [ ] 支持秒级时钟回拨服务端采取主动屏蔽客户端请求策略、分级以上时钟回拨服务端采取主动下线策略；
- [x] 配合超时重试机制对劫持包采用沉默和重发处理，加强通信的安全性。
- [x] 支持服务端单向延时关闭处理请求。
- [x] 支持扩展`Bean`自定义实例化，满足第三方框架如`Spring`依赖注入、切面执行。

### 1. 服务提供
- 负载均衡策略
- 序列化策略
- 自动发现和注销服务
- 注册中心
- 单机与集群

---- 

### 2. 安全策略
- 心跳机制
- 信息摘要
- 超时重试机制
- 幂等性
- 雪花算法
- 健壮性

---- 

### 3. 设计模式
- 单例模式
- 动态代理
- 静态工厂
- 建造者
- 策略模式
- Future(观察者）

---- 

## 亮点

- [信息摘要算法的应用](/document/zh/亮点.md#1-信息摘要算法的应用)
- [心跳机制](/document/zh/亮点.md#2-心跳机制)
- [SPI机制](/document/zh/亮点.md#3-SPI-机制)
- [IO 异步非阻塞](/document/zh/亮点.md#4-IO-异步非阻塞)
- [RNF 协议](/document/zh/亮点.md#5-RNF-协议)
- [场景应用](/document/zh/亮点.md#6-场景应用)
- [高可用集群](/document/zh/亮点.md#7-高可用集群)
- [超时重试机制](/document/zh/亮点.md#8-超时重试机制)
- [雪花算法](/document/zh/亮点.md#9-雪花算法)
- [高并发](/document/zh/亮点.md#10-高并发)

## 快速开始

[版本 (v1.0.0 - 2.0.9)](/document/zh/快速开始v1.0.0-2.0.9.md)<br/>
[版本 (v2.1.0 - 2.x.x)](/document/zh/快速开始v2.1.0-2.x.x.md)

---- 

---- 

### 12. 异常解决
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

- InvalidSystemClockException

抛出异常`cn.fyupeng.idworker.exception.InvalidSystemClockException`

雪花算法生成中是有很小概率出现时钟回拨，时间回拨需要解决`id`值重复的问题，故而有可能抛出`InvalidSystemClockException`中断异常，逻辑不可处理异常。

- WorkerIdCantApplyException

抛出异常`cn.fyupeng.idworker.exception.WorkerIdCantApplyException`

雪花算法生成中，借助`IdWorker`生成器生成分布式唯一`id`时，是借助了机器码，当机器码数量生成达到最大值将不可再申请，这时将抛出中断异常`WorkerIdCantApplyException`。

- NoSuchMethodError

抛出异常`io.netty.resolver.dns.DnsNameResolverBuilder.socketChannelType(Ljava/lang/Class;)Lio/netty/resolver/dns/DnsNameResolverBuilder`

整合`SpringBoot`时会覆盖`netty`依赖和`lettuce`依赖，`SpringBoot2.1.2`之前，内含`netty`版本较低，而且`RPC`框架支持兼容`netty-all:4.1.52.Final`及以上，推荐使用`SpringBoot2.3.4.RELEASE`即以上可解决该问题。

- AsyncTimeUnreasonableException

抛出异常`cn.fyupeng.exception.AsyncTimeUnreasonableException`

异步时间设置不合理异常，使用注解@Reference时，字段`asyncTime`必须大于`timeout`，这样才能保证超时时间内不会报异常`java.util.concurrent.TimeoutException`，否则最大超时时间将可能不可达并且会打印`warn`日志，导致触发下一次重试，该做法在`2.0.6`和`2.1.8`中将强制抛出异常终止线程。

与此相同用法的有`RetryTimeoutExcepton`和`RpcTransmissionException`，都会终结任务执行。

- RpcTransmissionException

抛出异常`cn.fyupeng.exception.RpcTransmissionException`

数据传输异常，在协议层解码中抛出的异常，一般是因为解析前的实现类与解析后接收实习类`toString()`方法协议不同导致的，也可能是包被劫持并且发生内容篡改。

内部设计采用`toSring()`方法来，而不进行某一种固定的方式来校验，这让校验有更大的不确定性，以此获得更高的传输安全，当然这种设计可以让开发人员自行设计具有安全性的`toString`方法实现，如不实现，将继承`Object`的内存地址toString打印，由于是通过网络序列化传输的，也就是深克隆方式创建类，服务端的原校验码和待校验一般不同，就会抛该异常，一般都需要重新`toString()`方法。

---- 

### 13. 健壮性（善后工作）

服务端的延时关闭善后工作，能够保证连接的正常关闭。

- TCP 关闭（四次挥手）

```shell
8191	80.172711	192.168.2.185	192.168.2.185	TCP	44	8085 → 52700 [FIN, ACK] Seq=3290 Ack=3566 Win=2616320 Len=0
8190	80.172110	192.168.2.185	192.168.2.185	TCP	44	8085 → 52700 [ACK] Seq=3290 Ack=3566 Win=2616320 Len=0
8191	80.172711	192.168.2.185	192.168.2.185	TCP	44	8085 → 52700 [FIN, ACK] Seq=3290 Ack=3566 Win=2616320 Len=0
8192	80.172751	192.168.2.185	192.168.2.185	TCP	44	52700 → 8085 [ACK] Seq=3566 Ack=3291 Win=2616320 Len=0
```

而且不再出现发送`RST`问题，即接收缓冲区中还有数据未接收，出现的原因为`Netty`自身善后工作出现了问题，即在`future.channel().closeFuture().sync()`该操作执行后，线程终止不会往下执行，即时有`finally`依旧如此，于是使用关闭钩子来自动调用完成连接的正常关闭。

- 关闭钩子

```java
public class ServerShutdownHook {

  private static final ServerShutdownHook shutdownHook = new ServerShutdownHook();
  private ServiceRegistry serviceRegistry;
  private RpcServer rpcServer;

  public ServerShutdownHook addRegistry(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
    return this;
  }

  public ServerShutdownHook addServer(RpcServer rpcServer) {
    this.rpcServer = rpcServer;
    return this;
  }

  public static ServerShutdownHook getShutdownHook() {
    return shutdownHook;
  }
  /**
   * 添加清除钩子
   * 开启 子线程的方式 帮助 gc
   */
  public void addClearAllHook() {
    log.info("All services will be cancel after shutdown");
    Runtime.getRuntime().addShutdownHook(new Thread(()->{
      JRedisHelper.remWorkerId(IpUtils.getPubIpAddr());
      log.info("the cache for workId has bean cleared successfully");
      //NacosUtils.clearRegistry();
      if (serviceRegistry != null) {
        serviceRegistry.clearRegistry();
      }
      //NettyServer.shutdownAll();
      // 开启子线程（非守护线程） 的方式能够 避免因服务器 关闭导致 关闭钩子 未能正常执行完毕（守护线程）
      if(rpcServer != null) {
        rpcServer.shutdown();
      }
    }));
  }
}
```

- 使用方法

在服务端或者客户端代理启动时调用

```java
public class NettyServer extends AbstractRpcServer {
    
  @Override
  public void start() {
    /**
     *  封装了 之前 使用的 线程吃 和 任务队列
     *  实现了 ExecutorService 接口
     */
    ServerShutdownHook.getShutdownHook()
            .addServer(this)
            .addRegistry(serviceRegistry)
            .addClearAllHook();
  }
}
```

Netty已经提供了优雅关闭，即`bossGroup.shutdownGracefully().sync()`，可将其用静态方法封装起来，交由钩子调用即可。

---- 

### 14. 版本追踪

#### 1.0版本

- [ [#1.0.1](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.1/pom) ]：解决真实分布式场景下出现的注册服务找不到的逻辑问题；

- [ [#1.0.2](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.2/pom) ]：解耦注册中心的地址绑定，可到启动器所在工程项目的资源下配置`resource.properties`文件；

- [ [#1.0.4.RELEASE](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.4.RELEASE/pom) ]：修复`Jar`方式部署项目后注册到注册中心的服务未能被发现的问题，解耦`Jar`包启动配置文件的注入，约束名相同会覆盖项目原有配置信息；

- [ [#1.0.5](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.5/pom) ]：将心跳机制打印配置默认级别为`trace`，默认日志级别为`info`，需要开启到`logback.xml`启用。

- [ [#1.0.6](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.6/pom) ]：默认请求包大小为`4096`字节，扩容为`100000`字节，满足日常的`100000`字的数据包，不推荐发送大数据包，如有需求看异常`OutOfMemoryError`说明。

- [ [#1.0.10](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.10/pom) ]: 修复负载均衡出现`select`失败问题，提供配置中心高可用集群节点注入配置、负载均衡配置、容错自动切换

#### 2.0版本

- [ [#2.0.0](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.0/pom) ]：优化`1.0`版本，`2.0`版本问世超时重试机制，使用到幂等性来解决业务损失问题，提高业务可靠性。

- [ [#2.0.1](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.1/pom) ]：版本维护

- [ [#2.0.2](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.2/pom) ]：修复集群配置中心宕机重试和负载问题

- [ [#2.0.3](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.3/pom) ]：提供个性化服务版本号，支持各种场景，如测试和正式场景，让服务具有更好的兼容性，支持版本维护和升级。

- [ [#2.0.4](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.4/pom) ]：支持`SPI`机制，接口与实现解耦。

- [ [#2.0.5](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.5/pom) ]：`2.0`将长期维护，`2.1`版本中继承`2.0`待解决的问题得到同步解决。

- [ [#2.0.6](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.6/pom) ]：整体整改和性能优化。

- [ [#2.0.8](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.8/pom) ]: 代码逻辑优化以及预加载优化。

- [ [#2.0.9](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.9/pom) ]：修复客户端/服务端未能正常关闭问题，导致对端连接异常终止。

#### 2.1版本

- [ [#2.1.0](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.1.0/pom) ]：引入雪花算法与分布式缓存，`2.0.0`版本仅支持单机幂等性，修复分布式场景失效问题，采用`轮询负载+超时机制`，能高效解决服务超时问题。

- [ [#2.1.1](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.1.1/pom) ]：更改配置信息`cn.fyupeng.client-async`为`cn.fyupeng.server-async`。

- [ [#2.1.3](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.1.3/pom) ]：修复公网获取`403`异常。

- [ [#2.1.5](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.1.5/pom) ]：修复注册中心`group`默认缺省报错异常。

- [ [#2.1.7](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.1.7/pom) ]：修复保存文章正常，读取文章超出边界异常问题、解决防火墙下`netty`无法监听阿里云、腾讯云本地公网地址问题、修复查询为空/无返回值序列化逻辑异常问题、修复分布式缓存特情况下出现的序列化异常现象。

- [ [#2.1.8](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.1.8/pom) ]：整体整改和性能优化。

- [ [#2.1.9](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.1.9/pom) ]：代码逻辑优化以及预加载优化。

- [ [#2.1.10](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.1.10/pom) ]：修复客户端/服务端未能正常关闭问题，导致对端连接异常终止、整合统一的业务线程池，以便后期清理工作。

#### 2.2版本

- [ [#2.2.0](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.2.0/pom) ]：支持 扩展 `Bean` 自定义实例化，满足 `Spring` 注解依赖注入和注解切面执行需求、修正注解包名为`annotation`、解决服务注销时未能手动清除服务实例内存、性能小幅度提升、维护并发下轮询策略的不稳定性。

- [ [#2.2.1](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.2.1/pom) ]：代码优化、性能优化、改进时钟回拨策略、支持第三方服务注册与发现功能的SPI扩展、支持负载均衡获取泛型服务。

- [ [#2.2.2](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.2.2/pom) ]：解决单例工厂失效问题、优化线程池工厂、优化冗余配置。

- [ [#2.2.3](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.2.3/pom) ]：将自定义协议16字节改为8字节、优化超时重试机制，修复高并发下存在的线程安全问题、大幅度提高性能。


---- 

### 15. 开发说明

RNF 开源 RPC 框架已遵循 Apache License Version 2.0 协议，使用前请遵循以上协议，如有版权纠纷或使用不便请与作者联系！


