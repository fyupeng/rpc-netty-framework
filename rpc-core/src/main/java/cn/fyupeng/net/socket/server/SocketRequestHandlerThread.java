package cn.fyupeng.net.socket.server;

import cn.fyupeng.exception.UnSupportBodyException;
import cn.fyupeng.exception.UnrecognizedException;
import cn.fyupeng.handler.RequestHandler;
import cn.fyupeng.protocol.RpcRequest;
import cn.fyupeng.protocol.RpcResponse;
import cn.fyupeng.serializer.CommonSerializer;
import cn.fyupeng.util.ObjectReader;
import cn.fyupeng.util.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @Auther: fyp
 * @Date: 2022/3/22
 * @Description: 作为一个服务器线程，去处理客户端发起的 请求
 * @Package: cn.fyupeng.net.socket.server
 * @Version: 1.0
 */
@Slf4j
public class SocketRequestHandlerThread implements Runnable {

    private Socket socket;
    private RequestHandler requestHandler;
    private CommonSerializer serializer;

    // 在创建 新线程时 进行赋值
    public SocketRequestHandlerThread(Socket socket, RequestHandler requestHandler, CommonSerializer serializer) {
        this.socket = socket;
        this.requestHandler = requestHandler;
        this.serializer = serializer;
    }

    @Override
    public void run() {
        RpcResponse response = null;
        try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())){
            // 读取 客户端 发回的 请求
            //RpcRequest rpcRequest = (RpcRequest) ois.readObject();
            RpcRequest rpcRequest = (RpcRequest) ObjectReader.readObject(ois);
            Object result = requestHandler.handler(rpcRequest);

            String checkCode = new String(DigestUtils.md5(result.toString().getBytes("UTF-8")));
            // 返回 处理结果 或 处理中途抛出的 异常
            //oos.writeObject(result);
            if (result instanceof Exception) {
                RpcResponse rpcResponse = RpcResponse.failure(((Exception) result).getMessage(), rpcRequest.getRequestId());
                ObjectWriter.writeObject(oos, rpcResponse, serializer);
            }else {
                RpcResponse rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId(), checkCode);
                ObjectWriter.writeObject(oos, rpcResponse, serializer);
            }

        } catch (IOException | UnSupportBodyException | UnrecognizedException e) {
            //e.printStackTrace();
            log.error("Error occurred while invoking or sent,info:  ", e);
        }
    }

}
