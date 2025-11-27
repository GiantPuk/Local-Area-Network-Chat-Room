package control;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class MainPanel extends JFrame {
    private JButton startClientButton;
    private JButton startServerButton;
    private Process serverProcess;
    
    public MainPanel() {
        initializeGUI();
        setupEventListeners();
    }
    
    private void initializeGUI() {
        setTitle("局域网聊天室控制系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // 系统标题
        JLabel titleLabel = new JLabel("局域网聊天室管理系统", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // 创建按钮面板（居中放置）
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 100, 0, 100));
        
        // 创建两个按钮
        startClientButton = new JButton("启动客户端");
        startServerButton = new JButton("启动服务器");
        
        // 设置按钮样式
        startClientButton.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        startServerButton.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        startClientButton.setPreferredSize(new Dimension(150, 60));
        startServerButton.setPreferredSize(new Dimension(150, 60));
        
        // 添加按钮到面板
        buttonPanel.add(startClientButton);
        buttonPanel.add(startServerButton);
        
        // 将按钮面板添加到主面板中央
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // 添加说明文本
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setBackground(getBackground());
        infoArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        infoArea.setText("使用说明：\n" +
                        "• 启动服务器：启动聊天室服务器和管理界面\n" +
                        "• 启动客户端：启动一个新的聊天客户端\n" +
                        "• 注意：每个窗口都在独立进程中运行，互不影响");
        infoArea.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        mainPanel.add(infoArea, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void setupEventListeners() {
        // 启动客户端按钮事件
        startClientButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startClientControl();
            }
        });
        
        // 启动服务器按钮事件
        startServerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startServerControl();
            }
        });
        
        // 主窗口关闭时停止服务器进程
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                stopServerProcess();
            }
        });
    }
    
    /**
     * 启动客户端控制函数 - 在新的JVM进程中启动
     */
    private void startClientControl() {
        System.out.println("启动客户端按钮被点击");
        
        try {
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
            String classpath = System.getProperty("java.class.path");
            String className = "client.ChatClient";
            
            ProcessBuilder builder = new ProcessBuilder(
                javaBin, "-cp", classpath, className
            );
            
            builder.start();
            System.out.println("客户端进程已启动");
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, 
                "启动客户端失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    /**
     * 启动服务器控制函数 - 在新的JVM进程中启动
     */
    private void startServerControl() {
        System.out.println("启动服务器按钮被点击");
        
        try {
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
            String classpath = System.getProperty("java.class.path");
            String className = "server.ServerManagerGUI";
            
            ProcessBuilder builder = new ProcessBuilder(
                javaBin, "-cp", classpath, className
            );
            
            serverProcess = builder.start();
            System.out.println("服务器进程已启动");
            
            // 监听服务器进程退出
            new Thread(() -> {
                try {
                    int exitCode = serverProcess.waitFor();
                    System.out.println("服务器进程已退出，代码: " + exitCode);
                    serverProcess = null;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, 
                "启动服务器失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    /**
     * 停止服务器进程
     */
    private void stopServerProcess() {
        if (serverProcess != null && serverProcess.isAlive()) {
            serverProcess.destroy();
            System.out.println("已停止服务器进程");
        }
    }
    
    // 主方法用于测试
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainPanel().setVisible(true);
            }
        });
    }
}