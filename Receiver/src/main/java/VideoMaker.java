import io.humble.video.*;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class VideoMaker {
    enum State{
        ERROR,
        CREATED,
        //encoder is initialing
        INITIALING,
        //encoder is ready to convert image to video
        READY,
        RECORDING,
        //if the state is end, you need reset the filename or the new file will overwrite the old file.
        END
    }
    //video relevant variables
    public Rational framerate;
    public Muxer muxer;
    public MuxerFormat format;
    public Codec codec;
    public Encoder encoder;
    public MediaPictureConverter converter = null;
    public MediaPacket packet;
    public  MediaPicture picture;

    private boolean isRecording = false;
    private State state = State.CREATED;

    public VideoMaker(int snapsPerSecond) throws Exception{
        //this call will cost a lot of time
        framerate = Rational.make(1, snapsPerSecond);
        //init(filename, null, null, snapsPerSecond, width, height);
    }

    //init base decoder and encoder
    //@params: formatname and codecname could be null
    public boolean setParams(String filename, String formatname, String codecname, int Width, int Height) throws InterruptedException, IOException {
        if(state == State.RECORDING || state == State.INITIALING){
            return false;
        }
        state = State.INITIALING;

//        /**
//         * Set up the AWT infrastructure to take screenshots of the desktop.
//         */
//        final Robot robot = new Robot();
//        final Toolkit toolkit = Toolkit.getDefaultToolkit();
//        final Rectangle screenbounds = new Rectangle(toolkit.getScreenSize());



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
        encoder.setWidth(Width);
        encoder.setHeight(Height);


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
        /** Now begin our main loop of taking screen snaps.
         * We're going to encode and then write out any resulting packets. */
        packet = MediaPacket.make();

        state = State.READY;
        return true;
    }

    public State getState(){
        State tmpState = state;
        return tmpState;
    }

    public void recording(BufferedImage dataImage, int count) {
        state = State.RECORDING;

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
        
        state = State.READY;
    }

    public void end(){
        state = State.END;
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
        System.out.println("Record End");
    }
}
