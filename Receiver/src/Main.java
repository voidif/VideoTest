import java.net.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.image.*;

public class Main {

    public static JLabel jlb=new JLabel();// GUI container that will hold the image object

    public static JFrame jfr=new JFrame(); // GUI container that will hold the container containing the image


    public Main() {
    }

    public static void main(String[] args) {


        try{
            /*
             * 接收客户端发送的数据
             */
            // 1.创建服务器端DatagramSocket，指定端口
            DatagramSocket socket = new DatagramSocket(30000);
            // 2.创建数据报，用于接收客户端发送的数据
            byte[] data = new byte[1024];// 创建字节数组，指定接收的数据包的大小
            DatagramPacket packet = new DatagramPacket(data, data.length);
            // 3.接收客户端发送的数据
            System.out.println("****服务器端已经启动，等待客户端发送数据");
            socket.receive(packet);// 此方法在接收到数据报之前会一直阻塞
            // 4.读取数据
            String info = new String(data, 0, packet.getLength());



//            DataInputStream dis;
//
//            ServerSocket server=new ServerSocket(30000);// listening for connections on port 8000
//
//            Socket socket=server.accept();// accept and connect to a scocket


            while(true){
//                dis=new DataInputStream(socket.getInputStream()); 	// create an input stream
//
//                int len=dis.readInt();// read the length of the byte array sent by the client transmitting video
//
//
//                byte [] buffer=new byte[len]; // initialize a byte array of the length received from the client
//
//                dis.readFully(buffer,0,len);  // read the byte array data sent from the client and write it into the buffer array
//

                InputStream in = new ByteArrayInputStream(data, 0, packet.getLength());

                BufferedImage im=ImageIO.read(in); // creating an image from the byte array

                jlb.setIcon(new ImageIcon(im));// setting the image onto the JLabel container

                jfr.add(jlb);// JLabel container being added to the JFrame container

                jfr.pack();

                jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                jfr.setVisible(true);

                System.gc();
            }
        }
        catch(Exception e){e.printStackTrace();}
    }}