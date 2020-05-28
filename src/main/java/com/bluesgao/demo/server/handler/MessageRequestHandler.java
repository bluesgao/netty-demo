package com.bluesgao.demo.server.handler;

import com.alibaba.fastjson.JSON;
import com.bluesgao.demo.protocol.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageRequestHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        log.info("MessageRequestHandler channelRead0 message:{}", JSON.toJSONString(message));
        //todo 业务处理

        //构造返回消息
        Message resp = new Message();
        resp.setBody("我收到了你的消息：" + message.getBody());
        ctx.channel().writeAndFlush(resp);
    }
}
