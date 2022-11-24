## Introduction

![Version](https://img.shields.io/static/v1?label=VERSION&message=2.1.0&color=brightgreen)
![Jdk](https://img.shields.io/static/v1?label=JDK&message=8.0&color=green)
![Nacos](https://img.shields.io/static/v1?label=NACOS&message=1.43&color=orange)
![Netty](https://img.shields.io/static/v1?label=NETTY&message=4.1.75.Final&color=blueviolet)
![Version](https://img.shields.io/static/v1?label=LICENCE&message=MIT&color=brightgreen)

A Distributed Microservice RPC Framework | [Chinese Documentation](README.CN.md) | [SpringBoot conformity RPC](springboot整合rpc-netty-framework.md)

- [x] Solutions based on `Socket` and `Netty` asynchronous non-blocking communication.
- [x] support distributed timeout retry mechanism, idempotent historical result elimination strategy, asynchronous caching for efficient communication.
- [x] Implementation of `id` generator using `Jedis/Lettuce` two snowflake-based algorithms;
- [x] Support for the `JDK` built-in `SPI` mechanism for decoupling interfaces from implementations.
- [x] registry high availability, providing clustered registries that can continue to serve users through caching even after all registered nodes are down.
- [x] Providing personalized services, introducing personalized service `name`, service `group`, suitable in test, experimental and formal environments, as well as providing better services for compatibility, maintenance and upgrading of later versions.
- [ ] Provide cluster registry downtime restart service.
- [x] Providing unlimited horizontal scaling of the service.
- [x] provide two load balancing policies for the service, such as random and polled load.
- [ ] provide request timeout retry and guarantee the idempotency of business execution, timeout retry can reduce the delay of thread pool tasks, thread pool guarantees the stability of the number of threads created under high concurrency scenarios, but thus brings delay problems, deal with the problem can be enabled retry requests, and retry reaches the threshold will abandon the request, consider the service temporarily unavailable, resulting in business loss, please use with caution.
- [ ] provide custom annotated extensions to the service, using proxy extensions that can non-intrusively extend personalized services.
- [x] provide scalable serialization services, currently providing `Kryo` and `Jackson` two serialization methods .
- [x] provide a logging framework `Logback`.
- [x] provides Netty extensible communication protocol, the communication protocol header uses the same 16-bit magic number `0xCAFEBABE` as Class, packet identification id to identify request and response packets, `res` length to prevent sticky packets, and finally `res`, which internally adds a check digit and a unique identification id to allow the server to efficiently handle multiple different request packets or resend request packets at the same time, and packet validation.

Architecture Diagram

- Retry mechanism architecture diagram

![超时重试.png](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/超时重试.png)


- Service Discovery and Registration Architecture Diagram

![服务发现与注册.png](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/服务发现与注册.png)


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

### 3. `SPI` mechanism
Resource directory `META-INF/services` under the new interface fully qualified name as a file name, the contents of the implementation class fully qualified name, support `JDK` built-in `SPI`.

The essence to create instances through reflection to create instances without parameters, if the constructor involves the injection of members through parameters, then the interface can be converted to an abstract class, the abstract class exposes the set method to allow subclasses to override, thus indirectly achieve injection.

This mechanism separates the implementation of the interface when the registry logic layer handles service discovery and registration to the configuration file `META-INF/services`, thus better supporting other plugins such as `Zookeeper`, `Eureka` extensions.

Configuration files applied to.
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
### 4. IO asynchronous non-blocking

IO asynchronous non-blocking allows the client to be in a blocking state when requesting data, and can process things of interest during the time period when the requested data is returned.

- Principle

Using the `CompletableFuture` concurrency tool class born in java8, it can process data asynchronously and obtain it when needed in the future.

- accomplish

The data is transmitted in the channel `channel` between the server and the client. The client sends a request packet to the channel and needs to wait for the server to return. In this case, you can use `CompletableFuture` as the return result, just let the client read the After the data, the result is put in the value through the `complete()` method, and the result is obtained through the `get()` method in the future.

### 5. RNF Protocol
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
  <version>2.0.4</version>
</dependency>
```

The latest version `2.1.0` is still in the testing stage, introducing snowflake algorithm, distributed cache to solve the `2.0.0` version timeout only single machine available and distributed failure problem.
```xml
<dependency>
  <groupId>cn.fyupeng</groupId
  <artifactId>rpc-core</artifactId>
  <version>2.1.0</version>
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

Advantage: 

`Nacos` was chosen as the registry because of the high availability for long-term service reliability
- A list of registry services is kept locally
- Services remain available during automatic restart recovery of down nodes

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

The retry mechanism server side should guarantee the principle of executing the retry packet once, i.e., to achieve idempotency.

Implementation idea: with the help of distributed cache for the first request packet asynchronous cache request `id`, distributed cache selection `Redis`, client selection `Jedis and Lettuce`, given a `key` expiration time to meet the retry mechanism to reduce the steps to clean up the cache again.

The idempotency acts on the expiration time, which also affects the number of retries of the timeout mechanism as well as the high concurrency scenario. There is the advantage of the snowflake algorithm, which does not generate duplicate ids at different times, so you can boldly set the expiration time larger, depending on how much memory you can afford and turn to the memory bottleneck.


### 9. Snowflakes algorithm 

```java
/**
     * Custom distributed id number only
     *  1-bit Symbol bit
     * 41-bit Timestamp
     * 10-bit Worker id
     * 12-bit Concurrent Sequence Number
     *
     *       The distribute unique id is as follows :
     * +---------------++---------------+---------------+-----------------+
     * |     Sign      |     epoch     |    workerId   |     sequence     |
     * |    1 bits     |    41 bits    |    10 bits   |      12 bits      |
     * +---------------++---------------+---------------+-----------------+
     */
```

- Introduction

Snowflake algorithm: It mainly consists of three parts: timestamp, machine code and sequence code, and each part has a representative meaning.
> Sign Bit

Represents the sign bit, with a fixed value of `0`, indicating a positive number.
> Timestamp

The timestamp represents the running time, it consists of `41` bits and can be used up to 69 years, expressed by the difference between the current system timestamp and the early morning timestamp of the day the service starts.
> Machine Code

Machine code represents distributed nodes, it consists of `10` bits and can represent up to `1024` machine codes, which are generated by default using the current service host number `hashCode`, high-low dissimilarity and distributed caching algorithm, machine code generation exceptions are generated by `SecureRandom` using random seeds, `AtomicLong` and `CAS` locks, when machine code maximum number will throw an exception `WorkerIdCantApplyException`.
> Sequence Number

Sequence number code concurrency, which occurs in the same millisecond, i.e. millisecond concurrency as part of a unique value, it consists of `12` bits and can represent up to `4096` sequence numbers.

- Application Scenarios

(1) Multi-service node load to achieve business persistence primary key unique

(2) The maximum number of concurrent requests within milliseconds can reach `4095`, and the sequence number can be changed appropriately to meet different scenarios
  
(3) Meet the basic ordered unique number `id`, conducive to efficient index query and maintenance
  
(4) `id` number before the `6` bit attached to the current time year month day log query, can be used as a log record

And in `RPC` mainly used the snowflake algorithm to achieve the unique identification number of the request packet, because `UUUID` generation uniqueness and time continuity than the snowflake algorithm is better, but it id value is non-increasing sequence, in the index establishment and maintenance of higher cost.

Snowflake algorithm generated `id` basic orderly incremental, can be used as an index value, and low maintenance costs, the cost is a strong dependence on the machine clock, in order to maximize its advantages and reduce the shortcomings, to the nearest time within the preservation of the timestamp and sequence number, dial back that obtain the sequence number at that time, with the self-incrementing, no blocking to restore the timestamp before the clock dial back, dial back too much time to throw an abnormal interruption, and the server restart when a small probability of possible redial will thus lead to `id` value duplication problem.

```properties
cn.fyupeng.redis.server-addr=127.0.0.1:6379
cn.fyupeng.redis.server-auth=true
cn.fyupeng.redis.server-pwd=123456
```

In addition to this, the timeout retry mechanism, in the distributed scenario, the second retry will be loaded to other service nodes through the load balancing policy, using the snowflake algorithm to make up for the problem of the inability to solve the idempotency in the distributed scenario.

Timeout retry uses `Jedis/Lettuce` two ways to implement caching, and the corresponding caching connection client ways can be configured on the server side and client side respectively.

```properties
cn.fyupeng.redis.server-way=lettuce
cn.fyupeng.redis.client-way=jedis
cn.fyupeng.redis.client-async=true
```

How do I choose between `JRedisHelper` and `LRedisHelper`?

JRedisHelper
- Thread safety
- Pessimistic locking mechanism for `synchronized` and `lock`
- No thread pooling
- Connection count of `1`
- Synchronized operations
- Cache and get machine `id` by host number
- Cache and fetch request results based on request `id`

LRedisHelper
- Thread safety
- Provides thread pooling
- Connection count is stable and provided by the thread pool
- Asynchronous/synchronous operations
- Provides caching and fetching machine `id` by host number
- Provide caching and fetching of request results based on request `id`
>Special Reminder

Highly concurrent requests do not have duplicate request numbers, the current maximum millisecond concurrency `4096`, and the timeout mechanism, `LRedisHelper` thread pool on the connection timeout control and other configuration parameters are not mature, specific application scenarios can download the source code to modify the parameters.

### 10. exception resolution
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

Throw exception `cn.fyupeng.exception.AnnotationMissingException`

After the retry mechanism is enabled, if the client fails to invoke the service after the number of retries, the service is considered unavailable and a timeout retry exception is thrown.

After the exception is thrown, the thread will be interrupted and the tasks not yet executed by the thread will be terminated, and if the retry mechanism is not enabled by default, the exception will not be thrown.


- InvalidSystemClockException

Throw exception `cn.fyupeng.idworker.exception.InvalidSystemClockException`

Snowflake algorithm generation is a small probability of clock redirection, time redirection needs to solve the problem of `id` value duplication, so it is possible to throw `InvalidSystemClockException` interrupt exception, logic can not handle the exception.
- WorkerIdCantApplyException

Throw exception `cn.fyupeng.idworker.exception.WorkerIdCantApplyException`

Snowflake algorithm generation, with the help of `IdWorker` generator to generate a distributed unique `id`, is with the help of machine code, when the number of machine code generated to reach the maximum will no longer apply, then will throw an interrupt exception `WorkerIdCantApplyException`.

### 11. Version Tracking

#### Version 1.0

- [ [#1.0.1](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.1/pom) ]: Solve the problem that the registration service cannot be found in real distributed scenarios logical problem;

- [ [#1.0.2](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.2/pom) ]: Decoupled registry address binding, available to the launcher Configure the `resource.properties` file under the resources of the project where you are located;

- [ [#1.0.4.RELEASE](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.4.RELEASE/pom) ]: Fixing the problem that services registered to the registry are not discovered after deploying the project in the `Jar` way, decoupling the injection of the `Jar` package startup configuration file, where the same constraint name will overwrite the original configuration information of the project.

- [ [#1.0.5](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.5/pom) ]: The default level of the heartbeat mechanism printing configuration is `trace`, and the default log level is `info`, which needs to be enabled in `logback.xml`.

- [ [#1.0.6](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.6/pom) ]: The default request packet size is `4096` Bytes, and the expansion is `100000` Bytes, meet the daily `100000` word data packets, it is not recommended to send large data packets, if necessary, see the exception `OutOfMemoryError` description.

- [ [#1.0.10](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/1.0.10/pom) ]: Repair the problem of `select` failure in load balancing, provide configuration center highly available cluster node injection configuration, load balancing configuration, fault-tolerant automatic switching

#### version 2.0
- [ [#2.0.0](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.0/pom) ]: Optimized version `1.0` version, version `2.0` introduced timeout retry mechanism, using to idempotency to solve the business loss problem and improve business reliability.

- [ [#2.0.1](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.1/pom )]: Version maintenance

- [ [#2.0.2](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.2/pom )]: Repair the problems of downtime retry and load in the cluster configuration center

- [ [#2.0.3](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.3/pom )]: Provide personalized service version number to support various scenarios, such as test and formal scenarios, allowing better compatibility of services and supporting version maintenance and upgrades.

- [ [#2.0.4](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.0.4/pom )]: Support `SPI` mechanism, interface and implementation decoupling.

- [ [#2.1.0](https://search.maven.org/artifact/cn.fyupeng/rpc-netty-framework/2.1.0/pom) ]: introduce snowflake algorithm and distributed cache, `2.0.0` version only supports single machine idempotency, fix the distributed scenario failure problem, use `polling load + timeout mechanism`, can efficiently solve the service timeout problem.

### 12. Development Notes

If you have secondary development ability, you can directly modify the source code, and finally use the command `mvn clean package` in the project directory to package the core package and dependency package to the `rpc-netty-framework\rpc-core\target` directory , this project is an open source project, if you think it will be adopted by the developers of this project, please add the original author `GitHub` link https://github.com/fyupeng after the open source, thank you for your cooperation!

