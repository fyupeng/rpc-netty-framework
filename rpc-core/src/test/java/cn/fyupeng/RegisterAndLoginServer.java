package cn.fyupeng;

import cn.fyupeng.anotion.ServiceScan;
import cn.fyupeng.enums.SerializerCode;
import cn.fyupeng.exception.RpcException;
import cn.fyupeng.net.netty.server.NettyServer;

/**
 * @Auther: fyp
 * @Date: 2022/8/13
 * @Description:
 * @Package: com.fyupeng
 * @Version: 1.0
 */
@ServiceScan
public class RegisterAndLoginServer {
    public static void main(String[] args) {
        try {
            NettyServer nettyServer = new NettyServer("192.168.232.1", 8081, SerializerCode.KRYO.getCode());
            nettyServer.start();
        } catch (RpcException e) {
            e.printStackTrace();
        }
    }
}
