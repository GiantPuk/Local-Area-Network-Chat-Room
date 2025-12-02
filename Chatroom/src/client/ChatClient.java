package client;

import java.io.*;
import java.net.*;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.awt.*;
import java.awt.event.*; // 需要添加这个导入
import common.Message;

public class ChatClient {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String username;
    private ClientGUI gui;
    private MessageReceiver receiver;
    private volatile boolean connected = false;
    
    public void connect(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            connected = true;
            
            System.out.println("连接到服务器成功");
            
            // 启动GUI
            gui = new ClientGUI(this);
            gui.setVisible(true);
            
            // 登录流程
            boolean loginSuccess = performLogin();
            
            if (loginSuccess) {
                // 启动消息接收线程
                receiver = new MessageReceiver(input, gui);
                new Thread(receiver).start();
                System.out.println("登录成功，开始接收消息");
            } else {
                disconnect();
            }
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, 
                "连接服务器失败: " + e.getMessage() + 
                "\n请确保:\n1. 服务器已启动\n2. 防火墙已允许Java网络连接\n3. 使用正确的服务器IP地址", 
                "连接失败", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    private boolean performLogin() {
        while (connected && !socket.isClosed()) {
            // 显示登录对话框
            gui.showLoginDialog();
            if (username == null || username.trim().isEmpty()) {
                // 用户取消登录
                return false;
            }
            
            try {
                // 发送登录消息
                sendLoginMessage();
                System.out.println("已发送登录请求，用户名: " + username);
                
                // 等待服务器响应（设置超时时间）
                socket.setSoTimeout(5000); // 5秒超时
                Message response = (Message) input.readObject();
                socket.setSoTimeout(0); // 重置超时
                
                if ("LOGIN_SUCCESS".equals(response.getType())) {
                    JOptionPane.showMessageDialog(gui, "登录成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
                    return true;
                } else if ("LOGIN_FAIL".equals(response.getType())) {
                    JOptionPane.showMessageDialog(gui, response.getContent(), "登录失败", JOptionPane.WARNING_MESSAGE);
                    // 继续循环，重新输入用户名
                }
            } catch (SocketTimeoutException e) {
                JOptionPane.showMessageDialog(gui, "登录超时，请检查服务器状态", "错误", JOptionPane.ERROR_MESSAGE);
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(gui, "登录过程出现错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false;
    }
    
    private void sendLoginMessage() {
        try {
            Message loginMsg = new Message("LOGIN", username, "");
            output.writeObject(loginMsg);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void sendMessage(String content) {
        if (!connected || socket.isClosed()) {
            JOptionPane.showMessageDialog(gui, "连接已断开，无法发送消息", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Message chatMsg = new Message("CHAT", username, content);
            output.writeObject(chatMsg);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(gui, "发送消息失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void disconnect() {
        connected = false;
        try {
            if (output != null) {
                Message logoutMsg = new Message("LOGOUT", username, "");
                output.writeObject(logoutMsg);
                output.flush();
            }
            if (receiver != null) {
                receiver.stop();
            }
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 创建连接配置对话框
            JPanel panel = new JPanel(new GridLayout(2, 2));
            panel.add(new JLabel("服务器IP地址:"));
            JTextField ipField = new JTextField("localhost");
            panel.add(ipField);
            panel.add(new JLabel("端口号:"));
            JTextField portField = new JTextField("8888");
            panel.add(portField);
            
            int result = JOptionPane.showConfirmDialog(null, panel, 
                "连接设置", JOptionPane.OK_CANCEL_OPTION);
            
            if (result == JOptionPane.OK_OPTION) {
                try {
                    String serverIP = ipField.getText().trim();
                    int port = Integer.parseInt(portField.getText().trim());
                    
                    ChatClient client = new ChatClient();
                    client.connect(serverIP, port);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "端口号必须是数字", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}