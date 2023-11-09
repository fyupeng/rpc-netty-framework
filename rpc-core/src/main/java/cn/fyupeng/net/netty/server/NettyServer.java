package cn.fyupeng.net.netty.server;

import cn.fyupeng.codec.CommonDecoder;
import cn.fyupeng.codec.CommonEncoder;
import cn.fyupeng.config.AbstractRedisConfiguration;
import cn.fyupeng.exception.RpcException;
import cn.fyupeng.hook.ServerShutdownHook;
import cn.fyupeng.net.AbstractRpcServer;
import cn.fyupeng.provider.ServiceProvider;
import cn.fyupeng.registry.ServiceRegistry;
import cn.fyupeng.serializer.CommonSerializer;
import cn.fyupeng.util.IpUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;


/**
 * @Auther: fyp
 * @Date: 2022/3/23
 * @Description: netty服务器
 * @Package: cn.fyupeng.net.netty.server
 * @Version: 1.0
 */
@Slf4j
public class NettyServer extends AbstractRpcServer {

    /**
     * 不放到 抽象层 是 因为 不希望 被继承，子类会 破坏 CommonSerializer 的 完整性
     * 使用时，主机名一般运行在项目所在服务器上，推荐 localhost 和 127.0.0.1
     * 或者 公网 主机名
     */
    private final CommonSerializer serializer;

    /**
     * Netty 服务端 连接监听 和 业务 事件循环组
     * 考虑到 一个进程中 只创建一个 NettyServer 为了共享 EventLoopGroup 和 优雅 善后处理 使用 static final 修饰
     * final 只是 引用 对象的地址 不可变，内容 成员还是可以变的
     */
    private static final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private static String redisServerWay = "";

    /**
     * 服务器启动时 优先做一些预加载
     */
    static {
        AbstractRedisConfiguration.getServerConfig();
         /**
         * 其他 预加载选项
         */
        NettyChannelDispatcher.init();
    }

    /**
     *
     * @param hostName 启动服务所在机器的主机号，可以是私网或者公网
     * @param port 启动服务所在机器的端口号
     * @param serializerCode 序列化代码
     * @throws RpcException
     */
    public NettyServer(String hostName, int port, Integer serializerCode) throws RpcException {
        this.hostName = hostName.equals("localhost") || hostName.equals("127.0.0.1") ? IpUtils.getPubIpAddr() : hostName;
        log.info("start with host: {}, port: {}", this.hostName, port);
        this.port = port;
        //serviceRegistry = new NacosServiceRegistry();
        //serviceProvider = new DefaultServiceProvider();
        /**
         * 使用 SPI 机制，接口与实现类解耦到配置文件
         */
        serviceRegistry = ServiceLoader.load(ServiceRegistry.class).iterator().next();
        serviceProvider = ServiceLoader.load(ServiceProvider.class).iterator().next();

        serializer = CommonSerializer.getByCode(serializerCode);
        // 扫描 @ServiceScan 包下的 所有 @Service类，并 注册它们
        scanServices();
    }

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

        try {
            /**
             *  启动服务
             */
            // 栈上分配资源
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT.DEFAULT)//缓冲池
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            /**
                             * 读 超时 触发, WriteIdleTime 和 allIdleTime 为 0 表示不做处理
                             */
                            pipeline.addLast(new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer("\r\n", CharsetUtil.UTF_8)));
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new CommonEncoder(serializer, "\r\n"));
                            pipeline.addLast(new CommonDecoder());
                            pipeline.addLast(new ResponseEncoder(serializer));
                            pipeline.addLast(new NettyServerHandler());
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("Error occurred while starting server! {}",e);
        } finally {
            // 如果服务器 直接通过 关闭 来断开 finally 及 后面的代码将 无法执行，搬迁 到 关闭钩子 优雅关闭
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        // 无法执行
    }

    @Override
    public void shutdown() {
        shutdownAll();
        shutdownNettyChannelDispatcher();
    }

    /**
     * 关闭线程池的使用，由 NettyServer 的处理器 NettyServerHandler 业务模块 NettyChannelDispatcher 使用
     */
    public static void shutdownNettyChannelDispatcher() {
        NettyChannelDispatcher.shutdownAll();
    }

    /**
     * 结束生命周期 交给 关闭钩子
     */
    public static void shutdownAll() {
        log.info("close all EventLoopGroup now ...");
        try {
            bossGroup.shutdownGracefully().sync();
            log.info("close Netty Server Boss EventLoopGroup [{}] [{}]", bossGroup.getClass(), bossGroup.isTerminated());
        } catch (InterruptedException e) {
            log.error("close thread was interrupted: ", e);
        }
        try {
            workerGroup.shutdownGracefully().sync();
            log.info("close Netty Server Worker EventLoopGroup [{}] [{}]", workerGroup.getClass(), bossGroup.isTerminated());
        } catch (InterruptedException e) {
            log.error("close thread was interrupted: ", e);
        }
        try {
            bossGroup.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("failed to close Netty Server Boss EventLoopGroup: ", e);
            bossGroup.shutdownNow();
        }
        try {
            workerGroup.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("failed to close Netty Server Boss EventLoopGroup: ", e);
            workerGroup.shutdownNow();
        }
        log.info("Netty Server EventLoopGroup closed successfully");
    }

}
