package server.rpc;

import java.io.Serializable;

public class RpcCommand implements Serializable {
    private byte type;
    private int requestId;
    private int length;
    private byte[] content;

    public RpcCommand(byte type, int requestId, int length, byte[] content) {
        this.type = type;
        this.requestId = requestId;
        this.length = length;
        this.content = content;
    }

    public byte getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public byte[] getContent() {
        return content;
    }

    public int getRequestId() {
        return requestId;
    }
}
