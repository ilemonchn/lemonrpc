package server.rpc;

public class ResponseCommand extends RpcCommand {

    public ResponseCommand(int requestId, int length, byte[] content){
        super(RpcCommandType.RESPONSE.value(), requestId, length, content);
    }


}
