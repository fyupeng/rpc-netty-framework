package cn.fyupeng.enums;

/**
 * @Auther: fyp
 * @Date: 2022/3/24
 * @Description:
 * @Package: cn.fyupeng.enums
 * @Version: 1.0
 */
public enum SerializerCode {
    KRYO(0),// KRYO 序列化 方式
    JSON(1), // JSON 序列化方式
    HESSIAN(2), // HESSIAN 序列化方式
    JFURY(3), // JAVA-FURY 序列化方式
    GFURY(4), // GO-FURY 序列化方式
    XFURY(5), // XLANG-FURY 序列化方式
    CJSON(6), // Client-JSON 序列化方式
    SJSON(6); // Server-JSON 序列化方式

    private final int code;

    SerializerCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
