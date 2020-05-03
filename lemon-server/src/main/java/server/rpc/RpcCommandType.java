package server.rpc;

public enum  RpcCommandType {
    REQUEST((byte)1),
    RESPONSE((byte)2);

    private byte value;
    RpcCommandType(byte value) {
        this.value = value;
    }

    public byte value() {
        return this.value;
    }

}
