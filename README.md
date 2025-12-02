# 局域网聊天室

## 项目地址
https://github.com/GiantPuk/Local-Area-Network-Chat-Room
## 项目结构

```bash
ChatSystem/
├── src/
│   ├── common/
│   │   └── Message.java                # 消息格式
│   ├── server/
│   │   ├── ChatServer.java             # 服务器逻辑
│   │   ├── ServerManagerGUI.java       # 服务器端GUI
│   │   ├── ClientHandler.java          # 多线程接受用户状态
│   │   └── UserManager.java            # 用户管理
│   ├── client/
│   │   ├── ChatClient.java             # 客户端逻辑
│   │   ├── ClientGUI.java              # 客户端GUI
│   │   └── MessageReceiver.java        # 消息管理
│   └── control/
│       └── MainPanel.java              # 主管理界面，进程管理
└── README.md
```


## 运行方式
### 直接使用命令行运行
在src目录下运行 
```bash
control.MainPanel
```

### 在IDE中运行

打开项目并运行，有可能存在配置问题，重新编译一下再运行即可
