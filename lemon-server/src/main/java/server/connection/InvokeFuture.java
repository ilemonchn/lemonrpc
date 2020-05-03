package server.connection;

import server.rpc.ResponseCommand;

import java.util.concurrent.CountDownLatch;

public class InvokeFuture {
    private Integer reqId;

    public InvokeFuture(Integer reqId) {
        this.reqId = reqId;
    }

    private volatile ResponseCommand response;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public void putResponse(ResponseCommand response) {
        this.response = response;
        countDownLatch.countDown();
    }

    public ResponseCommand waitResponse() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

    public Integer getReqId() {
        return reqId;
    }
}
