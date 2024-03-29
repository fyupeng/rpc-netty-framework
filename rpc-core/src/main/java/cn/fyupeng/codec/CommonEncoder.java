package cn.fyupeng.codec;

import cn.fyupeng.enums.PackageType;
import cn.fyupeng.protocol.RpcRequest;
import cn.fyupeng.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @Auther: fyp
 * @Date: 2022/3/24
 * @Description:
 * @Package: cn.fyupeng.codec
 * @Version: 1.0
 */
@Slf4j
public class CommonEncoder extends MessageToByteEncoder {

    // 对象头的魔术: cafe babe 表示 class 类型的文件
    //private static final int MAGIC_NUMBER = 0xCAFEBABE;
    private static final short MAGIC_NUMBER = (short) 0xBABE;

    private final CommonSerializer serializer;
    private final String delimiter;

    public CommonEncoder(CommonSerializer serializer, String delimiter) {
        this.serializer = serializer;
        this.delimiter = delimiter;
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
     *
     *                            ↓  改良  ↓
     *
     * 自定义对象头 协议 8 字节
     * 2 字节 魔数
     * 1 字节 协议包类型
     * 1 字节 序列化类型
     * 4 字节 数据长度
     * +---------------+---------------+-----------------+-------------+
     * | Magic Number  | Package Type  | Serializer Type | Data Length |
     * | 2 bytes       | 1 bytes       | 1 bytes         | 4 bytes     |
     * +---------------+---------------+-----------------+-------------+
     * |                           Data Bytes                          |
     * |                       Length: ${Data Length}                  |
     * +---------------+---------------+-----------------+-------------+
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        out.writeShort(MAGIC_NUMBER); // 写进的2个字节
        if (msg instanceof RpcRequest) {
            out.writeByte(PackageType.REQUEST_PACK.getCode()); // 写进的1个字节
        } else {
            out.writeByte(PackageType.RESPONSE_PACK.getCode()); // 写进的1个字节
        }
        out.writeByte(serializer.getCode()); // 写进的1个字节
        byte[] bytes = serializer.serialize(msg);
        int length = bytes.length;
        out.writeInt(bytes.length); // 写进的4个字节
        log.debug("encode object length [{}] bytes", length);
        out.writeBytes(bytes); // 写进的 对象内容，二进制形式
        out.writeBytes(this.delimiter.getBytes(StandardCharsets.UTF_8));
    }
}
