package cn.fyupeng.serializer;

import cn.fyupeng.enums.SerializerCode;
import cn.fyupeng.protocol.RpcRequest;
import cn.fyupeng.protocol.RpcResponse;
import io.fury.Fury;
import io.fury.Language;
import lombok.extern.slf4j.Slf4j;


/**
 * @Auther: fyp
 * @Date: 2022/3/25
 * @Description:
 * @Package: cn.fyupeng.serializer
 * @Version: 1.0
 */
@Slf4j
public class FurySerializer implements CommonSerializer {

    /**
     * 3 - Java
     * 4 - Go
     */
    private int serializerCode;

    private static final ThreadLocal<Fury> jFuryThreadLocal = ThreadLocal.withInitial(() -> {
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(Object.class);
        fury.register(RpcRequest.class);
        fury.register(RpcResponse.class);
        return fury;
    });
    private static final ThreadLocal<Fury> gFuryThreadLocal = ThreadLocal.withInitial(() -> {
        Fury fury = Fury.builder().withLanguage(Language.GO).build();
        fury.register(Object.class);
        fury.register(RpcRequest.class);
        fury.register(RpcResponse.class);
        return fury;
    });
    private static final ThreadLocal<Fury> xFuryThreadLocal = ThreadLocal.withInitial(() -> {
        Fury fury = Fury.builder().withLanguage(Language.XLANG).build();
        fury.register(Object.class);
        fury.register(RpcRequest.class);
        fury.register(RpcResponse.class);
        return fury;
    });

    public FurySerializer(int serializerCode) {
        this.serializerCode = serializerCode;
    }

    @Override
    public byte[] serialize(Object obj) {
        byte[] data = null;
        if (serializerCode == JFURY_SERIALIZER) {
            Fury fury = jFuryThreadLocal.get();
            data = fury.serialize(obj);
            System.out.println(data);
            jFuryThreadLocal.remove();
        } else if (serializerCode == GFURY_SERIALIZER) {
            Fury fury = gFuryThreadLocal.get();
            data = fury.serialize(obj);
            gFuryThreadLocal.remove();
        } else {
            log.error("serializerCode [{}] not found!", serializerCode);
        }
        return data;
    }

    @Override
    public Object deserialize(byte[] data, Class<?> clazz) {
        if (data == null) {
            return null;
        }
        Object obj = null;
        if (serializerCode == JFURY_SERIALIZER) {
            Fury fury = jFuryThreadLocal.get();
            obj = fury.deserialize(data);
            jFuryThreadLocal.remove();
        } else if (serializerCode == GFURY_SERIALIZER) {
            Fury fury = gFuryThreadLocal.get();
            obj = fury.deserialize(data);
            gFuryThreadLocal.remove();
        } else {
            log.error("serializerCode [{}] not found!", serializerCode);
        }
        return obj;
    }

    @Override
    public int getCode() {
        String serializerCode = "";
        switch (this.serializerCode) {
            case 3: serializerCode = SerializerCode.JFURY.name(); break;
            case 4: serializerCode = SerializerCode.GFURY.name(); break;
            case 5: serializerCode = SerializerCode.XFURY.name(); break;
            default : serializerCode = SerializerCode.JFURY.name();
        }
        return SerializerCode.valueOf(serializerCode).getCode();
    }


    public static void main(String[] args) {
        Fury fury = jFuryThreadLocal.get();
        byte[] data = fury.serialize(new RpcRequest());
        System.out.println(data);
        Object deserialize = fury.deserialize(data);
        System.out.println(deserialize);
    }

}
