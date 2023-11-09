package cn.fyupeng.net.netty.client;

import cn.fyupeng.protocol.RpcRequest;
import cn.fyupeng.serializer.CommonSerializer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;


/**
 * @Auther: fyp
 * @Date: 2022/3/24
 * @Description:
 * @Package: cn.fyupeng.net.netty.client
 * @Version: 1.0
 */
@Slf4j
public class RequestEncoder extends MessageToMessageEncoder<RpcRequest> {

    private CommonSerializer serializer;

    public RequestEncoder(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest, List<Object> list) throws Exception {

        rpcRequest.setParamTypes(adaptParamTypes(rpcRequest.getParamTypes()));
        rpcRequest.setReturnType(adaptParamTypes(rpcRequest.getReturnType()));
        rpcRequest.setMethodName(adaptMethodName(rpcRequest.getMethodName()));

        list.add(rpcRequest);
        channelHandlerContext.write(rpcRequest);

    }

    private String[] adaptParamTypes(String[] paramTypes) throws ClassNotFoundException {
        String[] adaptParamTypes = new String[paramTypes.length];
        if (CommonSerializer.CJSON_SERIALIZER == serializer.getCode()) {
            for (int idx = 0; idx < paramTypes.length; idx++) {
                Class<?> paramTypeClazz = Class.forName(paramTypes[idx]);
                if (String.class.equals(paramTypeClazz)) {
                    adaptParamTypes[idx] = "string";
                } else if (Float.class.equals(paramTypeClazz) || float.class.equals(paramTypeClazz)) {
                    adaptParamTypes[idx] = "float32";
                } else if (Double.class.equals(paramTypeClazz) || double.class.equals(paramTypeClazz)) {
                    adaptParamTypes[idx] = "float64";
                } else if (Integer.class.equals(paramTypeClazz) || int.class.equals(paramTypeClazz)) {
                    adaptParamTypes[idx] = "int";
                }
            }
            return adaptParamTypes;
        }
        return paramTypes;
    }

    private String adaptParamTypes(String returnType) throws ClassNotFoundException {
        String adaptParamType = "";
        if (CommonSerializer.CJSON_SERIALIZER == serializer.getCode()) {
            Class<?> returnTypeClazz = Class.forName(returnType);
            if (String.class.equals(returnTypeClazz)) {
                adaptParamType = "string";
            } else if (Float.class.equals(returnTypeClazz) || float.class.equals(returnTypeClazz)) {
                adaptParamType = "float32";
            } else if (Double.class.equals(returnTypeClazz) || double.class.equals(returnTypeClazz)) {
                adaptParamType = "float64";
            } else if (Integer.class.equals(returnTypeClazz) || int.class.equals(returnTypeClazz)) {
                adaptParamType = "int";
            }
            return adaptParamType;
        }
        return returnType;
    }

    private String adaptMethodName(String methodName) {
        // Java 请求远程方法调用适配
        if (CommonSerializer.CJSON_SERIALIZER == serializer.getCode()) {
            return StringUtils.capitalize(methodName);
        }
        return methodName;
    }

}
