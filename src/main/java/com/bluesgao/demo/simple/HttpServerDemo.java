package com.bluesgao.demo.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpServerDemo {
    private int port;

    public HttpServerDemo(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new ChannelInitializer<NioServerSocketChannel>() {
                        @Override
                        protected void initChannel(NioServerSocketChannel ch) throws Exception {
                            log.info("ServerBootstrap handler");
                        }
                    })
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // server端接收到的是httpRequest，所以要使用HttpRequestDecoder进行解码
                            ch.pipeline().addLast("http-decoder", new HttpRequestDecoder());
                            //将多个消息转换为单一的FullHttpRequest或FullHttpResponse对象
                            ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65535));
                            // server端发送的是httpResponse，所以要使用HttpResponseEncoder进行编码
                            ch.pipeline().addLast("http-encoder", new HttpResponseEncoder());
                            //解决大数据包传输问题，用于支持异步写大量数据流并且不需要消耗大量内存也不会导致内存溢出错误( OutOfMemoryError )。
                            //仅支持ChunkedInput类型的消息。也就是说，仅当消息类型是ChunkedInput时才能实现ChunkedWriteHandler提供的大数据包传输功能
                            ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());//解决大码流的问题
                            ch.pipeline().addLast("http-server", new SimpleChannelInboundHandler<FullHttpRequest>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
                                    // 获取请求的uri
                                    String uri = req.uri();
                                    String msg = "<html><head><title>DEMO</title></head><body>你请求uri为：" + uri + "</body></html>";
                                    // 创建http响应
                                    FullHttpResponse response = new DefaultFullHttpResponse(
                                            HttpVersion.HTTP_1_1,
                                            HttpResponseStatus.OK,
                                            Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
                                    // 设置头信息
                                    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
                                    //response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
                                    // 将html write到客户端
                                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                                }
                            });
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定并开始接受传入的连接。
            ChannelFuture f = serverBootstrap.bind(port).sync();
            log.info(HttpServerDemo.class.getName() + " started and listen on " + f.channel().localAddress());
            //等到服务器socket关闭。
            // 在这个例子中，这种情况不会发生，但你可以优雅的做到这一点
            // 关闭服务器.
            //sync()会同步等待连接操作结果，用户线程将在此wait()，直到连接操作完成之后，线程被notify(),用户代码继续执行
            //closeFuture()当Channel关闭时返回一个ChannelFuture,用于链路检测
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        int port = 8888;
        try {
            new HttpServerDemo(port).start();
        } catch (Exception e) {

        }
    }
}
