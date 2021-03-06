package com.zhkucst.net.netty.server;

import com.zhkucst.handler.RequestHandler;
import com.zhkucst.protocol.RpcRequest;
import com.zhkucst.protocol.RpcResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * @Auther: fyp
 * @Date: 2022/3/24
 * @Description:
 * @Package: com.zhkucst.net.netty.server
 * @Version: 1.0
 */
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static RequestHandler requestHandler;

    static {
        requestHandler = new RequestHandler();
    }



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        try {
            /**
             * 心跳包 只 作为 检测包，不做处理
             */
            if (msg.getHeartBeat()) {
                log.info("receive hearBeatPackage from customer...");
                return;
            }
            log.info("server has received request: {}", msg);
            Object result = requestHandler.handler(msg);
            // 生成 校验码，客户端收到后 会 对 数据包 进行校验
            if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                String checkCode = new String(DigestUtils.md5(result.toString().getBytes("UTF-8")));
                RpcResponse rpcResponse = RpcResponse.success(result, msg.getRequestId(),checkCode);
                ChannelFuture future = ctx.writeAndFlush(rpcResponse);
            } else {
                log.error("channel is not writable");
            }
            /**
             * 1. 通道关闭后，对于 心跳包 将不可用
             * 2. 由于客户端 使用了 ChannelProvider 来 缓存 channel，这里关闭后，无法 发挥 channel 缓存的作用
             */
            //future.addListener(ChannelFutureListener.CLOSE);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("error occurred while invoking!,info: ", cause);
        ctx.close();
    }

    /**
     * 监听 所有 客户端 发送的 心跳包
     * IdleState.READER_IDLE 时间内 服务端 没有 读操作（即客户端没有写操作，心跳包发送失败，失去连接）
     * 触发方法执行，关闭 服务端 与 客户端的 通道 channel
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("Heartbeat packets have not been received for a long time");
                ctx.channel().close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
