package cn.fyupeng.serializer;


/**
 * @Auther: fyp
 * @Date: 2022/3/24
 * @Description: 公共的序列化接口
 * @Package: cn.fyupeng.serializer
 * @Version: 1.0
 */
public interface CommonSerializer {

    Integer KRYO_SERIALIZER = 0;
    Integer JSON_SERIALIZER = 1;
    Integer HESSIAN_SERIALIZER = 2;
    Integer JFURY_SERIALIZER = 3;
    Integer GFURY_SERIALIZER = 4;
    Integer XFURY_SERIALIZER = 5;
    // 处理请求包 - Golang（客户端） -> Java（服务端） 跨协议 JSON 序列化、Java（服务端） -> Golang（客户端） 跨协议 JSON 反序列化
    Integer CJSON_SERIALIZER = 6;
    // 处理响应包 - Java（客户端） -> Golang（服务端） 跨协议 JSON 反序列化、Golang（服务端） -> Java（客户端） 跨协议 JSON 序列化
    Integer SJSON_SERIALIZER = 7;
    Integer DEFAULT_SERIALIZER = KRYO_SERIALIZER;

    byte[] serialize(Object obj);

    Object deserialize(byte[] bytes, Class<?> clazz);
    /**
     * 网络序列化传输 最大化减少字节数，并且可以自动识别客户端采用的序列化方式并加以处理
     * @return
     */
    int getCode();

    /**
     * 规定协议代码获取对应序列化方式
     * @param code
     * @return
     */
    static CommonSerializer getByCode(int code) {
        switch (code) {
            case 0:
                return new KryoSerializer();
            case 1:
                return new JsonSerializer();
            case 2:
                return new HessianSerializer();
            case 3:
                return new FurySerializer(JFURY_SERIALIZER);
            case 4:
                return new FurySerializer(GFURY_SERIALIZER);
            case 5:
                return new FurySerializer(XFURY_SERIALIZER);
            case 6:
                return new CJsonSerializer();
            default:
                return null;
        }
    }
}
