package client;

import java.io.*;
import common.Message;

public class MessageReceiver implements Runnable {
    private ObjectInputStream input;
    private ClientGUI gui;
    private boolean running;
    
    public MessageReceiver(ObjectInputStream input, ClientGUI gui) {
        this.input = input;
        this.gui = gui;
        this.running = true;
    }
    
    public void run() {
        try {
            while (running) {
                Message message = (Message) input.readObject();
                handleMessage(message);
            }
        } catch (EOFException e) {
            System.out.println("服务器连接已关闭");
        } catch (IOException | ClassNotFoundException e) {
            if (running) {
                e.printStackTrace();
            }
        }
    }
    
    private void handleMessage(Message message) {
        switch (message.getType()) {
            case "CHAT":
            case "SYSTEM":
                gui.appendMessage(message);
                break;
            case "USER_LIST":
                String[] users = message.getContent().split(",");
                gui.updateUserList(users);
                break;
            case "LOGIN_SUCCESS":
                // 登录成功，不需要特殊处理，已经在ChatClient中处理了
                break;
            case "LOGIN_FAIL":
                // 登录失败，已经在ChatClient中处理了
                break;
        }
    }
    
    public void stop() {
        running = false;
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}