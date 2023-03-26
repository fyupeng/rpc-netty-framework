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

[1. 信息摘要算法的应用](/document/zh/亮点.md#1-信息摘要算法的应用)<br/>
[2. 心跳机制](/document/zh/亮点.md#2-心跳机制)<br/>
[3. SPI机制](/document/zh/亮点.md#3-SPI-机制)<br/>
[4. IO 异步非阻塞](/document/zh/亮点.md#4-IO-异步非阻塞)<br/>
[5. RNF 协议](/document/zh/亮点.md#5-RNF-协议)<br/>
[6. 场景应用](/document/zh/亮点.md#6-场景应用)<br/>
[7.高可用集群](/document/zh/亮点.md#7-高可用集群)<br/>
[8 超时重试机制](/document/zh/亮点.md#8-超时重试机制)<br/>
[9. 雪花算法](/document/zh/亮点.md#9-雪花算法)<br/>
[10. 高并发](/document/zh/亮点.md#10-高并发)<br/>
[11. 健壮性](/document/zh/亮点.md#11-健壮性)

## 快速开始

- [1. 版本 (v1.0.0 - 2.0.9)](/document/zh/快速开始v1.0.0-2.0.9.md)<br/>
- [2. 版本 (v2.1.0 - 2.x.x)](/document/zh/快速开始v2.1.0-2.x.x.md)

---- 

---- 

## 异常解决
- [ServiceNotFoundException](/document/zh/异常解决.md#1-ServiceNotFoundException)

- [ReceiveResponseException](/document/zh/异常解决.md#2-ReceiveResponseException)

- [NotSuchMethodException](/document/zh/异常解决.md#3-RegisterFailedException)

- [NotSuchMethodException](/document/zh/异常解决.md#4-NotSuchMethodException)

- [DecoderException](/document/zh/异常解决.md#5-DecoderException)

- [AnnotationMissingException](/document/zh/异常解决.md#6-InvocationTargetException)

- [AnnotationMissingException](/document/zh/异常解决.md#7-AnnotationMissingException)

- [OutOfMemoryError](/document/zh/异常解决.md#8-OutOfMemoryError)

- [RetryTimeoutExcepton](/document/zh/异常解决.md#9-RetryTimeoutExcepton)

- [InvalidSystemClockException](/document/zh/异常解决.md#10-InvalidSystemClockException)

- [WorkerIdCantApplyException](/document/zh/异常解决.md#11-WorkerIdCantApplyException)

- [NoSuchMethodError](/document/zh/异常解决.md#12-NoSuchMethodError)

- [AsyncTimeUnreasonableException](/document/zh/异常解决.md#13-AsyncTimeUnreasonableException)

- [RpcTransmissionException](/document/zh/异常解决.md#14-RpcTransmissionException)

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


