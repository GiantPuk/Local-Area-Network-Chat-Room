package server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;

public class ServerManagerGUI extends JFrame {
    private ChatServer server;
    private UserManager userManager;
    private JTextArea logArea;
    private JList<String> userList;
    private DefaultListModel<String> listModel;
    private JButton startButton;
    private JButton stopButton;
    private JButton refreshButton;
    private JButton kickButton;
    private JLabel ipInfoLabel;
    
    public ServerManagerGUI() {
        this.userManager = new UserManager();
        this.server = new ChatServer(userManager);
        initializeGUI();
    }
    
    public ServerManagerGUI(UserManager userManager) {
        this.userManager = userManager;
        this.server = new ChatServer(userManager);
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("聊天室服务器管理");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        
        // 主面板布局
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(600);
        
        // 左侧：日志和控制面板
        JPanel leftPanel = new JPanel(new BorderLayout());
        
        // 控制按钮面板
        JPanel controlPanel = new JPanel(new FlowLayout());
        startButton = new JButton("启动服务器");
        stopButton = new JButton("停止服务器");
        refreshButton = new JButton("刷新列表");
        kickButton = new JButton("踢出用户");
        
        stopButton.setEnabled(false);
        kickButton.setEnabled(false);
        
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(refreshButton);
        controlPanel.add(kickButton);
        
        // IP信息面板
        JPanel infoPanel = new JPanel(new FlowLayout());
        ipInfoLabel = new JLabel("服务器IP: 请先启动服务器");
        ipInfoLabel.setForeground(Color.BLUE);
        infoPanel.add(ipInfoLabel);
        
        // 日志区域
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(240, 240, 240));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("服务器日志"));
        
        // 创建底部面板包含按钮和日志
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(controlPanel, BorderLayout.NORTH);
        bottomPanel.add(logScroll, BorderLayout.CENTER);
        
        leftPanel.add(infoPanel, BorderLayout.NORTH);
        leftPanel.add(bottomPanel, BorderLayout.CENTER);
        
        // 右侧：用户列表
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("在线用户管理"));
        
        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
        JScrollPane userScroll = new JScrollPane(userList);
        
        rightPanel.add(userScroll, BorderLayout.CENTER);
        
        mainSplitPane.setLeftComponent(leftPanel);
        mainSplitPane.setRightComponent(rightPanel);
        
        add(mainSplitPane);
        
        // 事件监听
        setupEventListeners();
    }
    
    private void setupEventListeners() {
        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());
        refreshButton.addActionListener(e -> refreshUserList());
        kickButton.addActionListener(e -> kickUser());
        
        userList.addListSelectionListener(e -> {
            kickButton.setEnabled(!userList.isSelectionEmpty());
        });
    }
    
    private void startServer() {
        new Thread(() -> {
            server.start();
        }).start();
        
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        logMessage("服务器已启动，端口：8888");
        
        // 显示IP信息
        displayIPInfo();
    }
    
    private void stopServer() {
        int result = JOptionPane.showConfirmDialog(this,
            "确定要停止服务器吗？所有在线用户将被断开连接。",
            "确认停止服务器",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                // 禁用停止按钮，防止重复点击
                stopButton.setEnabled(false);
                
                // 调用服务器的停止方法
                server.stopServer();
                
                // 更新UI状态
                SwingUtilities.invokeLater(() -> {
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    logMessage("服务器已停止");
                    ipInfoLabel.setText("服务器IP: 服务器已停止");
                    
                    // 清空用户列表
                    listModel.clear();
                });
            }).start();
        }
    }
    
    private void displayIPInfo() {
        new Thread(() -> {
            try {
                // 等待服务器完全启动
                Thread.sleep(500);
                
                // 获取所有网络接口
                StringBuilder ipInfo = new StringBuilder();
                ipInfo.append("<html><b>服务器IP地址:</b><br>");
                ipInfo.append("• 本机连接: localhost 或 127.0.0.1<br>");
                
                // 获取局域网IP
                boolean hasLanIP = false;
                for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                    for (InetAddress inetAddress : Collections.list(networkInterface.getInetAddresses())) {
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof java.net.Inet4Address) {
                            ipInfo.append("• 局域网连接: ").append(inetAddress.getHostAddress()).append("<br>");
                            hasLanIP = true;
                        }
                    }
                }
                
                if (!hasLanIP) {
                    ipInfo.append("• 未检测到局域网IP地址<br>");
                }
                
                ipInfo.append("<br>客户端连接设置:<br>");
                ipInfo.append("&nbsp;&nbsp;服务器IP: 使用上述IP地址<br>");
                ipInfo.append("&nbsp;&nbsp;端口号: 8888</html>");
                
                SwingUtilities.invokeLater(() -> {
                    ipInfoLabel.setText(ipInfo.toString());
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    ipInfoLabel.setText("<html>服务器IP: 获取失败<br>请检查网络连接</html>");
                });
            }
        }).start();
    }
    
    private void refreshUserList() {
        if (userManager != null) {
            List<String> users = userManager.getOnlineUsers();
            listModel.clear();
            for (String user : users) {
                listModel.addElement(user);
            }
            logMessage("用户列表已刷新，当前在线用户：" + users.size() + "人");
        }
    }
    
    private void kickUser() {
        String selectedUser = userList.getSelectedValue();
        if (selectedUser != null) {
            int result = JOptionPane.showConfirmDialog(this, 
                "确定要踢出用户 '" + selectedUser + "' 吗？", 
                "确认踢出", JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                userManager.kickUser(selectedUser); 
                refreshUserList();
            }
        }
    }
    
    public void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    // 提供给外部调用的日志方法
    public void addLog(String message) {
        logMessage(message);
    }
    
    // 提供给外部更新用户列表的方法
    public void updateUserList(List<String> users) {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            for (String user : users) {
                listModel.addElement(user);
            }
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerManagerGUI serverGUI = new ServerManagerGUI();
            serverGUI.setVisible(true);
            serverGUI.logMessage("服务器管理界面已启动");
        });
    }
}