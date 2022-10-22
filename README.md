## Introduction

![Version](https://img.shields.io/static/v1?label=VERSION&message=2.0.0&color=brightgreen)
![Jdk](https://img.shields.io/static/v1?label=JDK&message=8.0&color=green)
![Nacos](https://img.shields.io/static/v1?label=NACOS&message=1.43&color=orange)
![Netty](https://img.shields.io/static/v1?label=NETTY&message=4.1.20.Final&color=blueviolet)
![Version](https://img.shields.io/static/v1?label=LICENCE&message=MIT&color=brightgreen)

A Distributed Microservice RPC Framework | [Chinese Documentation](README.CN.md) | [SpringBoot conformity RPC](springboot整合rpc-netty-framework.md)
### 1. service provisioning
- Load Balancing Policy
- Serialization policy
- Auto-discovery and logout services
- Registration Center
- Standalone and Cluster
### 2. Security Policies
- Heartbeat mechanism
- Message digest
- Timeout retry mechanism
- Idempotency
### 3. Design Patterns
- Singleton Pattern
- Dynamic Proxy
- Static Factory
- Builder
- Strategy Mode
- Future (Observer)
## Highlights
### 1. Application of Information Digest Algorithm
The use of the information digest algorithm is actually not difficult. It can be achieved by adding a member variable `checkCode` of type `String` to the data packet for anti-counterfeiting.

- Principle

The sender encrypts the original message into a digest with the `HASH` function, and then sends the digital digest together with the original message to the receiver. The receiver also uses the HASH function to encrypt the original message into a digest to see if the two digests are the same. Indicates that the information is complete. Otherwise, it is incomplete.

- accomplish

When the client sends a request packet, the server will use MD5 one-way encryption to become a unique information digest (128 bits, 16 bytes) after the content of the request execution result is converted into bytecode and stored in the response packet. The corresponding member variable `checkCode`, so after the client gets the response packet, the most valuable place (the result of the request to be executed is changed), then the `checkCode` will not guarantee consistency, which is the principle of the information summary application.

**Security Enhancements**

Considering that this is only for the consistency of results returned by customer requirements, and does not ensure that the same request content exists between request packets, the request `id` is introduced.

Each packet will generate a unique `requestId`. After the request packet is sent, the packet can only be accepted by the client that sent the request. Even if one of the two places is maliciously changed by the other party, the client will report an error and discard the received The response packet will not be unpacked and returned to the user.

If not only the returned result is changed, but both the result and the message digest are modified, it is difficult for the other party to ensure that the modified content is the same as the modified message digest after encryption, because it is necessary to ensure the consistent data transmission protocol and data encoding and decoding.
### 2. Heartbeat mechanism

The `RPC` of the heartbeat mechanism is widely used. The implementation of the heartbeat mechanism in this project is very simple, and the countermeasure is to force the server to disconnect. Of course, some `RPC` frameworks implement the server to actively try to reconnect.

- Principle

For the application of the heartbeat mechanism, a `handler` processor in the `Netty` framework is actually used. Through the handler, the heartbeat packet is sent regularly to let the server know that the client remains active.

- Accomplish

Utilize the `IdleStateEvent` event listener in the `Netty` framework, rewrite the `userEventTriggered()` method, listen for read operations on the server side, read client write operations, monitor write operations on the client side, and monitor whether it is still there Activity, that is, whether there is a request sent to the server.

If the client does not actively disconnect the connection with the server, but continues to maintain the connection, then after the client's write operation times out, that is, the client's listener listens to the client to make a write operation event within the specified time, then at this time The client processor actively sends a heartbeat packet to the server to ensure that the client allows the server to ensure that it remains active.

### 3. IO asynchronous non-blocking

IO asynchronous non-blocking allows the client to be in a blocking state when requesting data, and can process things of interest during the time period when the requested data is returned.

- Principle

Using the `CompletableFuture` concurrency tool class born in java8, it can process data asynchronously and obtain it when needed in the future.

- accomplish

The data is transmitted in the channel `channel` between the server and the client. The client sends a request packet to the channel and needs to wait for the server to return. In this case, you can use `CompletableFuture` as the return result, just let the client read the After the data, the result is put in the value through the `complete()` method, and the result is obtained through the `get()` method in the future.

### 4. RNF Protocol
```java
/**
     * custom object header protocol 16 bytes
     * 4 bytes magic number
     * 4 bytes protocol packet type
     * 4 bytes serialized type
     * 4 bytes data length
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

## Quick Start

### 1.Dependences

#### 1.1 Direct Import

First import  two jar package files `rpc-core-1.0.0.jar` and `rpc-core-1.0.0-jar-with-dependencies.jar`

The `jar` package includes bytecode files and `java` source code. After introduction, `class` and `sources` will be automatically imported together. The source code can be used as a reference

![dependencies](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/依赖.png)

#### 1.2 Maven Import

Import the following `maven` will also import the dependencies of `rpc-common` and the default registry center framework `nacos-client`

```xml
<dependency>
  <groupId>cn.fyupeng</groupId
  <artifactId>rpc-core</artifactId>
  <version>1.0.10</version>
</dependency>
```
Ali repository in October began in the system upgrade, some versions have not been synchronized, recommend another `maven` official repository
```xml
<mirror>
  <id>repo1maven</id>
  <mirrorOf>*</mirrorOf>
  <name>maven public repository</name
  <url>https://repo1.maven.org/maven2</url
</mirror>
```

### 2. Start Nacos 

`-m:模式`，`standalone:单机`

命令使用:

```ruby
startup -m standalone
```

> Note: Open source RPC uses the local port number 8848 specified by nacos by default

Official Documentation：https://nacos.io/zh-cn/docs/quick-start.html

Nacos start effect：

![effect](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/nacos.png)

### 3. Provide Interface
```java
public interface HelloService {
    String sayHello(String message);
}
```
### 4. Start Server
- Real Service
```java
@Service
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String message) {
        return "hello, here is service！";
    }
}
```
- Service Launcher
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
> Note: Add the annotations `cn.fyupeng.Service` and `cn.fyupeng.ServiceScan` to be scanned by the automatic discovery service and registered to nacos

### 5. Start Client
There are two ways to connect to the server when initializing the client:

- Direct connection

- use load balancing
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

- Project mode start

Add resource.properties to resources

The hostname is specified using `localhost` or `127.0.0.1`

````properties

cn.fyupeng.nacos.register-addr=localhost:8848

````

- `Jar` way to start

, Compatible with `springboot` external startup configuration file injection, you need to create a `config` folder in the same directory as the `Jar` package, and inject the configuration file in `config` like `springboot`, but the configuration injected by `springboot` The file default constraint name is `application.properties`, and the `rpc-netty-framework` default constraint name is `resource.properties`.

The currently injectable configuration information is:

````properties

cn.fyupeng.nacos.register-addr=localhost:8848
cn.fyupeng.nacos.cluster.use=true
cn.fyupeng.nacos.cluster.load-balancer=random
cn.fyupeng.nacos.cluster.nodes=192.168.10.1:8847,192.168.10.1:8848,192.168.10.1:8849
````

#### 5.2 Log configuration

Add `logback.xml` to `resources`
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
In addition, the framework also provides Rpc services in `Socket` mode

### 6. Application scenarios

- Support springBoot integration

In order to support `springBoot` to integrate `logback` logs, inherit `rpc-netty-framework` to use the same set of logs, abandon the built-in `slf4j-api` of `nacos-client` and the original `Jar` package of `commons-loging`, Because this framework will lead to repeated log binding and log printing method parameter compatibility problems when integrating `springboot`, this problem can be solved by using `jcl-over-slf4j-api`;

In `springboot1.0` and `2.0` versions, instead of using its default version of `spring-boot-starter-log4j`, it is recommended to use `1.3.8.RELEASE`;

The simple configuration of springboot is as follows
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
        <exclusions>
            <!-- Exclude springboot's default logback logging framework -->
            <exclusion>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-logging</artifactId>
            </exclusion>
            <!-- Exclude the default commons-logging implementation of springboot (the version is low, and there is a problem that the method cannot be found) -->
            <exclusion>
                <groupId>org.springframework</groupId>
                <artifactId>spring-jcl</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    
    <!-- Integration with logback (available via @Slf4j annotation) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.10</version>
    </dependency>
    <!-- import log4j log dependency, the purpose is to use jcl-over-slf4j to rewrite the implementation of commons logging -->
    <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j</artifactId>
    <version>1.3.8.RELEASE</version>
    </dependency>
</dependencies>

```

### 7. Highly available clusters
```properties
cn.fyupeng.nacos.cluster.use=true
cn.fyupeng.nacos.cluster.load-balancer=random
cn.fyupeng.nacos.cluster.nodes=192.168.10.1:8847,192.168.10.1:8848,192.168.10.1:8849
```
- Usage and Points to Note

  By default, `cn.fyupeng.nacos.cluster.use` obeys the convention better than the configuration setting, and the default value is `false`, indicating that the cluster is not opened by default
  `cn.fyupeng.nacos.cluster.load-balancer` and `cn.fyupeng.nacos.cluster.nodes` must be turned on in cluster mode before they can be used.
- Cluster node load policy
  - `random`: random policy
  - `round`: polling policy

- Cluster node fault-tolerant switching
  - Node downtime: When a node is down, it will re-elect a new normal node from the node configuration list, otherwise it will retry infinitely

Cluster nodes are theoretically infinitely scalable and can be extended using the separator `[;,|]`.

### 8. Timeout retry mechanism
The retry mechanism is not used by default, in order to ensure the correctness of the service, because there is no guarantee of idempotency.

The reason is that the client cannot detect whether there is a problem in the client's network transmission or a problem in the server's network transmission on the way back after receiving correctly, because if the former is the case, then retrying can guarantee idempotency, but if the latter is the case, it may lead to multiple executions of the same service, which is a non-consistent result for the client.

Timeout retry processing can lead to idempotency problem, so we use `HashSet` to add request `id` to do timeout processing in the server:
- Timeout retry: `cn.fyupeng.anotion.Reference` annotation provides three configuration parameters: retry count, timeout time and asynchronous time, where
- Number of retries: the number of times the server fails to respond within the timeout period and is allowed to trigger a timeout
- Timeout time: the maximum time allowed for the client to wait for the server, and the timeout triggers the retry mechanism
- Asynchronous time: the time to wait for the asynchronous response from the server, and can only be used in the timeout retry mechanism, the default use of non-timeout retry blocking wait mode

> For Example:
```java
private static RandomLoadBalancer randomLoadBalancer = new RandomLoadBalancer();
    private static NettyClient nettyClient = new NettyClient(randomLoadBalancer, CommonSerializer.KRYO_SERIALIZER);
    private static RpcClientProxy rpcClientProxy = new RpcClientProxy(nettyClient);

    @Reference(retries = 2, timeout = 1000, asyncTime = 3000)
    private static HelloWorldService service = rpcClientProxy.getProxy(HelloWorldService.class, Client.class);
```
The implementation of retry is not difficult either, just use `proxy + for + arguments` to implement it.
> Core code implementations:
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
- Idempotency

The retry mechanism server has to guarantee the principle of executing the retry package once, that is, to achieve idempotency
  
Implementation idea: you need to use `HashSet` and `HashMap` to store the request `id` of the retry request package and the execution result of the last request, how to determine whether to retry can be added by the `add` method of the `Set` collection, add failure is the retry package, the request `id` corresponding to the last request should return the result to the client, the last step is to do Garbage cleanup.
  
Because of concurrency considerations, garbage disposal using double-check lock, that is, through the `if` judgment `Set` threshold and `synchronized` keyword used in conjunction to achieve.

> Core code implementations:
```java
/**
             * 这里要防止重试
             * 分为两种情况
             * 1. 如果是 客户端发送给服务端 途中出现问题，请求包之前 服务器未获取到，也就是 唯一请求id号 没有重复
             * 2. 如果是 服务端发回客户端途中出现问题，导致客户端触发 超时重试，这时服务端会 接收 重试请求包，也就是有 重复请求id号
             */
            // 请求id 为第一次请求 id
            Object result = null;
            if (timeoutRetryRequestIdSet.add(msg.getRequestId())) {
                result = requestHandler.handler(msg);
                resMap.put(msg.getRequestId(), result);
            //请求id 为第二次或以上请求
            } else {
                result = resMap.get(msg.getRequestId());
            }
```

### 9. exception resolution
- ServiceNotFoundException

Throws exception `ServiceNotFoundException`

Stack information: `service instances size is zero, can't provide service! please start server first!`

Under normal circumstances, general errors can be resolved from the error report.

Solve the situation that the real service does not exist, resulting in an abnormal situation in the strategy used in the load balancing. After the repair, a `ServiceNotFoundException` will be forced to be thrown. Perhaps the service is not started in most cases.

Of course, it is recommended that the real service should be in the inner package of the service launcher, the same layer may not work.

Unless an annotation is used to indicate the package name `@ServiceScan("com.fyupeng")`

In other cases, if there is no response from the server, and the service has been successfully registered in the registry, then you have to check whether the package names of the interface named in the server and the client are consistent. If they are inconsistent, the service cannot be automatically discovered. The registry found that the most common error is `service instances size is zero`.
- ReceiveResponseException

Throws exception `data in package is modified Exception`

The implementation of the information digest algorithm uses the `equals` method of the `String` type, so when the client writes the `Service` interface, if the return type is not the eight basic types + String type, that is, the complex object type, then the Write the `toString` method.

Do not use the default `toString` method of `Object`, because it prints the information as a `16`-bit memory address by default. During verification, the sent packet and the requested packet need to be re-instantiated. To put it bluntly, it is Deep clone, **must** override the original `toString` method of `Object`.

In order to avoid this situation, it is recommended that all `PoJo` and `VO` classes must rewrite the `toString` method. In fact, all entities of the return type of the real business methods must rewrite the `toString` method.

If the return body has nested complex objects, all complex objects must rewrite `toString`. As long as the `toString` method of different objects but the same content prints the same information, the data integrity detection will not be false.

- RegisterFailedException

Throws an exception `Failed to register service Exception`

The reason is that the registry is not started or the address and port of the registry are not specified, or the port access of the server where `Nacos` is located fails due to a firewall problem.

When using the framework, the following two points should be noted.

(1) support the registration of local addresses, such as localhost or 127.0.0.1, then the registered address will be resolved into a public address.

(2) support the registration of intranet addresses and extranet addresses, then the address is the corresponding intranet address or extranet address, and will not resolve them.

- NotSuchMethodException

Throws exception `java.lang.NoSuchMethodError: org.slf4j.spi.LocationAwareLogger.log`

The reason for this exception is that the dependency package depends on the `jar` package of `jcl-over-slf4j`, which is the same as the `jcl-over-slf4j` provided in `springboot-starter-log4j`. It is recommended to manually delete `rpc-core` -1.0.0-jar-with-dependenceies.jar in the `org.apache.commons` package

- DecoderException

Throws exception: `com.esotericsoftware.kryo.KryoException: Class cannot be created (missing no-arg constructor): java.lang.StackTraceElement`

Mainly because `Kryo` serialization and deserialization are created by reflection with no parameter construction, so when using the `Pojo` class, you must first create a parameterless constructor for it, otherwise the exception will be thrown and cannot be executed normally .

- InvocationTargetException

Throws exception: `Serialization trace:stackTrace (java.lang.reflect.InvocationTargetException)`

The main reason is that the reflection call fails. The main reason is that the reflection execution target function fails, and the related functions are missing. It may be a problem with the constructor or other method parameters.

- AnnotationMissingException

Throwing exception: `cn.fyupeng.exception.AnnotationMissingException`

As can be seen from the printing information, trace the printing of the `AbstractRpcServer` class information
````ruby
cn.fyupeng.net.AbstractRpcServer [main] - mainClassName: jdk.internal.reflect.DirectMethodHandleAccessor
````
If `mainClassName` is not the class name of the `@ServiceScan` annotation mark, you need to modify or rewrite the `getStackTrace` method under the package `cn.fyupeng.util.ReflectUtil`, and add the unfiltered package name to the filter list. Yes, it may be related to the version of `JDK`.

- OutOfMemoryError

Throws exception `java.lang.OutOfMemoryError: Requested array size exceeds VM limit`

It is basically impossible to throw this error. Considering concurrent requests, it may cause many problems if the request package is subpackaged, so only one request package is sent per request. For example, in application scenarios, large data needs to be sent, such as publishing Articles, etc., need to manually override the `serialize` method of the serialization class used.

For example: KryoSerializer can override the size of the write cache in the `serialize` method. The default value is `4096`. Exceeding this size will easily report an array out-of-bounds exception.
````java
/**
* bufferSize: buffer size
*/
Output output = new Output(byteArrayOutputStream,100000))
````

- RetryTimeoutExcepton

Throw exception `cn.fyupeng.exception.AnnotationMissingException`.

After the retry mechanism is enabled, if the client fails to invoke the service after the number of retries, the service is considered unavailable and a timeout retry exception is thrown.

After the exception is thrown, the thread will be interrupted and the tasks not yet executed by the thread will be terminated, and if the retry mechanism is not enabled by default, the exception will not be thrown.

### 10. Version Tracking

#### Version 1.0

- [ [#1.0.1](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.1/pom) ]: Solve the problem that the registration service cannot be found in real distributed scenarios logical problem;

- [ [#1.0.2](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.2/pom) ]: Decoupled registry address binding, available to the launcher Configure the `resource.properties` file under the resources of the project where you are located;

- [ [#1.0.4.RELEASE](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.4.RELEASE/pom) ]: Fixing the problem that services registered to the registry are not discovered after deploying the project in the `Jar` way, decoupling the injection of the `Jar` package startup configuration file, where the same constraint name will overwrite the original configuration information of the project.

- [ [#1.0.5](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.5/pom) ]: The default level of the heartbeat mechanism printing configuration is `trace`, and the default log level is `info`, which needs to be enabled in `logback.xml`.

- [ [#1.0.6](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.6/pom) ]: The default request packet size is `4096` Bytes, and the expansion is `100000` Bytes, meet the daily `100000` word data packets, it is not recommended to send large data packets, if necessary, see the exception `OutOfMemoryError` description.

- [ [#1.0.10](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.10/pom) ]: Repair the problem of `select` failure in load balancing, provide configuration center highly available cluster node injection configuration, load balancing configuration, fault-tolerant automatic switching

#### version 2.0
- [ [#2.0.0](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.0/pom) ]: Optimized version 1.0 version, version 2.0 introduced timeout retry mechanism, using to idempotency to solve the business loss problem and improve business reliability.

- [ [#2.0.1](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.1/pom )]: Version maintenance

- [ [#2.0.2](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.2/pom )]: Repair the problems of downtime retry and load in the cluster configuration center

### 10. Development Notes

If you have secondary development ability, you can directly modify the source code, and finally use the command `mvn clean package` in the project directory to package the core package and dependency package to the `rpc-netty-framework\rpc-core\target` directory , this project is an open source project, if you think it will be adopted by the developers of this project, please add the original author `GitHub` link https://github.com/fyupeng after the open source, thank you for your cooperation!

