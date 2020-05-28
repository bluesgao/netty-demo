package com.bluesgao.demo.simple;

import com.bluesgao.demo.codec.MessageCodec;
import com.bluesgao.demo.protocol.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
public class DemoClient {
    private static final int MAX_RETRY = 5;
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8000;


    public static void main(String[] args) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new SimpleChannelInboundHandler(){
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                System.out.println(new Date() + ": 客户端激活");

//                                // 创建登录对象
//                                Message msg = new Message();
//                                msg.setBody("aaaaaaaaaaaaaaaaaaaaaaaaaa");
//
//                                // 编码
//                                ByteBuf buffer = MessageCodec.INSTANCE.encode(ctx.alloc().ioBuffer(), msg);
//
//                                // 写数据
//                                ctx.channel().writeAndFlush(buffer);
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                System.out.println("异常关闭:"+cause.getMessage());
                                ctx.close();
                            }

                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
                                ByteBuf byteBuf = (ByteBuf) o;
                                System.out.println("收到服务端回复数据: "  + byteBuf.toString(Charset.forName("utf-8")));
                            }
                        });
                    }
                });

        connect(bootstrap, HOST, PORT, MAX_RETRY);
    }

    private static void connect(Bootstrap bootstrap, String host, int port, int retry) {
        bootstrap.connect(host, port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println(new Date() + ": 连接成功，启动控制台线程……");
                Channel channel = ((ChannelFuture) future).channel();
                startConsoleThread(channel);
            } else if (retry == 0) {
                System.err.println("重试次数已用完，放弃连接！");
            } else {
                // 第几次重连
                int order = (MAX_RETRY - retry) + 1;
                // 本次重连的间隔
                int delay = 1 << order;
                System.err.println(new Date() + ": 连接失败，第" + order + "次重连……");
                bootstrap.config().group().schedule(() -> connect(bootstrap, host, port, retry - 1), delay, TimeUnit
                        .SECONDS);
            }
        });
    }

    private static void startConsoleThread(Channel channel) {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                System.out.println("输入消息发送至服务端: ");
                Scanner sc = new Scanner(System.in);
                String line = sc.nextLine();
                Message msg = new Message();
                msg.setBody(line);
                ByteBuf byteBuf = MessageCodec.INSTANCE.encode(channel.alloc().ioBuffer(), msg);
                log.info("send:"+byteBuf.toString(Charset.forName("utf-8")));
                channel.writeAndFlush(byteBuf);
            }

        }).start();
    }
}

