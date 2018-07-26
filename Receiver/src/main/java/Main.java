

import java.net.*;
import java.io.*;
import javax.imageio.*;

import javax.swing.*;
import java.awt.image.*;


public class Main {

    public static JLabel jlb = new JLabel();// GUI container that will hold the image object

    public static JFrame jfr = new JFrame(); // GUI container that will hold the container containing the image

    public static int count = 0;
    public static VideoMaker videoMaker;

    public static void main(String[] args) {


        try {
            //init coder
            videoMaker = new VideoMaker(10);
            System.out.println("Setparams");
            videoMaker.setParams("E:/test/a.mp4", null, null, 640, 480);
            //videoMaker.changeSetting("E:/test/b.mp4", 10);
            System.out.println("Coder Init finish");

            ServerSocket server = new ServerSocket(30000);// listening for connections on port 8000
            Socket childSocket = server.accept();// accept and connect to a scocket

            jfr.setVisible(true);
            jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jfr.add(jlb);// JLabel container being added to the JFrame container
            DataInputStream is = new DataInputStream(childSocket.getInputStream());     //获取输入流


            while (true) {
                //3.连接后获取输入流，读取客户端信息


                int length = is.readInt();
                //System.out.print("length " + length);
                byte[] data = new byte[length];
                is.readFully(data, 0, length);

                InputStream in = new ByteArrayInputStream(data);
                BufferedImage im = ImageIO.read(in); // creating an image from the byte array

                count ++;
                //coder
                if (count < 200) {
                    videoMaker.recording(im, count);
                } else if(count == 200){
                    videoMaker.end();
                }

                if (count == 300){
                    System.out.println("Change Setting");
                    videoMaker.setParams("E:/test/b.mp4", null, null, 640, 480);
                }
                if (count > 350 && count < 500){
                    videoMaker.recording(im, count - 350);
                } else if(count == 500){
                    videoMaker.end();
                }


                if (im == null) {
                    continue;
                }
                jlb.setIcon(new ImageIcon(im));// setting the image onto the JLabel container
                jfr.pack();


                System.gc();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


//            /*
//             * 接收客户端发送的数据(UDP)
//             */
//            // 1.创建服务器端DatagramSocket，指定端口
//            DatagramSocket socket = new DatagramSocket(30000);
//            // 2.创建数据报，用于接收客户端发送的数据
//            byte[] data = new byte[1024];// 创建字节数组，指定接收的数据包的大小
//            DatagramPacket packet = new DatagramPacket(data, data.length);
//            // 3.接收客户端发送的数据
//            System.out.println("****服务器端已经启动，等待客户端发送数据");
//            socket.receive(packet);// 此方法在接收到数据报之前会一直阻塞
//            // 4.读取数据
//            String info = new String(data, 0, packet.getLength());
