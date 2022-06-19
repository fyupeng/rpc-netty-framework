package com.zhkucst.net.socket.client;

import com.zhkucst.exception.RpcException;
import com.zhkucst.exception.SerializerNotSetException;
import com.zhkucst.net.RpcClient;
import com.zhkucst.protocol.RpcRequest;
import com.zhkucst.protocol.RpcResponse;
import com.zhkucst.serializer.CommonSerializer;
import com.zhkucst.util.ObjectReader;
import com.zhkucst.util.ObjectWriter;
import com.zhkucst.util.RpcMessageChecker;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @Auther: fyp
 * @Date: 2022/3/22
 * @Description:
 * @Package: com.zhkucst.net.socket.client
 * @Version: 1.0
 */
@Slf4j
public class SocketClient implements RpcClient {

    private String hostName;
    private int port;

    private final CommonSerializer serializer;

    public SocketClient(String hostName, int port, Integer serializerCode) {
        this.hostName = hostName;
        this.port = port;
        this.serializer = CommonSerializer.getByCode(serializerCode);
    }

    public Object sendRequest(RpcRequest rpcRequest) throws RpcException {
        if (serializer == null) {
            log.error("Serializer not set");
            throw new SerializerNotSetException("Serializer not set Exception");
        }
        // 使用jdk9 使用的 try catch 可以自动关闭, 必须实现 Closeable
        try (Socket socket = new Socket(hostName,  port)){
            // 使用了装饰者模式
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            ObjectWriter.writeObject(oos, rpcRequest, serializer);
            Object obj = ObjectReader.readObject(ois);
            RpcResponse rpcResponse = (RpcResponse) obj;

            RpcMessageChecker.check(rpcRequest, rpcResponse);

            return rpcResponse;

        } catch (IOException e) {
            //e.printStackTrace();
            log.error("Error occurred while invoking badly,info: {}", e);
            return null;
        }
    }
}







