package server;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import common.Message;

public class UserManager {
    private ConcurrentHashMap<String, ObjectOutputStream> users;
    
    public UserManager() {
        this.users = new ConcurrentHashMap<>();
    }
    
    // 添加用户名检查方法
    public boolean isUsernameTaken(String username) {
        return users.containsKey(username);
    }
    
    public boolean addUser(String username, ObjectOutputStream output) {
        if (isUsernameTaken(username)) {
            return false; // 用户名已存在
        }
        users.put(username, output);
        return true; // 添加成功
    }
    
    public void removeUser(String username) {
        // 先尝试发送下线通知
        ObjectOutputStream output = users.get(username);
        if (output != null) {
            try {
                Message logoutMsg = new Message("LOGOUT", "系统", "您已下线");
                output.writeObject(logoutMsg);
                output.flush();
            } catch (IOException e) {
                // 忽略异常
            }
        }
        users.remove(username);
    }
    
    // 踢出特定用户
    public void kickUser(String username) {
        ObjectOutputStream output = users.get(username);
        if (output != null) {
            try {
                Message kickMsg = new Message("FORCE_LOGOUT", "系统", "您已被管理员踢出");
                output.writeObject(kickMsg);
                output.flush();
                // 关闭输出流
                output.close();
            } catch (IOException e) {
                // 忽略异常
            }
        }
        users.remove(username);
        broadcastUserList();
        broadcastSystemMessage("用户 " + username + " 已被踢出"); // 现在这个方法存在了
    }
    
    // 广播系统消息的便捷方法
    public void broadcastSystemMessage(String content) {
        broadcastMessage(new Message("SYSTEM", "系统", content));
    }
    
    public void broadcastMessage(Message message) {
        Iterator<Map.Entry<String, ObjectOutputStream>> iterator = users.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ObjectOutputStream> entry = iterator.next();
            try {
                entry.getValue().writeObject(message);
                entry.getValue().flush();
            } catch (IOException e) {
                // 如果发送失败，可能是连接已断开，从列表中移除
                iterator.remove();
            }
        }
    }
    
    public void broadcastUserList() {
        List<String> userList = new ArrayList<>(users.keySet());
        Message userListMsg = new Message("USER_LIST", "系统", 
            String.join(",", userList));
        broadcastMessage(userListMsg);
    }
    
    // 获取所有在线用户
    public List<String> getOnlineUsers() {
        return new ArrayList<>(users.keySet());
    }
    
    // 新增：获取用户输出流
    public ObjectOutputStream getUserOutputStream(String username) {
        return users.get(username);
    }
}