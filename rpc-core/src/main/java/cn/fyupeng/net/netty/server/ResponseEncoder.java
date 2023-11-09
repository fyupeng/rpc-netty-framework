package cn.fyupeng.net.netty.server;

import cn.fyupeng.protocol.RpcRequest;
import cn.fyupeng.protocol.RpcResponse;
import cn.fyupeng.serializer.CommonSerializer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


/**
 * @Auther: fyp
 * @Date: 2022/3/24
 * @Description:
 * @Package: cn.fyupeng.net.netty.client
 * @Version: 1.0
 */
@Slf4j
public class ResponseEncoder extends MessageToMessageEncoder<RpcResponse> {

    private CommonSerializer serializer;

    public ResponseEncoder(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse, List<Object> list) throws Exception {
        channelHandlerContext.write(rpcResponse);
    }
}
