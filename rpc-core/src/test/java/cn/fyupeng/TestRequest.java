package cn.fyupeng;

import cn.fyupeng.protocol.RpcRequest;
import cn.fyupeng.serializer.JsonSerializer;

/**
 * @Auther: fyp
 * @Date: 2023/8/6
 * @Description:
 * @Package: cn.fyupeng
 * @Version: 1.0
 */
public class  TestRequest {
    public static void main(String[] args) {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setHeartBeat(Boolean.TRUE);
        JsonSerializer jsonSerializer = new JsonSerializer();
        byte[] serialize = jsonSerializer.serialize(rpcRequest);
        String res = new String(serialize);
        System.out.println(res);
    }
}
