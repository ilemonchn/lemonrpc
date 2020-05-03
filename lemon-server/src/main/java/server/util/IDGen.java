package server.util;

import java.util.concurrent.atomic.AtomicInteger;

public class IDGen {
    private static AtomicInteger uniqueId = new AtomicInteger(0);

    public static Integer genReqId(){
        return uniqueId.incrementAndGet();
    }

}
