package com.zhkucst.util;

import com.zhkucst.enums.PackageType;
import com.zhkucst.exception.UnrecognizedException;
import com.zhkucst.protocol.RpcRequest;
import com.zhkucst.protocol.RpcResponse;
import com.zhkucst.serializer.CommonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Auther: fyp
 * @Date: 2022/3/27
 * @Description:
 * @Package: com.zhkucst.util
 * @Version: 1.0
 */
@Slf4j
public class ObjectReader {

    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    public static Object readObject(InputStream in) throws IOException, UnrecognizedException {
        byte[] numberBytes = new byte[4];
        in.read(numberBytes);
        int magic = bytes2Int(numberBytes);
        if (magic != MAGIC_NUMBER) {
            log.error("Unrecognized protocol package: {}", magic);
            throw new UnrecognizedException("Unrecognized protocol package error");
        }
        in.read(numberBytes);
        int packageCode = bytes2Int(numberBytes);
        Class<?> packageClass;
        if (packageCode == PackageType.REQUEST_PACK.getCode()){
            packageClass = RpcRequest.class;
        } else if (packageCode == PackageType.RESPONSE_PACK.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            log.error("Unrecognized data package: {}", packageCode);
            throw new UnrecognizedException("Unrecognized data package error");
        }
        in.read(numberBytes);
        int serializerCode = bytes2Int(numberBytes);
        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);
        if (serializer == null) {
            log.error("Unrecognized deserializer : {}", serializerCode);
            throw new UnrecognizedException("Unrecognized deserializer error");
        }
        in.read(numberBytes);
        int length = bytes2Int(numberBytes);
        byte[] bytes = new byte[length];
        in.read(bytes, 0, length);
        return serializer.deserialize(bytes, packageClass);
    }

    private static int bytes2Int(byte[] value) {
        int result = 0;
        int mark = 0xFF;
        if (value.length == 4) {
            int a = (value[0] & mark) << 24;
            int b = (value[1] & mark) << 16;
            int c = (value[2] & mark) << 8;
            int d = value[3] & mark;
            result = a | b | c | d;
        } else {
            log.error("Illegal size in bytes");
        }
        return result;
    }

}
