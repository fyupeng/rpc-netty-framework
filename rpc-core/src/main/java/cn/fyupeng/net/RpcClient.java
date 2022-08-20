package cn.fyupeng.net;

import cn.fyupeng.exception.RpcException;
import cn.fyupeng.protocol.RpcRequest;

/**
 * @Auther: fyp
 * @Date: 2022/3/23
 * @Description:
 * @Package: cn.fyupeng.net
 * @Version: 1.0
 */
public interface RpcClient {
    Object sendRequest(RpcRequest rpcRequest) throws RpcException;
}
