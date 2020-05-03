package example;

public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(User user) {
        System.out.println("server port receive user:" + user);
        return "hi " + user.getName() + user.getAge();
    }
}
