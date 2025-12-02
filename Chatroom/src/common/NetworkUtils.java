package common;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworkUtils {
    
    /**
     * 获取本机所有IP地址
     */
    public static List<String> getAllIPAddresses() {
        List<String> ipList = new ArrayList<>();
        ipList.add("localhost");
        ipList.add("127.0.0.1");
        
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                for (InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
                    if (address.getHostAddress().contains(":")) {
                        continue; // 跳过IPv6
                    }
                    ipList.add(address.getHostAddress());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return ipList;
    }
    
    /**
     * 测试服务器是否可达
     */
    public static boolean isServerReachable(String ip, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), timeout);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 获取本机主机名
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "未知主机";
        }
    }
    
    /**
     * 获取友好的IP地址列表
     */
    public static String getFriendlyIPList() {
        StringBuilder sb = new StringBuilder();
        sb.append("本机IP地址列表:\n");
        
        List<String> ips = getAllIPAddresses();
        for (int i = 0; i < ips.size(); i++) {
            sb.append(i + 1).append(". ").append(ips.get(i)).append("\n");
        }
        
        return sb.toString();
    }
}