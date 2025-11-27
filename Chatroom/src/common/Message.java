package common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String type;        // 消息类型：LOGIN, CHAT, LOGOUT
    private String sender;      // 发送者
    private String content;     // 消息内容
    private String timestamp;   // 时间戳
    
    public Message(String type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    // Getter和Setter方法
    public String getType() { return type; }
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }
    
    public String toString() {
        return String.format("[%s] %s: %s", timestamp, sender, content);
    }
}