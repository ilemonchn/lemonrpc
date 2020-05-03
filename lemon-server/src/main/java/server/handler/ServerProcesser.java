package server.handler;

import server.rpc.RequestCommand;
import server.rpc.ResponseCommand;

public interface ServerProcesser {
    ResponseCommand process(RequestCommand requestCommand);
}
