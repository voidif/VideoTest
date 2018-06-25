package com.example.voidif.videotest;

import java.io.ByteArrayOutputStream;
import java.net.*;

public class NetworkTool {

    private static final String ipName = "192.168.0.105";

    //发送视频流到PC端,这里传递过来的参数os中保存的是视频输出流数据
    public static void send(ByteArrayOutputStream os) {
        byte[] data = os.toByteArray();

        /*
         * 向服务器端发送数据
         */

        InetAddress address = null;
        try {
            // 1.定义服务器的地址、端口号、数据
            address = InetAddress.getByName(ipName);
            int port = 30000;
            // 2.创建数据报，包含发送的数据信息
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            // 3.创建DatagramSocket对象
            DatagramSocket socket = new DatagramSocket();
            // 4.向服务器端发送数据报
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
