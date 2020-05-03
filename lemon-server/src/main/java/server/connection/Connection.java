package server.connection;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import server.rpc.ResponseCommand;
import server.rpc.RpcCommand;

import java.util.concurrent.ConcurrentHashMap;

public class Connection {

    private String appName;

    public static final AttributeKey<Connection> CONNECTION = AttributeKey.valueOf("connection");

    private final ConcurrentHashMap<Integer, InvokeFuture> invokeFutureMap  = new ConcurrentHashMap<Integer, InvokeFuture>(
            4);

    private Channel channel;

    public Connection(String appName, Channel channel) {
        this.channel = channel;
        this.appName = appName;
        channel.attr(Connection.CONNECTION).set(this);
    }

    public void putResponse (ResponseCommand response) {
        int requestId = response.getRequestId();
        InvokeFuture invokeFuture = invokeFutureMap.get(requestId);
        invokeFuture.putResponse(response);
    }

    public RpcCommand getResponse(Integer reqId) {
        return invokeFutureMap.get(reqId).waitResponse();
    }

    public Channel channel(){
        return this.channel;
    }

    public String getAppName() {
        return appName;
    }

    public void add(InvokeFuture invokeFuture) {
        invokeFutureMap.put(invokeFuture.getReqId(), invokeFuture);
    }
}
