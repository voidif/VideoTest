package com.example.voidif.videotest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;

public class NetworkTool {

    private static Socket socket;
    public static boolean isConnected = false;
    private static OutputStream os;

    public static boolean sendSwitch = false;

    private static final String ipName = "192.168.0.105";

    public static boolean connect(){
        try {
            socket = new Socket(ipName, 30000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(socket != null){
            try {
                isConnected = true;
                //2.获取输出流，向服务器端发送信息
                os = socket.getOutputStream();//字节输出流
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isConnected;
    }

    //发送视频流到PC端,这里传递过来的参数os中保存的是视频输出流数据
    public static void send(ByteArrayOutputStream data) {
        if(socket == null){
            return;
        }
        byte[] dataByte = data.toByteArray();
        if(dataByte.length <= 0){
            return;
        }
        //1.创建客户端Socket，指定服务器地址和端口

        byte[] buffer = new byte[1024];
        try {
            //实例化内存输入流，将视频流数据写入到内存中
            ByteArrayInputStream inputFromOs = new ByteArrayInputStream(data.toByteArray());
            //不断从内存中读取数据到buffer中，不断再从buffer中将视频数据发送到outSocket流中
            //获取长度
            int length = dataByte.length;
            System.out.println(length);
            os.write(i2b(length));

            int amount;
            while((amount = inputFromOs.read(buffer)) != -1)
                os.write(buffer, 0, amount);

            //close
            os.flush();
//            inputFromOs.close();
//            data.close();
//            socket.shutdownOutput();//关闭输出流
//            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 网上抄来的，将 int 转成字节
    public static byte[] i2b(int i) {
        return new byte[]{
                (byte) ((i >> 24) & 0xFF),
                (byte) ((i >> 16) & 0xFF),
                (byte) ((i >> 8) & 0xFF),
                (byte) (i & 0xFF)
        };
    }
}

//        byte[] data = os.toByteArray();
//
//        /*
//         * 向服务器端发送数据(UDP)
//         */
//
//        InetAddress address = null;
//        try {
//            // 1.定义服务器的地址、端口号、数据
//            address = InetAddress.getByName(ipName);
//            int port = 30000;
//            // 2.创建数据报，包含发送的数据信息
//            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
//            // 3.创建DatagramSocket对象
//            DatagramSocket socket = new DatagramSocket();
//            // 4.向服务器端发送数据报
//            socket.send(packet);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
