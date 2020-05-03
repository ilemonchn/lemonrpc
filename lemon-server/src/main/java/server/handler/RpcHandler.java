package server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import server.connection.Connection;
import server.rpc.RequestCommand;
import server.rpc.ResponseCommand;

public class RpcHandler extends ChannelInboundHandlerAdapter {

    private boolean serverSide;

    private ServerProcesser serverProcesser;

    public RpcHandler (boolean serverSide) {
        this.serverSide = serverSide;
    }

    public RpcHandler (boolean serverSide, ServerProcesser serverProcesser) {
        this.serverSide = serverSide;
        this.serverProcesser = serverProcesser;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (serverSide) {
            RequestCommand requestCommand = (RequestCommand) msg;
            ResponseCommand ret = serverProcesser.process(requestCommand);
            ctx.channel().writeAndFlush(ret);
        } else {
            ResponseCommand resp = (ResponseCommand) msg;
            Connection connection = ctx.channel().attr(Connection.CONNECTION).get();
            connection.putResponse(resp);
        }
    }

}
