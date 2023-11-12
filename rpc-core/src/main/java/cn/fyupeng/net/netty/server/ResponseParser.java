package cn.fyupeng.net.netty.server;

import cn.fyupeng.protocol.RpcRequest;
import cn.fyupeng.protocol.RpcResponse;
import cn.fyupeng.serializer.CommonSerializer;
import cn.hutool.core.bean.BeanUtil;
import io.netty.channel.ChannelHandlerContext;
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
public class ResponseParser extends MessageToMessageEncoder<RpcResponse> {

    private CommonSerializer serializer;

    public ResponseParser(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse, List<Object> list) throws Exception {

        RpcResponse resp = BeanUtil.toBean(rpcResponse, RpcResponse.class);
        resp.setDataType(rpcResponse.getDataType());

        list.add(resp);
    }

    private String adaptDataType(String dataType) throws ClassNotFoundException {
        String adaptDataType = "";
        if (CommonSerializer.SJSON_SERIALIZER == serializer.getCode()) {
                Class<?> paramTypeClazz = Class.forName(dataType);
            if (String.class.equals(paramTypeClazz)) {
                adaptDataType = "string";
            } else if (Float.class.equals(paramTypeClazz) || float.class.equals(paramTypeClazz)) {
                adaptDataType = "float32";
            } else if (Double.class.equals(paramTypeClazz) || double.class.equals(paramTypeClazz)) {
                adaptDataType = "float64";
            } else if (Integer.class.equals(paramTypeClazz) || int.class.equals(paramTypeClazz)) {
                adaptDataType = "int";
            }
            return adaptDataType;
        }
        return dataType;
    }

}
