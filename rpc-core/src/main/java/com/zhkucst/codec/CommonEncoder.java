package com.zhkucst.codec;

import com.zhkucst.enums.PackageType;
import com.zhkucst.protocol.RpcRequest;
import com.zhkucst.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Auther: fyp
 * @Date: 2022/3/24
 * @Description:
 * @Package: com.zhkucst.codec
 * @Version: 1.0
 */
public class CommonEncoder extends MessageToByteEncoder {

    // 对象头的魔术: cafe babe 表示 class 类型的文件
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    private final CommonSerializer serializer;

    public CommonEncoder(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * 自定义对象头 协议 16 字节
     * 4 字节 魔数
     * 4 字节 协议包类型
     * 4 字节 序列化类型
     * 4 字节 数据长度
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     *
     *       The transmission protocol is as follows :
     * +---------------+---------------+-----------------+-------------+
     * | Magic Number  | Package Type  | Serializer Type | Data Length |
     * | 4 bytes       | 4 bytes       | 4 bytes         | 4 bytes     |
     * +---------------+---------------+-----------------+-------------+
     * |                           Data Bytes                          |
     * |                       Length: ${Data Length}                  |
     * +---------------+---------------+-----------------+-------------+
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        out.writeInt(MAGIC_NUMBER); // 写进的四个字节
        if (msg instanceof RpcRequest) {
            out.writeInt(PackageType.REQUEST_PACK.getCode()); // 写进的四个字节
        } else {
            out.writeInt(PackageType.RESPONSE_PACK.getCode()); // 写进的四个字节
        }
        out.writeInt(serializer.getCode()); // 写进的四个字节
        byte[] bytes = serializer.serialize(msg);
        out.writeInt(bytes.length); // 写进的四个字节
        out.writeBytes(bytes); // 写进的 对象内容，二进制形式
    }
}
