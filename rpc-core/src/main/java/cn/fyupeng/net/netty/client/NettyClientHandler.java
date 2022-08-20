package cn.fyupeng.net.netty.client;

import cn.fyupeng.factory.SingleFactory;
import cn.fyupeng.protocol.RpcRequest;
import cn.fyupeng.protocol.RpcResponse;
import cn.fyupeng.serializer.CommonSerializer;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;


/**
 * @Auther: fyp
 * @Date: 2022/3/24
 * @Description:
 * @Package: cn.fyupeng.net.netty.client
 * @Version: 1.0
 */
@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private final UnprocessedRequests unprocessedRequests;

    public NettyClientHandler() {
        unprocessedRequests = SingleFactory.getInstance(UnprocessedRequests.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        try {
            log.info(String.format("customer has received message: %s", msg));
            /**
             * 1. 取出 缓存在 AttributeKey 中 常量池ConstantPool 的 ConcurrentMap<String, RpcResponse>
             *  key 为 "rpcResponse"的 AttributeKey<RpcResponse>
             * 2. 底层原理是 putIfAbsent(name, tempConstant), 只第一次有效，下次不会 put, 只返回 第一次的值
             * 3. 一个 AttributeKey 类 分配 一个 常量池，多个AttributeKey 共享
             * 4. 多线程环境下 常量池中的 ConcurrentHashMap<String,T > 是共享的，并且是 线程安全的
             */
            //AttributeKey<RpcResponse> key = AttributeKey.valueOf(msg.getRequestId());
            //ctx.channel().attr(key).set(msg);
            unprocessedRequests.complete(msg);
            //ctx.channel().close();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("error occurred while invoking,info:", cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            // 客户端 没有发送 数据了，设置 写超时总会被 触发，从而 发送心跳包 给 服务端
            if (state == IdleState.WRITER_IDLE) {
                log.info("Send heartbeat packets to server[{}]", ctx.channel().remoteAddress());
                ChannelProvider.get((InetSocketAddress) ctx.channel().remoteAddress(), CommonSerializer.getByCode(CommonSerializer.DEFAULT_SERIALIZER));
                RpcRequest rpcRequest = new RpcRequest();
                rpcRequest.setHeartBeat(true);
                ctx.writeAndFlush(rpcRequest).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
