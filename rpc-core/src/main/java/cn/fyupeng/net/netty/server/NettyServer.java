package cn.fyupeng.net.netty.server;

import cn.fyupeng.codec.CommonDecoder;
import cn.fyupeng.codec.CommonEncoder;
import cn.fyupeng.exception.RpcException;
import cn.fyupeng.hook.ShutdownHook;
import cn.fyupeng.net.AbstractRpcServer;
import cn.fyupeng.provider.DefaultServiceProvider;
import cn.fyupeng.registry.NacosServiceRegistry;
import cn.fyupeng.serializer.CommonSerializer;
import cn.fyupeng.util.IpUtils;
import cn.fyupeng.util.JsonUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

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

    public NettyServer(String hostName, int port, Integer serializerCode) throws RpcException {
        this.hostName = hostName.equals("localhost") || hostName.equals("127.0.0.1") ? IpUtils.getPubIpAddr() : hostName;
        log.info("start with host: {}, port: {}", this.hostName, this.port);
        this.port = port;
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new DefaultServiceProvider();
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
        ShutdownHook.getShutdownHook().addClearAllHook();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            /**
             *  启动服务
             */
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            /**
                             * 读 超时 触发, WriteIdleTime 和 allIdleTime 为 0 表示不做处理
                             */
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new CommonEncoder(serializer));
                            pipeline.addLast(new CommonDecoder());
                            pipeline.addLast(new NettyServerHandler());
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(port).sync();
            future.channel().closeFuture().sync();

        } catch (Exception e) {
            log.error("Error occurred while starting server! {}",e);
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
