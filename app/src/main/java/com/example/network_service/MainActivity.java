package com.example.network_service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button connect;

    private TextView text;

    private static final String HOST ="192.168.103.2"; //"113.17.169.50";

    private static final int POST = 1234;//30792;

    private Socket socket;

    private BufferedInputStream reader;

    private BufferedOutputStream writer;

    private static final int UPDATE_TEXT = 1;

    private String response = null;

    private Bitmap bitmap;

    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connect = (Button) findViewById(R.id.connect);
        text = (TextView) findViewById(R.id.initText);
        imageView = (ImageView) findViewById(R.id.image_view);
        connect.setOnClickListener(this);
    }
    @Override
    public void onClick(View v){
        //连接
        if(v.getId()==R.id.connect){ //
             connectRequest();
        }
    }

    //更新UI界面
    private Handler handler = new Handler(){

        public void handleMessage(Message msg){
            switch (msg.what){
                case UPDATE_TEXT:
                    text.setText("成功");
                    imageView.setImageBitmap(bitmap);
                    break;
                case 2 :
                    Toast.makeText(MainActivity.this, "等待接收信息..", Toast.LENGTH_SHORT).show();
                case 3 :
                    Toast.makeText(MainActivity.this, "连接超时..", Toast.LENGTH_SHORT).show();
            }
        }
    };
    //子线程的信息传递
    private void setMessage(final int sign){
        Message message = new Message();
        message.what = sign;
        handler.sendMessage(message);
    }

    //连接请求
    private void connectRequest(){
        //创建Socket
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    socket = new Socket();
                    SocketAddress socketAddress = new InetSocketAddress(HOST, POST);
                    //3秒超时
                    socket.connect(socketAddress,3000);
                    //获取数据流
                    reader = new BufferedInputStream(socket.getInputStream());
                    writer = new BufferedOutputStream(socket.getOutputStream());
                    writer.write("android".getBytes("UTF-8"));
                    writer.flush();
                    startServerReplyListener(reader);
                }catch (SocketTimeoutException e){
                    setMessage(3);
                } catch (IOException ex) {

                }
            }
        }).start();
    }

    //接收数据
    public void startServerReplyListener(final BufferedInputStream reader){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] img = new byte[1024 * 11];
                    byte[] buff = new byte[1024 * 2];
                    byte[] begin = new byte[3];
                    byte[] end = new byte[3];
                    int num = 0;
                    int i = 0;
                    while(true){
                        boolean istrue = false;
                        while((num = reader.read(buff)) != -1){
                            //判断尾部
                            if(num < 1024*2){
                                System.arraycopy(buff, num-3, end, 0, 3);
                                String endString = new String(end);
                                if(endString.equals("end")){
                                    num = num - 3;
                                    istrue = true;
                                    setMessage(2);
                                }else {
                                    i = 0;
                                }
                            }
                            System.arraycopy(buff,0, img, i*1024*2, num);
                            if(istrue == true){
                                bitmap = BitmapFactory.decodeByteArray(img, 0, i*1024*2+num);
                                i=0;
                                setMessage(UPDATE_TEXT);
                                istrue = false;
                            }
                            i++;
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    //销毁socket
    @Override
    public void onDestroy(){
        super.onDestroy();
        try {
            writer.flush();
            socket.close();
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}






















