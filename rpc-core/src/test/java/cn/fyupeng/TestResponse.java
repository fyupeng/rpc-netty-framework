package cn.fyupeng;

import cn.fyupeng.protocol.RpcResponse;
import cn.fyupeng.serializer.JsonSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Arrays;
import java.util.Base64;

/**
 * @Auther: fyp
 * @Date: 2023/8/6
 * @Description:
 * @Package: cn.fyupeng
 * @Version: 1.0
 */
public class TestResponse {
    public static void main(String[] args) throws JsonProcessingException {
        Student student = new Student(123, "小明", true);
        RpcResponse data = RpcResponse.success(student, "1235", new byte[]{1,2,3,4});
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] bytes = objectMapper.writeValueAsBytes(data);
        System.out.println(new String(bytes));
        JsonSerializer jsonSerializer = new JsonSerializer();
        byte[] serialize = jsonSerializer.serialize(data);
        System.out.println(new String(serialize));

    }
}
