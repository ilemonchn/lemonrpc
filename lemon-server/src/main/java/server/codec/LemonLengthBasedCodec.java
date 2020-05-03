package server.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.MessageToByteEncoder;
import server.rpc.RequestCommand;
import server.rpc.ResponseCommand;
import server.rpc.RpcCommand;
import server.rpc.RpcCommandType;

import java.io.Serializable;

public class LemonLengthBasedCodec implements Codec {

    public ChannelHandler newEncoder() {
        return new LemonLengthBasedEncoder();
    }

    public ChannelHandler newDecoder() {
        return new LemonLengthBasedDecoder();
    }

    public final class LemonLengthBasedEncoder extends MessageToByteEncoder<Serializable> {

        protected void encode(ChannelHandlerContext channelHandlerContext, Serializable msg, ByteBuf out) throws Exception {
            if (msg instanceof RpcCommand) {
                RpcCommand command = (RpcCommand) msg;
                out.writeByte(command.getType());
                out.writeInt(command.getRequestId());
                out.writeInt(command.getLength());
                out.writeBytes(command.getContent());
            }
        }
    }

    public final class LemonLengthBasedDecoder extends ChannelInboundHandlerAdapter {

        private ByteBuf cumulator;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (!(msg instanceof ByteBuf)) {
                ctx.fireChannelRead(msg);
                return;
            }
            ByteBuf in = (ByteBuf) msg;
            if (cumulator == null) {
                cumulator = in;
            } else {
                ByteBuf oldBuf = cumulator;
                ByteBuf newBuf = ctx.alloc().buffer(cumulator.readableBytes() + in.readableBytes());
                newBuf.writeBytes(oldBuf);
                newBuf.writeBytes(in);
                cumulator = newBuf;
                in.release();
                oldBuf.release();
            }

            while (cumulator.isReadable()) {
                if (cumulator.readableBytes() <= 4 + 4 + 1) {
                    return;
                }
                cumulator.markReaderIndex();
                byte type = cumulator.readByte();
                int requestId = cumulator.readInt();
                int length = cumulator.readInt();
                if (cumulator.readableBytes() < length) {
                    cumulator.resetReaderIndex();
                    return;
                }

                byte[] content = new byte[length];
                cumulator.readBytes(content);
                RpcCommand command;
                if (type == RpcCommandType.REQUEST.value()) {
                    command = new RequestCommand(requestId, length, content);
                } else {
                    command = new ResponseCommand(requestId, length, content);
                }
                ctx.fireChannelRead(command);
                cumulator.discardReadBytes();
            }
        }
    }

}
