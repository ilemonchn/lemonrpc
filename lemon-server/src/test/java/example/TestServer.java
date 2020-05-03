package example;

import server.LemonServer;
import server.RpcClientProxy;
import server.Server;

import java.util.concurrent.TimeUnit;

public class TestServer {
    public static void main(String[] args) throws Exception {
        HelloService helloService = new HelloServiceImpl();
        Server server = new LemonServer(helloService);
        server.start();

        TimeUnit.SECONDS.sleep(1);

        RpcClientProxy<HelloService> proxy = new RpcClientProxy<>();
        proxy.setAppName("lemonTest");
        proxy.setServiceInterface(HelloService.class);
        proxy.setTargetIP("127.0.0.1");
        proxy.setTargetPort(11170);
        proxy.init();
        HelloService client = proxy.getObject();
        int age = 0;
        while (true) {
            User user = new User("lemon", age);
            age++;
            System.out.println("client send:" + user);
            System.out.println("client resp:" + client.sayHello(user));
            System.out.println("------------------------------------");
            TimeUnit.SECONDS.sleep(1);
        }
    }
}
