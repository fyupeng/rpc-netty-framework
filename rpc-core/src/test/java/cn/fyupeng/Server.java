package cn.fyupeng;

import cn.fyupeng.annotation.ServiceScan;
import cn.fyupeng.enums.SerializerCode;
import cn.fyupeng.exception.RpcException;
import cn.fyupeng.net.netty.server.NettyServer;
import cn.fyupeng.serializer.CommonSerializer;

/**
 * @Auther: fyp
 * @Date: 2022/8/13
 * @Description:
 * @Package: com.fyupeng
 * @Version: 1.0
 */
@ServiceScan
public class Server {
    public static void main(String[] args) {
        try {
            NettyServer nettyServer = new NettyServer( "192.168.81.191", 9527, CommonSerializer.JSON_SERIALIZER);
            nettyServer.start();
        } catch (RpcException e) {
            e.printStackTrace();
        }
    }
}
