package server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import server.codec.Codec;
import server.codec.LemonLengthBasedCodec;
import server.handler.DefaultServerProcesser;
import server.handler.RpcHandler;
import server.handler.ServerIdleHandler;

import java.util.concurrent.TimeUnit;

public class LemonServer implements Server{

    private Object serviceImpl;

    public LemonServer(Object serviceImpl) {
        this.serviceImpl = serviceImpl;
    }

    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1,
                new ThreadFactoryBuilder().setNameFormat("nettyServer-boss-%d").setDaemon(false).build());
        EventLoopGroup workGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2,
                new ThreadFactoryBuilder().setNameFormat("nettyServer-work-%d").setDaemon(true).build());
        final Codec codec = new LemonLengthBasedCodec();

        bootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(
                        32768, 65536))
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel channel) {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast("decoder", codec.newDecoder());
                        pipeline.addLast("encoder", codec.newEncoder());
                        pipeline.addLast("idleStateHandler", new IdleStateHandler(0, 0, 600000,
                                    TimeUnit.MILLISECONDS));
                        pipeline.addLast("serverIdleHandler", new ServerIdleHandler());
                        pipeline.addLast("handler", new RpcHandler(true, new DefaultServerProcesser(serviceImpl)));
                    }
                });

        ChannelFuture channelFuture = bootstrap.bind( 11170).syncUninterruptibly();
        if (channelFuture.isSuccess()) {
            System.out.println("Server boot success on 11170");
            System.out.println();
        }
    }

    public void stop() {

    }
}
