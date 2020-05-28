package com.bluesgao.demo.codec;

import com.alibaba.fastjson.JSON;
import com.bluesgao.demo.protocol.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf) throws Exception {
        log.info("编码 message:{}", JSON.toJSONString(message));
        MessageCodec.INSTANCE.encode(byteBuf, message);
    }
}
