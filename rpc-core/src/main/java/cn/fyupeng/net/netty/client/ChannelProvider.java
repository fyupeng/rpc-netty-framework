package cn.fyupeng.net.netty.client;

import cn.fyupeng.codec.CommonDecoder;
import cn.fyupeng.codec.CommonEncoder;
import cn.fyupeng.exception.ConnectFailedException;
import cn.fyupeng.exception.RpcException;
import cn.fyupeng.serializer.CommonSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @Auther: fyp
 * @Date: 2022/3/28
 * @Description:
 * @Package: cn.fyupeng.net.netty.client
 * @Version: 1.0
 */
@Slf4j
public class ChannelProvider {

    private static EventLoopGroup group;
    private static Bootstrap bootstrap = initBootstrap();
    /**
     * 缓存 Channel
     */
    private static Map<String, Channel> channels = new ConcurrentHashMap<>();

    /**
     * 这里 是可以 共享 channel 的
     * 共享的策略是 同一台 主机 并且 是用同一种 编解码处理的，使用同一条 通道
     * @param address
     * @param serializer
     * @return
     * @throws RpcException
     */
    public static Channel get(InetSocketAddress address, CommonSerializer serializer) throws RpcException {
        String key = address.toString() + serializer.getCode();
        if (channels.containsKey(key)) {
            Channel channel = channels.get(key);
            /**
             * channel 关闭 并不会 让 channel 在 map 中 为空
             * 不做 处理的话，会 影响 GC 垃圾回收
              */
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channels.remove(key);
            }
        }

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new IdleStateHandler(3, 5, 7, TimeUnit.SECONDS))
                        .addLast(new CommonDecoder())
                        .addLast(new CommonEncoder(serializer))
                        .addLast(new NettyClientHandler());
                /**
                 * 读 channel --> 解码 --> (注册检测心跳包，后面自己会检测) --> 处理客户端 --> 编码 --> 写 channel
                 */
            }
        });
        Channel channel = null;
        try {
            channel = connect(bootstrap, address);
        } catch (ExecutionException | InterruptedException e) {
            //e.printStackTrace();
            log.error("error occurred while customer connecting server: {}", e.getMessage());
            throw new ConnectFailedException("error occurred while customer connecting server Exception");
        }
        channels.put(key, channel);
        return channel;

    }

    private static Channel connect(Bootstrap bootstrap, InetSocketAddress address) throws ExecutionException, InterruptedException {

        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        ChannelFuture channelFuture = bootstrap.connect(address);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // future 并不能 直接 拿到，得等待返回
                    completableFuture.complete(future.channel());
                }else {
                    throw new IllegalStateException();
                }
            }
        });
        return completableFuture.get();
    }

    private static Bootstrap initBootstrap() {
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                // 连接 失败重连 最大超时时间
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                // TCP 底层 心跳机制
                .option(ChannelOption.SO_KEEPALIVE, true)
                // TCP 默认开启的 Nagle 算法，作用是 尽可能 发送大数据块，减少网络传输，降低延迟
                .option(ChannelOption.TCP_NODELAY, true);
        return bootstrap;
    }

}
