package com.example.voidif.videotest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetworkTool {

    private static final String ipName = "";

    //发送视频流到PC端,这里传递过来的参数os中保存的是视频输出流数据
    public static void send(ByteArrayOutputStream os) {

        //定义用来保存从输入流中读取的视频流数据的byte数组
        byte[] buffer = new byte[1024];
        try {
            Socket client = new Socket(ipName,30000);
            OutputStream outSocket = client.getOutputStream();
            //实例化内存输入流，将视频流数据写入到内存中
            ByteArrayInputStream inputFromOs = new ByteArrayInputStream(os.toByteArray());
            //不断从内存中读取数据到buffer中，不断再从buffer中将视频数据发送到outSocket流中
            int amount;
            while((amount = inputFromOs.read(buffer)) != -1)
                outSocket.write(buffer, 0, amount);
            //这里需要刷新用到缓冲区的输出流
            os.flush();
            inputFromOs.close();
            os.close();
            outSocket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.println("无法找到要连接的服务器");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO错误");
        }
    }
}
