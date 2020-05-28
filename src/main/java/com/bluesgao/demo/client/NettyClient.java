package com.bluesgao.demo.client;

import com.bluesgao.demo.client.handler.MessageResponseHandler;
import com.bluesgao.demo.codec.MessageCodec;
import com.bluesgao.demo.codec.MessageDecoder;
import com.bluesgao.demo.codec.MessageEncoder;
import com.bluesgao.demo.protocol.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
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
public class NettyClient implements Client {
    private static final int MAX_RETRY = 5;
    private NioEventLoopGroup workerGroup;
    private Bootstrap bootstrap;

    public NettyClient() {
        this.workerGroup = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
    }

    @Override
    public void connect(String host, Integer port) {
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast("message-decoder",new MessageDecoder());
                        ch.pipeline().addLast("response-handler",new MessageResponseHandler());
                        ch.pipeline().addLast("message-encoder",new MessageEncoder());
                    }
                });
        int retry = 0;
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
                bootstrap.config().group().schedule(() -> connect(host, port), delay, TimeUnit
                        .SECONDS);
            }
        });
    }

    @Override
    public void disConnect() {

    }

    private static void startConsoleThread(Channel channel) {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                System.out.print("输入消息发送至服务端: ");
                Scanner sc = new Scanner(System.in);
                String line = sc.nextLine();
                Message msg = new Message();
                msg.setBody(line);
                channel.writeAndFlush(msg);
            }

        }).start();
    }

    public static void main(String[] args) {
        Client client = new NettyClient();
        client.connect("127.0.0.1", 8000);
    }
}
