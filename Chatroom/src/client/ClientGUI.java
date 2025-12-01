package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import common.Message;

public class ClientGUI extends JFrame {
    private ChatClient client;
    private JTextArea chatArea;
    private JTextArea inputArea;
    private JList<String> userList;
    private DefaultListModel<String> listModel;
    private JButton sendButton;
    
    public ClientGUI(ChatClient client) {
        this.client = client;
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("局域网聊天室");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // 主面板布局
        JSplitPane mainSplitPane = new JSplitPane();
        mainSplitPane.setDividerLocation(600);
        
        // 聊天区域
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(240, 240, 240));
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatPanel.add(chatScroll, BorderLayout.CENTER);
        
        // 输入区域
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputArea = new JTextArea(3, 20);
        inputArea.setLineWrap(true);
        JScrollPane inputScroll = new JScrollPane(inputArea);
        sendButton = new JButton("发送");
        
        inputPanel.add(inputScroll, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        
        // 用户列表
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBorder(BorderFactory.createTitledBorder("在线用户"));
        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
        userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        
        mainSplitPane.setLeftComponent(chatPanel);
        mainSplitPane.setRightComponent(userPanel);
        
        add(mainSplitPane);
        
        // 事件监听
        setupEventListeners();
    }
    
    private void setupEventListeners() {
        sendButton.addActionListener(e -> sendMessage());
        
        inputArea.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    e.consume();
                    sendMessage();
                }
            }
        });
        
        // 窗口关闭事件
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                client.disconnect();
            }
        });
    }
    
    public void disconnect(){
        client.disconnect();
    }
    private void sendMessage() {
        String message = inputArea.getText().trim();
        if (!message.isEmpty()) {
            client.sendMessage(message);
            inputArea.setText("");
        }
    }
    
    public void appendMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message.toString() + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    public void updateUserList(String[] users) {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            for (String user : users) {
                if (!user.isEmpty()) {
                    listModel.addElement(user);
                }
            }
        });
    }
    
    public void showLoginDialog() {
        String username = JOptionPane.showInputDialog(this, 
            "请输入用户名:", "登录", JOptionPane.PLAIN_MESSAGE);
        if (username != null && !username.trim().isEmpty()) {
            client.setUsername(username.trim());
            setTitle("局域网聊天室 - " + username);
        } else {
            System.exit(0);
        }
    }
}