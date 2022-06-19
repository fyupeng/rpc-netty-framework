package com.zhkucst.util;

import com.zhkucst.enums.ResponseCode;
import com.zhkucst.exception.ReceiveResponseException;
import com.zhkucst.exception.RpcException;
import com.zhkucst.protocol.RpcRequest;
import com.zhkucst.protocol.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;

/**
 * @Auther: fyp
 * @Date: 2022/3/28
 * @Description:
 * @Package: com.zhkucst.util
 * @Version: 1.0
 */
@Slf4j
public class RpcMessageChecker {

    private static final String INTERFACE_NAME = "interfaceName";

    public RpcMessageChecker() {
    }

    public static void check(RpcRequest rpcRequest, RpcResponse rpcResponse) throws RpcException {

        if (rpcResponse == null) {
            log.error("service call failed, service: {}",rpcRequest.getInterfaceName());
            throw new ReceiveResponseException("service call failed Exception");
        }
        /**
         *  校验请求包 请求号 与 响应包中的 请求号 是否一致
         */
        if (!rpcResponse.getRequestId().equals(rpcRequest.getRequestId())) {
            log.error("inconsistent request numbers");
            throw new ReceiveResponseException("inconsistent request numbers Exception");
        }

        /**
         * 注意 rpcResponse 是 通过 反序列化 重新 实例化的 对象
         * rpcResponse.getStatusCode() 与 ResponseCode.SUCCESS.getCode() 不是同一个对象，虽然 ResponseCode 是单例模式
         * equals() 方法 判断 的 是 两个对象 的 值 相等 切忌 使用 !=
         */
        if (rpcResponse.getStatusCode() == null || !rpcResponse.getStatusCode().equals(ResponseCode.SUCCESS.getCode())) {
            System.out.println(rpcResponse.getStatusCode().equals(ResponseCode.SUCCESS.getCode()));
            log.error("service call failed, service: {}",rpcRequest.getInterfaceName());
            throw new ReceiveResponseException("service call failed Exception");
        }

        /**
         * 校验包 是否被人 修改过
         */
        try {
            String checkCode = new String(DigestUtils.md5(rpcResponse.getData().toString().getBytes("UTF-8")));
            if (checkCode == null || !checkCode.equals(rpcResponse.getCheckCode())) {
                log.error("data in package is modified， data:{}",rpcResponse.getData());
                throw new ReceiveResponseException("data in package is modified Exception");
            }
        } catch (UnsupportedEncodingException e) {
            log.error("package is damaged, package:{}", rpcResponse);
            throw new ReceiveResponseException("package is damaged Exception");
            //e.printStackTrace();
        }
        log.info("Packet verification succeeded!");
    }

}
