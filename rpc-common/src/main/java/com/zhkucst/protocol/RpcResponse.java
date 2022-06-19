package com.zhkucst.protocol;


import com.zhkucst.enums.ResponseCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Auther: fyp
 * @Date: 2022/3/22
 * @Description: 响应体
 * @Package: com.zhkucst.protocol
 * @Version: 1.0
 */
public class RpcResponse<T> implements Serializable {

    /**
     * 响应 对应的 请求号
     */
    private String requestId;
    /**
     * 检验码，数据 防伪
     */
    private String checkCode;
    // 响应状态吗
    private Integer statusCode;
    // 响应状态补充信息
    private String message;
    // 响应数据
    private T data;

    /**
     * 没有空 构造方法 会导致 反序列化 失败
     * Exception: no delegate- or property-based Creator
     */
    public RpcResponse() {
        super();
    }

    public static <T> RpcResponse success(String requestId, String checkCode) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(ResponseCode.SUCCESS.getCode());
        response.setRequestId(requestId);
        response.setCheckCode(checkCode);
        response.setMessage("ok");
        return response;
    }

    public static <T> RpcResponse success(T data, String requestId, String checkCode) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(ResponseCode.SUCCESS.getCode());
        response.setRequestId(requestId);
        response.setCheckCode(checkCode);
        response.setData(data);
        response.setMessage("ok");
        return response;
    }

    public static <T> RpcResponse failure(String message, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(ResponseCode.FAILURE.getCode());
        response.setRequestId(requestId);
        response.setMessage(message);
        return response;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RpcResponse{" +
                "requestId='" + requestId + '\'' +
                ", statusCode=" + statusCode +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
