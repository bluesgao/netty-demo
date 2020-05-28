package com.bluesgao.demo.server;

import com.bluesgao.demo.codec.MessageDecoder;
import com.bluesgao.demo.codec.MessageEncoder;
import com.bluesgao.demo.server.handler.MessageRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class NettyServer implements Server {
    private static final int PORT = 8000;
    private NioEventLoopGroup bossGroup = null;
    private NioEventLoopGroup workerGroup = null;

    public NettyServer() {
        if (bossGroup == null) {
            bossGroup = new NioEventLoopGroup();
        }
        if (workerGroup == null) {
            workerGroup = new NioEventLoopGroup();
        }
    }

    @Override
    public void start() {
        final ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline().addLast("message-decoder", new MessageDecoder()); //解码
                        ch.pipeline().addLast("message-encoder", new MessageEncoder()); //编码
                        ch.pipeline().addLast("message-handler", new MessageRequestHandler());
                    }
                });


        serverBootstrap.bind(PORT).addListener(future -> {
            if (future.isSuccess()) {
                log.info("端口[" + PORT + "]绑定成功!");
            } else {
                log.info("端口[" + PORT + "]绑定失败!");
            }
        });
    }

    @Override
    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
    }

    public static void main(String[] args) {
        Server server = new NettyServer();
        server.start();
    }
}
