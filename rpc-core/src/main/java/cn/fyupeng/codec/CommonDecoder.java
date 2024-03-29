package cn.fyupeng.codec;

import cn.fyupeng.enums.PackageType;
import cn.fyupeng.exception.UnrecognizedException;
import cn.fyupeng.protocol.RpcRequest;
import cn.fyupeng.protocol.RpcResponse;
import cn.fyupeng.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Auther: fyp
 * @Date: 2022/3/24
 * @Description: ByteToMessage 解码器（入站 handler）
 * @Package: cn.fyupeng.codec
 * @Version: 1.0
 */
@Slf4j
public class CommonDecoder extends ReplayingDecoder {

    // 对象头的魔术: cafe babe 表示 class 类型的文件
    //private static final int MAGIC_NUMBER = 0xCAFEBABE;
    private static final short MAGIC_NUMBER = (short) 0xBABE;


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws UnrecognizedException {

        short magic = in.readShort(); // 读取 2 字节 魔数
        if (magic != MAGIC_NUMBER) {
            log.error("Unrecognized protocol package: {}", Integer.toHexString(magic));
            throw new UnrecognizedException("Unrecognized protocol package error");
        }
        int packageCode = in.readByte(); // 读取 1 字节 协议包类型
        Class<?> packageClass;
        if (packageCode == PackageType.REQUEST_PACK.getCode()) {
            packageClass = RpcRequest.class;
        } else if (packageCode == PackageType.RESPONSE_PACK.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            log.error("Unrecognized data package: {}", packageCode);
            throw new UnrecognizedException("Unrecognized data package error");
        }
        int serializerCode = in.readByte(); // 读取 1 字节 序列化 类型
        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);
        if (serializer == null) {
            log.error("Unrecognized deserializer : {}", serializerCode);
            throw new UnrecognizedException("Unrecognized deserializer error");
        }
        int length = in.readInt();// 读取 4 字节 数据长度
        byte[] bytes = new byte[length];
        log.debug("decode object length [{}]", length);
        in.readBytes(bytes);
        // 自定义 反序列化器 对 二进制 反序列化 为 实例
        log.debug("serializer [{}] deserialize [{}]", serializer.getClass().getName(), packageClass.getName());
        Object obj = serializer.deserialize(bytes, packageClass);
        // 接着 传给 下一个 处理器 NettyServerHandler
        out.add(obj);
    }
}
