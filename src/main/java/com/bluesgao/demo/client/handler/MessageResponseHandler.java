package com.bluesgao.demo.client.handler;

import com.alibaba.fastjson.JSON;
import com.bluesgao.demo.protocol.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class MessageResponseHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
        log.info("MessageResponseHandler channelRead0 message:{}", JSON.toJSONString(message));
    }
}
