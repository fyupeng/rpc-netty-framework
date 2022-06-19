package com.zhkucst.enums;

/**
 * @Auther: fyp
 * @Date: 2022/3/24
 * @Description:
 * @Package: com.zhkucst.enums
 * @Version: 1.0
 */
public enum SerializerCode {
    KRYO(0),// KRYO 序列化 方式
    JSON(1); // JSON 序列化方式
    private final int code;

    SerializerCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
