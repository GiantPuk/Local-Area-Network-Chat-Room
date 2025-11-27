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
        users.remove(username);
    }
    
    public void broadcastMessage(Message message) {
        users.forEach((username, output) -> {
            try {
                output.writeObject(message);
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
}