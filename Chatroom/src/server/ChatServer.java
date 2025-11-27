package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 8888;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private UserManager userManager;
    
    public ChatServer() {
        this.threadPool = Executors.newCachedThreadPool();
        this.userManager = new UserManager();
    }
    
    // 新增构造函数，支持传入UserManager
    public ChatServer(UserManager userManager) {
        this.threadPool = Executors.newCachedThreadPool();
        this.userManager = userManager;
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("聊天服务器启动，端口：" + PORT);
            
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                // 为每个客户端创建新线程
                ClientHandler clientHandler = new ClientHandler(clientSocket, userManager);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        new ChatServer().start();
    }
}