package server.connection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionHolder {
    private static Map<String, LinkedBlockingQueue<Connection>> connectionMap = new ConcurrentHashMap<String, LinkedBlockingQueue<Connection>>();

    public static void putConnection(Connection connection){
        LinkedBlockingQueue<Connection> connections = connectionMap.computeIfAbsent(connection.getAppName(),
                c -> new LinkedBlockingQueue<>());
        connections.offer(connection);
    }

    public static Connection getConnection(String key){
        return connectionMap.get(key).peek();
    }


}
