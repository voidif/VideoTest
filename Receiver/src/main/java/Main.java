import io.humble.video.*;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

import java.awt.*;
import java.net.*;
import java.io.*;
import javax.imageio.*;
import javax.media.Buffer;
import javax.media.MediaLocator;
import javax.swing.*;
import java.awt.image.*;
import java.util.Vector;

public class Main {

    public static JLabel jlb=new JLabel();// GUI container that will hold the image object

    public static JFrame jfr=new JFrame(); // GUI container that will hold the container containing the image

    public static int count = 0;
    public static boolean isRecord = true;

    public Main() {
    }

    public static void main(String[] args) {


        try{
            //init coder
            init("E:/test/a.mp4", null, null, 100, 20);

            System.out.println("Coder Init finish");


            ServerSocket server = new ServerSocket(30000);// listening for connections on port 8000
            Socket childSocket = server.accept();// accept and connect to a scocket

            jfr.setVisible(true);
            jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jfr.add(jlb);// JLabel container being added to the JFrame container
            DataInputStream is = new DataInputStream(childSocket.getInputStream());     //获取输入流



            while(true){
                //3.连接后获取输入流，读取客户端信息


                int length = is.readInt();
                System.out.print("length " + length);
                byte[] data = new byte[length];
                is.readFully(data, 0, length);

                InputStream in = new ByteArrayInputStream(data);
                BufferedImage im = ImageIO.read(in); // creating an image from the byte array

                //coder
                if(isRecord){
                    recording(100, im, count);
                    count ++;
                    if(count > 200){
                        isRecord = false;
                    }
                } else {
                    end();
                }


                if(im == null){
                    continue;
                }
                jlb.setIcon(new ImageIcon(im));// setting the image onto the JLabel container
                jfr.pack();


                System.gc();
            }
        }
        catch(Exception e){e.printStackTrace();}
    }

    //video relevant variables
    public static Rational framerate;
    public static Muxer muxer;
    public static MuxerFormat format;
    public static Codec codec;
    public static Encoder encoder;
    public static MediaPictureConverter converter = null;
    public static MediaPacket packet;
    public static MediaPicture picture;

    public static void init(String filename, String formatname, String codecname, int duration, int snapsPerSecond) throws AWTException, InterruptedException, IOException{
//        /**
//         * Set up the AWT infrastructure to take screenshots of the desktop.
//         */
//        final Robot robot = new Robot();
//        final Toolkit toolkit = Toolkit.getDefaultToolkit();
//        final Rectangle screenbounds = new Rectangle(toolkit.getScreenSize());

        framerate = Rational.make(1, snapsPerSecond);

        /** First we create a muxer using the passed in filename and formatname if given. */
        muxer = Muxer.make(filename, null, formatname);

        /** Now, we need to decide what type of codec to use to encode video. Muxers
         * have limited sets of codecs they can use. We're going to pick the first one that
         * works, or if the user supplied a codec name, we're going to force-fit that
         * in instead.
         */
        format = muxer.getFormat();
        if (codecname != null)
        {
            codec = Codec.findEncodingCodecByName(codecname);
        }
        else
        {
            codec = Codec.findEncodingCodec(format.getDefaultVideoCodecId());
        }

        /**
         * Now that we know what codec, we need to create an encoder
         */
        encoder = Encoder.make(codec);

        /**
         * Video encoders need to know at a minimum:
         *   width
         *   height
         *   pixel format
         * Some also need to know frame-rate (older codecs that had a fixed rate at which video files could
         * be written needed this). There are many other options you can set on an encoder, but we're
         * going to keep it simpler here.
         */
//        encoder.setWidth(dataImage.getWidth());
//        encoder.setHeight(dataImage.getHeight());

        encoder.setWidth(640);
        encoder.setHeight(480);


        // We are going to use 420P as the format because that's what most video formats these days use
        final PixelFormat.Type pixelformat = PixelFormat.Type.PIX_FMT_YUV420P;
        encoder.setPixelFormat(pixelformat);
        encoder.setTimeBase(framerate);

        /** An annoynace of some formats is that they need global (rather than per-stream) headers,
         * and in that case you have to tell the encoder. And since Encoders are decoupled from
         * Muxers, there is no easy way to know this beyond
         */
        if (format.getFlag(MuxerFormat.Flag.GLOBAL_HEADER))
        {
            encoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);
        }

        /** Open the encoder. */
        encoder.open(null, null);


        /** Add this stream to the muxer. */
        muxer.addNewStream(encoder);

        /** And open the muxer for business. */
        muxer.open(null, null);

        /** Next, we need to make sure we have the right MediaPicture format objects
         * to encode data with. Java (and most on-screen graphics programs) use some
         * variant of Red-Green-Blue image encoding (a.k.a. RGB or BGR). Most video
         * codecs use some variant of YCrCb formatting. So we're going to have to
         * convert. To do that, we'll introduce a MediaPictureConverter object later. object.
         */

        picture = MediaPicture.make(encoder.getWidth(), encoder.getHeight(), pixelformat);
        picture.setTimeBase(framerate);

    }

    public static void recording(int duration, BufferedImage dataImage, int count) throws AWTException, InterruptedException, IOException{
        /** Now begin our main loop of taking screen snaps.
         * We're going to encode and then write out any resulting packets. */
        packet = MediaPacket.make();

        /** Make the screen capture && convert image to TYPE_3BYTE_BGR */
        final BufferedImage screen = RecordAndEncodeVideo.convertToType(dataImage, BufferedImage.TYPE_3BYTE_BGR);

        /** This is LIKELY not in YUV420P format, so we're going to convert it using some handy utilities. */
        if (converter == null)
        {
            converter = MediaPictureConverterFactory.createConverter(screen, picture);
        }
        converter.toPicture(picture, screen, count);

        do
        {
            encoder.encode(packet, picture);
            if (packet.isComplete())
            {
                muxer.write(packet, false);
            }
        } while (packet.isComplete());
//        for (int i = 0; i < duration / framerate.getDouble(); i++)
//        {
//
//
//            /** now we'll sleep until it's time to take the next snapshot. */
//            Thread.sleep((long) (1000 * framerate.getDouble()));
//        }
    }

    public static void end(){
        /** Encoders, like decoders, sometimes cache pictures so it can do the right key-frame optimizations.
         * So, they need to be flushed as well. As with the decoders, the convention is to pass in a null
         * input until the output is not complete.
         */
        do
        {
            encoder.encode(packet, null);
            if (packet.isComplete())
            {
                muxer.write(packet, false);
            }
        } while (packet.isComplete());

        /** Finally, let's clean up after ourselves. */
        if(muxer.getState() ==  Muxer.State.STATE_OPENED){
            muxer.close();
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
