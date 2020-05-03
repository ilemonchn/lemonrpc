package server.rpc;

public class RequestCommand extends RpcCommand {
    public RequestCommand(int requestId, int length, byte[] content){
        super(RpcCommandType.REQUEST.value(), requestId, length, content);
    }
}
