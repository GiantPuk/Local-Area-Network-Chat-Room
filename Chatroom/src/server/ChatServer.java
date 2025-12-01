package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import common.Message;

public class ChatServer {
    private static final int PORT = 8888;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private AtomicBoolean isRunning; 
    private UserManager userManager;
    
    public ChatServer() {
        this.threadPool = Executors.newCachedThreadPool();
        this.userManager = new UserManager();
    }
    
    // 新增构造函数，支持传入UserManager
    public ChatServer(UserManager userManager) {
        this.threadPool = Executors.newCachedThreadPool();
        this.userManager = userManager;
        this.isRunning = new AtomicBoolean(false); // 初始化
    }
    
    public void start() {
        try {
            isRunning.set(true); // 设置为运行状态
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
    public void stopServer() {
        if (!isRunning.get()) {
            return; // 服务器已经在停止状态
        }
        
        isRunning.set(false); // 设置停止标志
        System.out.println("正在停止服务器...");
        
        try {
            // 1. 关闭 ServerSocket（这会中断 serverSocket.accept()）
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("ServerSocket 已关闭");
            }
            
            // 2. 通知所有用户服务器关闭
            userManager.broadcastMessage(new Message("FORCE_LOGOUT", "服务器", "服务器正在关闭..."));
            
            // 3. 关闭线程池（等待现有任务完成）
            if (threadPool != null && !threadPool.isShutdown()) {
                threadPool.shutdown();
                try {
                    // 等待现有客户端处理线程完成（最多5秒）
                    if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                        threadPool.shutdownNow(); // 强制终止
                    }
                } catch (InterruptedException e) {
                    threadPool.shutdownNow();
                }
                System.out.println("线程池已关闭");
            }
            
            System.out.println("服务器已成功停止");
            
        } catch (IOException e) {
            System.out.println("停止服务器时发生错误: " + e.getMessage());
        }
    }
    
    // 新增：检查服务器是否正在运行
    public boolean isRunning() {
        return isRunning.get();
    }
    
    public static void main(String[] args) {
        new ChatServer().start();
    }
}