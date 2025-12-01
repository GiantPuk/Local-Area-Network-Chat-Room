// package server;

// import java.io.*;
// import java.net.*;
// import common.Message;

// public class ClientHandler implements Runnable {
//     private Socket socket;
//     private UserManager userManager;
//     private ObjectOutputStream output;
//     private ObjectInputStream input;
//     private String username;
    
//     public ClientHandler(Socket socket, UserManager userManager) {
//         this.socket = socket;
//         this.userManager = userManager;
//     }
    
//     public void run() {
//         try {
//             output = new ObjectOutputStream(socket.getOutputStream());
//             input = new ObjectInputStream(socket.getInputStream());
            
//             // 处理用户登录 - 支持重复用户名检查
//             boolean loginSuccess = false;
//             while (!loginSuccess && !socket.isClosed()) {
//                 Message loginMsg = (Message) input.readObject();
//                 if ("LOGIN".equals(loginMsg.getType())) {
//                     username = loginMsg.getSender();
                    
//                     // 检查用户名是否重复
//                     if (userManager.addUser(username, output)) {
//                         loginSuccess = true;
//                         // 发送登录成功消息
//                         Message successMsg = new Message("LOGIN_SUCCESS", "系统", "登录成功");
//                         output.writeObject(successMsg);
//                         output.flush();
                        
//                         // 广播用户列表和系统消息
//                         broadcastUserList();
//                         broadcastSystemMessage(username + " 加入了聊天室");
                        
//                         System.out.println("用户 " + username + " 登录成功");
//                     } else {
//                         // 发送登录失败消息（用户名重复）
//                         Message failMsg = new Message("LOGIN_FAIL", "系统", "用户名已存在，请重新输入");
//                         output.writeObject(failMsg);
//                         output.flush();
//                         System.out.println("用户 " + username + " 登录失败：用户名重复");
//                     }
//                 }
//             }
            
//             // 登录成功后开始处理消息
//             if (loginSuccess) {
//                 while (!socket.isClosed()) {
//                     Message message = (Message) input.readObject();
//                     if ("CHAT".equals(message.getType())) {
//                         userManager.broadcastMessage(message);
//                     } else if ("LOGOUT".equals(message.getType())) {
//                         break;
//                     }
//                 }
//             }
//         } catch (Exception e) {
//             System.out.println("客户端连接异常: " + e.getMessage());
//             e.printStackTrace();
//         } finally {
//             disconnect();
//         }
//     }
    
//     private void disconnect() {
//         if (username != null) {
//             userManager.removeUser(username);
//             broadcastUserList();
//             broadcastSystemMessage(username + " 离开了聊天室");
//             System.out.println("用户 " + username + " 断开连接");
//         }
//         try {
//             if (socket != null) socket.close();
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//     }
    
//     private void broadcastUserList() {
//         userManager.broadcastUserList();
//     }
    
//     private void broadcastSystemMessage(String content) {
//         userManager.broadcastMessage(new Message("SYSTEM", "系统", content));
//     }
// }
package server;

import java.io.*;
import java.net.*;
import common.Message;

public class ClientHandler implements Runnable {
    private Socket socket;
    private UserManager userManager;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String username;
    private volatile boolean running = true; // 添加运行标志
    
    public ClientHandler(Socket socket, UserManager userManager) {
        this.socket = socket;
        this.userManager = userManager;
    }
    
    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            
            // 处理用户登录 - 支持重复用户名检查
            boolean loginSuccess = false;
            while (!loginSuccess && running && !socket.isClosed()) {
                try {
                    // 设置读取超时，以便能够响应停止请求
                    socket.setSoTimeout(1000);
                    Message loginMsg = (Message) input.readObject();
                    socket.setSoTimeout(0); // 重置超时
                    
                    if ("LOGIN".equals(loginMsg.getType())) {
                        username = loginMsg.getSender();
                        
                        // 检查用户名是否重复
                        if (userManager.addUser(username, output)) {
                            loginSuccess = true;
                            // 发送登录成功消息
                            Message successMsg = new Message("LOGIN_SUCCESS", "系统", "登录成功");
                            output.writeObject(successMsg);
                            output.flush();
                            
                            // 广播用户列表和系统消息
                            broadcastUserList();
                            broadcastSystemMessage(username + " 加入了聊天室");
                            
                            System.out.println("用户 " + username + " 登录成功");
                        } else {
                            // 发送登录失败消息（用户名重复）
                            Message failMsg = new Message("LOGIN_FAIL", "系统", "用户名已存在，请重新输入");
                            output.writeObject(failMsg);
                            output.flush();
                            System.out.println("用户 " + username + " 登录失败：用户名重复");
                        }
                    }
                } catch (SocketTimeoutException e) {
                    // 超时正常，继续检查 running 标志
                    continue;
                }
            }
            
            // 登录成功后开始处理消息
            if (loginSuccess && running) {
                socket.setSoTimeout(1000); // 设置1秒超时
                while (running && !socket.isClosed()) {
                    try {
                        Message message = (Message) input.readObject();
                        if ("CHAT".equals(message.getType())) {
                            userManager.broadcastMessage(message);
                        } else if ("LOGOUT".equals(message.getType())) {
                            break;
                        }
                    } catch (SocketTimeoutException e) {
                        // 超时正常，继续检查 running 标志
                        continue;
                    } catch (EOFException e) {
                        // 客户端正常关闭连接
                        break;
                    }
                }
            }
        } catch (Exception e) {
            if (running) { // 只有在正常运行时才打印错误
                System.out.println("客户端连接异常: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            disconnect();
        }
    }
    
    // 新增：强制停止此客户端处理器
    public void stop() {
        running = false;
        disconnect();
    }
    
    private void disconnect() {
        if (username != null) {
            userManager.removeUser(username);
            broadcastUserList();
            broadcastSystemMessage(username + " 离开了聊天室");
            System.out.println("用户 " + username + " 断开连接");
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // 忽略关闭时的异常
        }
    }
    
    private void broadcastUserList() {
        userManager.broadcastUserList();
    }
    
    private void broadcastSystemMessage(String content) {
        userManager.broadcastMessage(new Message("SYSTEM", "系统", content));
    }
}