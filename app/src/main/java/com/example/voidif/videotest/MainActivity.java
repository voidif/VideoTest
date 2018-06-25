package com.example.voidif.videotest;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ProgressBar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private SurfaceView view;
    private SurfaceHolder holder;
    private Camera camera;

    private ProgressBar pgBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pgBar = findViewById(R.id.pgbar);



        initView();

        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                BackgroundTask myTask = new BackgroundTask(camera, data);
                myTask.execute(1);
            }
        });


    }

    public void initView(){
        view = findViewById(R.id.view);
        holder = view.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                camera = Camera.open();
                camera.autoFocus(null);
                try {
                    camera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                camera.setDisplayOrientation(90);
                camera.startPreview();

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }
}

class BackgroundTask extends AsyncTask<Integer, Integer, String>{
    //该方法运行在后台线程中，主要负责执行耗时的后台计算传输等工作，
    //实际的后台操作被UI Thread调用时，该方法被回调
    private Camera camera;
    private byte[] data;

    public BackgroundTask(Camera camera, byte[] data){
        super();
        this.camera = camera;
        this.data = data;
    }

    @Override
    protected String doInBackground(Integer... params) {
        Camera.Size size = camera.getParameters().getPreviewSize();
        int wide = size.width;
        int high = size.height;
        YuvImage image = new YuvImage(data, ImageFormat.NV21, wide, high, null);
        //因为要实时处理视频流，因此用内存操作流比较合适
        ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
        if(!image.compressToJpeg(new Rect(0, 0, wide, high), 100, os)){
            return null;
        }
        NetworkTool.send(os);
        return null;
    }

    //在doBackground方法中,每次调用publishProgress方法都会触发该方法
    //运行在UI线程中,可对UI控件进行操作
//    @Override
//    protected void onProgressUpdate(Integer... values) {
//        int value = values[0];
//        pgBar.setProgress(value);
//    }


}


