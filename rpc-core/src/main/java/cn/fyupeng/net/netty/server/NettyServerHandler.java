package cn.fyupeng.net.netty.server;

import cn.fyupeng.handler.RequestHandler;
import cn.fyupeng.idworker.utils.JRedisHelper;
import cn.fyupeng.idworker.utils.LRedisHelper;
import cn.fyupeng.protocol.RpcRequest;

import cn.fyupeng.protocol.RpcResponse;
import cn.fyupeng.serializer.CommonSerializer;
import cn.fyupeng.util.JsonUtils;
import cn.fyupeng.util.PropertiesConstants;
import com.alibaba.nacos.common.utils.StringUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @Auther: fyp
 * @Date: 2022/3/24
 * @Description:
 * @Package: cn.fyupeng.net.netty.server
 * @Version: 1.0
 */
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    /**
     * 服务器的监听通道读取方法是 多线程的，这样能应对多个 客户端的并发访问
     * @param ctx 通道处理上下文
     * @param msg 请求包
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
            /**
             * 心跳包 只 作为 检测包，不做处理
             */
            if (msg.getHeartBeat()) {
                log.debug("receive hearBeatPackage from customer...");
                return;
            }
            NettyChannelDispatcher.dispatch(ctx, msg);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        //super.channelWritabilityChanged(ctx);
        log.warn("trigger hi-lo channel buffer，now channel status:[active {}, writable: {}]", ctx.channel().isActive(), ctx.channel().isWritable());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("error occurred while invoking! info: ", cause);
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
