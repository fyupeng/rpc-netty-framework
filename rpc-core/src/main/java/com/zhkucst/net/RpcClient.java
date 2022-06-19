package com.zhkucst.net;

import com.zhkucst.exception.RpcException;
import com.zhkucst.protocol.RpcRequest;

/**
 * @Auther: fyp
 * @Date: 2022/3/23
 * @Description:
 * @Package: com.zhkucst.net
 * @Version: 1.0
 */
public interface RpcClient {
    Object sendRequest(RpcRequest rpcRequest) throws RpcException;
}
