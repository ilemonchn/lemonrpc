package server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import server.codec.Codec;
import server.codec.LemonLengthBasedCodec;
import server.connection.Connection;
import server.connection.ConnectionHolder;
import server.handler.RpcHandler;
import server.handler.ServerIdleHandler;
import server.proxy.RpcInvoker;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class RpcClientProxy<T> {

    private String appName;
    private String targetIP;
    private int targetPort;
    private Class<T> serviceInterface;

    public void init() {
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup workGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2,
                new ThreadFactoryBuilder().setNameFormat("nettyClient-work-%d").setDaemon(true).build());
        final Codec codec = new LemonLengthBasedCodec();
        bootstrap.group(workGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(
                        32768, 65536))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 500)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel channel) {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast("decoder", codec.newDecoder());
                        pipeline.addLast("encoder", codec.newEncoder());

                        pipeline.addLast("idleStateHandler", new IdleStateHandler(50000, 50000, 0,
                                TimeUnit.MILLISECONDS));
                        pipeline.addLast("serverIdleHandler", new ServerIdleHandler());
                        pipeline.addLast("handler", new RpcHandler(false));
                    }
                });
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(targetIP, targetPort));
        future.awaitUninterruptibly();
        if (!future.isSuccess()) {
            System.out.println("客户端连接失败, ip:"+targetIP+" port:"+targetPort);
        }
        System.out.println("客户端连接建立成功, ip:"+targetIP+" port:"+targetPort);
        System.out.println();
        Channel channel = future.channel();
        Connection connection = new Connection(appName, channel);
        ConnectionHolder.putConnection(connection);
    }

    public T getObject() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Object proxyInstance = Proxy.newProxyInstance(classLoader, new Class[]{serviceInterface}, new RpcInvoker(appName));
        return (T) proxyInstance;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTargetIP() {
        return targetIP;
    }

    public void setTargetIP(String targetIP) {
        this.targetIP = targetIP;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    public Class<T> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<T> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }
}
