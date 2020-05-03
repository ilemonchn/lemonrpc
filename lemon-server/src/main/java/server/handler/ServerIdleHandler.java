package server.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;

public class ServerIdleHandler extends ChannelDuplexHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            try {
                System.out.println("Idle Connection is closed " + ctx.channel().remoteAddress());
                ctx.close();
            } catch (Exception ex) {
                System.out.println("Idle close exception " + ex);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
