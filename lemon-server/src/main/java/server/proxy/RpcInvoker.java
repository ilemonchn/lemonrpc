package server.proxy;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import server.connection.Connection;
import server.connection.ConnectionHolder;
import server.connection.InvokeFuture;
import server.rpc.RequestCommand;
import server.rpc.RpcCommand;
import server.util.IDGen;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

public class RpcInvoker implements InvocationHandler {

    private String appName;

    public RpcInvoker(String appName) {
        this.appName = appName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        byte[] content = JSON.toJSONString(args[0]).getBytes(Charset.forName("utf-8"));
        int length = content.length;
        Integer reqId = IDGen.genReqId();
        RpcCommand command = new RequestCommand(reqId, length, content);
        Connection connection = ConnectionHolder.getConnection(appName);
        connection.channel().writeAndFlush(command).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (!channelFuture.isSuccess()) {
                    // remove invokeFuture
                }
            }
        });
        InvokeFuture invokeFuture = new InvokeFuture(reqId);
        connection.add(invokeFuture);
        RpcCommand response = connection.getResponse(reqId);
        Object ret = JSON.parseObject(new String(response.getContent(), Charset.forName("utf-8")), method.getReturnType());
        return ret;
    }
}
