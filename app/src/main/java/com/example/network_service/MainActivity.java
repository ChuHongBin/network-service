package com.example.network_service;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button connect;

    private TextView text;

    private static final String HOST = "192.168.103.2";

    private static final int POST = 881;

    private Socket socket;

    private Boolean connectSign=true;

    private BufferedReader reader;

    private BufferedWriter writer;

    private static final int UPDATE_TEXT = 1;

    private String response = null;

    private static final int CONNECTSIGN = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connect = (Button) findViewById(R.id.connect);
        text = (TextView) findViewById(R.id.initText);
        connect.setOnClickListener(this);
    }
    @Override
    public void onClick(View v){
        //连接
        if(v.getId()==R.id.connect && true == connectSign){ //
             connectRequest();
        }
    }

    //更新UI界面
    private Handler handler = new Handler(){

        public void handleMessage(Message msg){
            switch (msg.what){
                case UPDATE_TEXT:
                    text.setText(response);
                    break;
                case CONNECTSIGN:
                    Toast.makeText(MainActivity.this, "连接超时", Toast.LENGTH_SHORT).show();
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
                    connectSign =false;
                    socket = new Socket();
                    SocketAddress socketAddress = new InetSocketAddress(HOST, POST);
                    //3秒超时
                    socket.connect(socketAddress,3000);
                    //获取数据流
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    startServerReplyListener(reader);
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    writer.write("admin123\n");
                    writer.flush();
                    timeSend(writer);
                } catch (Exception e) {
                    connectSign = true;
                    setMessage(CONNECTSIGN);
                }
            }
        }).start();
    }
    //定时发送
    public void timeSend(final BufferedWriter writer){
        new Thread(new Runnable() {
            @Override
            public void run() {

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                writer.write("hello world .. \n");
                                writer.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    },3000,3000);
                }
        }).start();
    }

    //接收数据
    public void startServerReplyListener(final BufferedReader reader){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while((response = reader.readLine()) != null){
                        setMessage(UPDATE_TEXT);
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
            writer.write("bye");
            writer.flush();
            socket.close();
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}






















