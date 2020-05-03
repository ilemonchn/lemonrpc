package server.handler;

import com.alibaba.fastjson.JSON;
import server.rpc.RequestCommand;
import server.rpc.ResponseCommand;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultServerProcesser implements ServerProcesser {

    private Object target;
    private final Map<String, Method> cacheMethodMap = new ConcurrentHashMap<>();

    public DefaultServerProcesser(Object target) {
        this.target = target;
        for (Method method: target.getClass().getDeclaredMethods()) {
            cacheMethodMap.put(method.getName(), method);
        }
    }

    @Override
    public ResponseCommand process(RequestCommand requestCommand) {
        Method method = cacheMethodMap.get("sayHello");
        Class<?> parameterClass = method.getParameterTypes()[0];
        Object param = JSON.parseObject(new String(requestCommand.getContent(), Charset.forName("utf-8")), parameterClass);
        Object result = null;
        try {
            result = method.invoke(target, param);
        } catch (Exception e) {
            System.out.println("invoke error");
        }
        byte[] content = JSON.toJSONString(result).getBytes(Charset.forName("utf-8"));
        ResponseCommand resp = new ResponseCommand(requestCommand.getRequestId(),
                content.length, content);
        return resp;
    }
}
