package com.zhkucst.serializer;


/**
 * @Auther: fyp
 * @Date: 2022/3/24
 * @Description: 公共的序列化接口
 * @Package: com.zhkucst.serializer
 * @Version: 1.0
 */
public interface CommonSerializer {

    Integer KRYO_SERIALIZER = 0;
    Integer JSON_SERIALIZER = 1;
    Integer DEFAULT_SERIALIZER = KRYO_SERIALIZER;

    byte[] serialize(Object obj);

    Object deserialize(byte[] bytes, Class<?> clazz);

    int getCode();

    static CommonSerializer getByCode(int code) {
        switch (code) {
            case 0:
                return new KryoSerializer();
            case 1:
                return new JsonSerializer();
            default:
                return null;
        }
    }

}
