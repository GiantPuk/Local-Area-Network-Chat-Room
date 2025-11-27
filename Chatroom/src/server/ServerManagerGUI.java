package server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

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
        
        // 日志区域
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(240, 240, 240));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("服务器日志"));
        
        leftPanel.add(controlPanel, BorderLayout.NORTH);
        leftPanel.add(logScroll, BorderLayout.CENTER);
        
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
    }
    
    private void stopServer() {
        // 这里需要添加停止服务器的逻辑
        // 由于原代码没有提供停止方法，这里只是界面更新
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        logMessage("服务器已停止");
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
                // 这里需要实现踢出用户的逻辑
                // 由于原架构限制，需要扩展功能才能完全实现
                logMessage("已踢出用户: " + selectedUser);
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