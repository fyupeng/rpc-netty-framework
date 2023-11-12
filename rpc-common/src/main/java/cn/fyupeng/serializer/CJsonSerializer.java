package cn.fyupeng.serializer;

import cn.fyupeng.protocol.RpcResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.fyupeng.enums.SerializerCode;
import cn.fyupeng.protocol.RpcRequest;
import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;

/**
 * @Auther: fyp
 * @Date: 2022/3/24
 * @Description:
 * @Package: cn.fyupeng.serializer
 * @Version: 1.0
 * 存在未解决的 问题：
 * 1. 增加 心跳包后，JSON 无法 反序列化 存在 实例字段为 空的 包
 * 2. 对于 心跳包的解析也一样，由于 我们心跳包也是 封装到 rpcRequest 一些信息
 * 也就为空，故 心跳包 JSON 在 服务端 无法正常 解码
 */
@Slf4j
public class CJsonSerializer implements CommonSerializer {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(Object obj) {
        byte[] data = null;
        try {
            data = objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.error("Error occurred while serializing, info: {}", e.getMessage());
        }
        return data;
    }

    @Override
    public Object deserialize(byte[] data, Class<?> clazz) {
        if (data == null) {
            return null;
        }
        Object obj = null;
        try {
            obj = objectMapper.readValue(data, clazz);
            if (obj instanceof RpcRequest) {
                obj = validateAndHandlerRequest(obj);
            } else if (obj instanceof RpcResponse) {
                obj = validateAndHandlerResponse(obj);
            }
        } catch (IOException e) {
            log.error("Error occurred while deserializing, info: ", e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * 验证请求的参数类型 和 参数对象 的一致性 并处理
     * 1. 由于 rpcRequest 中的 字段 parameters 是 Object[] ,序列化后 是转换成 JSON 字符串 会丢失 类型信息
     * 2. 反序列化后，虽然能正确 转换成 对应的 对象，但类型 丢失 最终是 Object，可通过 字段 paramTypes 来恢复
     * @param obj
     * @return
     * @throws IOException
     */
    private Object validateAndHandlerRequest(Object obj) throws IOException, ClassNotFoundException {
        RpcRequest rpcRequest = (RpcRequest) obj;
        if (null == rpcRequest.getParamTypes()) return obj;
        for (int i = 0; i < rpcRequest.getParamTypes().length; i++) {
            Class<?> clazz = Class.forName(rpcRequest.getParamTypes()[i]);
            if (!clazz.isAssignableFrom(rpcRequest.getParameters()[i].getClass())) {
                byte[] bytes = objectMapper.writeValueAsBytes(rpcRequest.getParameters()[i]);
                rpcRequest.getParameters()[i] = objectMapper.readValue(bytes, clazz);
            }
        }
        return rpcRequest;
    }

    /**
     * 验证请求的参数类型 和 参数对象 的一致性 并处理
     * 1. 由于 rpcRequest 中的 字段 parameters 是 Object[] ,序列化后 是转换成 JSON 字符串 会丢失 类型信息
     * 2. 反序列化后，虽然能正确 转换成 对应的 对象，但类型 丢失 最终是 Object，可通过 字段 paramTypes 来恢复
     * @param obj
     * @return
     * @throws IOException
     */
    private Object validateAndHandlerResponse(Object obj) throws IOException, ClassNotFoundException {
        RpcResponse rpcResponse = (RpcResponse) obj;
        if (null == rpcResponse.getDataType()) return obj;
        Class<?> clazz = Class.forName(rpcResponse.getDataType());
        if (!clazz.isAssignableFrom(rpcResponse.getDataType().getClass())) {
            byte[] bytes = objectMapper.writeValueAsBytes(rpcResponse.getDataType());
            Object dataObject = objectMapper.readValue(bytes, clazz);
            rpcResponse.setData(dataObject);
        }
        return rpcResponse;
    }

    @Override
    public int getCode() {
        // 获取的是 枚举类中 枚举常量为 JSON 的 SerializerCode 实例
        return SerializerCode.valueOf(SerializerCode.CJSON.name()).getCode();
    }
}
