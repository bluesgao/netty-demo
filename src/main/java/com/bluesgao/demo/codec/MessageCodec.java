package com.bluesgao.demo.codec;

import com.bluesgao.demo.protocol.Message;
import com.bluesgao.demo.serialize.JsonSerializer;
import com.bluesgao.demo.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
public class MessageCodec {
    private static final int MAGIC_NUMBER = 0x12345678;
    private static final Map<Byte, Serializer> SERIALIZER_MAP;
    public static final MessageCodec INSTANCE = new MessageCodec();
    static {
        SERIALIZER_MAP = new HashMap<>();
        Serializer serializer = new JsonSerializer();
        SERIALIZER_MAP.put(serializer.getSerializerAlogrithm(), serializer);
    }

    public ByteBuf encode(ByteBuf byteBuf, Message msg) {
        //2,序列化msg对象
        byte[] bytes = Serializer.DEFAULT.serialize(msg);

        // 3. 实际编码过程
        byteBuf.writeInt(MAGIC_NUMBER);//魔数
        byteBuf.writeByte(1);//版本
        byteBuf.writeByte(Serializer.DEFAULT.getSerializerAlogrithm());//序列化算法
        byteBuf.writeInt(bytes.length);//data长度
        byteBuf.writeBytes(bytes);//body
        return byteBuf;
    }

    public Message decode(ByteBuf buf) {
        // 跳过 magic number
        //buf.skipBytes(4);
        int magic = buf.readInt();
        // 跳过版本号
        byte version = buf.readByte();
        //buf.skipBytes(1);
        // 序列化算法
        byte serializeAlgorithm = buf.readByte();
        // 数据包长度
        int length = buf.readInt();
        log.info("decode magic:{},version:{},serializeAlgorithm:{},length:{}",magic,version,serializeAlgorithm,length);
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        //获取序列化算法
        Serializer serializer = getSerializer(serializeAlgorithm);
        //反序列化
        return serializer.deserialize(Message.class, bytes);
    }

    private Serializer getSerializer(byte serializeAlgorithm) {
        return SERIALIZER_MAP.get(serializeAlgorithm);
    }
}
