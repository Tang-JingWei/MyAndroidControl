package com.iot.mycontrol;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.iot.mycontrol.netty.NettyClient;
import com.iot.mycontrol.netty.NettyServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText edittext_IP;
    private EditText edittext_port;
    private EditText edittext_send;

    private Button btn_server;
    private Button btn_client;
    private Button btn_led1;
    private Button btn_led2;
    private Button btn_led3;
    private Button btn_beep;
    private Button btn_temp;
    private Button btn_rst;
    private Button btn_send;
    private Button btn_clear;
    private TextView textView_receive;
    private TextView textView2_temp;
    private TextView textView2_humi;

    public static Handler mMainHandler;
    private ExecutorService mThreadPool;
    int led_en1 = 0;
    int led_en2 = 0;
    int led_en3 = 0;
    int beep_en = 0;
    int receive_en = 0;
    int temp_en = 0;

    String temp_str = null;

    NettyServer nettyServer;
    NettyClient nettyClient;


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edittext_IP = findViewById(R.id.editText_IP);
        edittext_port = findViewById(R.id.editText_port);
        edittext_send = findViewById(R.id.editText_send);
        btn_server = findViewById(R.id.btn_server);
        btn_client = findViewById(R.id.btn_client);
        btn_led1 = findViewById(R.id.btn_led1);
        btn_led2 = findViewById(R.id.btn_led2);
        btn_led3 = findViewById(R.id.btn_led3);
        btn_beep = findViewById(R.id.btn_beep);
        btn_temp = findViewById(R.id.btn_temp);
        btn_rst = findViewById(R.id.btn_rst);
        btn_send = findViewById(R.id.btn_send);
        btn_clear = findViewById(R.id.btn_clear);
        textView_receive = findViewById(R.id.textView_receive);
        textView2_temp = findViewById(R.id.textView2_temp);
        textView2_humi = findViewById(R.id.textView2_humi);

        textView_receive.setMovementMethod(ScrollingMovementMethod.getInstance());
        setTabHost();

        // 设置按钮Button监听器 buttonListener
        Button.OnClickListener buttonListener = new Button.OnClickListener() {

            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_server:
                        Log.d("ipAndPort", "IP=>" + edittext_IP.getText().toString() + ":" + Integer.parseInt(edittext_port.getText().toString()));
                        start_server(Integer.parseInt(edittext_port.getText().toString()));
                        break;
                    case R.id.btn_client:
                        Log.d("ipAndPort", "IP=>" + edittext_IP.getText().toString() + ":" + Integer.parseInt(edittext_port.getText().toString()));
                        start_client(edittext_IP.getText().toString(), Integer.parseInt(edittext_port.getText().toString()));
                        break;
                    case R.id.btn_led1:
                        btn_led1();
                        break;
                    case R.id.btn_led2:
                        btn_led2();
                        break;
                    case R.id.btn_led3:
                        btn_led3();
                        break;
                    case R.id.btn_beep:
                        btn_beep();
                        break;
                    case R.id.btn_temp:
                        btn_temp();
                        break;
                    case R.id.btn_rst:
                        btn_rst();
                        break;
                    case R.id.btn_send:
                        btn_send(edittext_send.getText().toString());
                        break;
                    case R.id.btn_clear:
                        btn_clear();
                        break;
                }
            }
        };

        btn_client.setOnClickListener(buttonListener);
        btn_server.setOnClickListener(buttonListener);
        btn_led1.setOnClickListener(buttonListener);
        btn_led2.setOnClickListener(buttonListener);
        btn_led3.setOnClickListener(buttonListener);
        btn_beep.setOnClickListener(buttonListener);
        btn_temp.setOnClickListener(buttonListener);
        btn_rst.setOnClickListener(buttonListener);
        btn_send.setOnClickListener(buttonListener);
        btn_clear.setOnClickListener(buttonListener);
        mThreadPool = Executors.newCachedThreadPool();

        mMainHandler = new Handler() {
            @SuppressLint({"HandlerLeak", "SetTextI18n"})
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        String[] split = msg.getData().getString("msg").split(" ");
                        Log.d("wifi_data", String.valueOf(msg.getData().getString("msg")));
                        textView2_temp.setText(split[0] + "℃");
                        textView2_humi.setText(split[1] + "RH%");
                        break;
                    case 1:
                        temp_str = textView_receive.getText().toString();
                        textView_receive.setText(temp_str + "\r\n" + msg.getData().getString("msg"));
                        break;
                    case 2:
                        Toast.makeText(MainActivity.this, msg.getData().getString("msg"), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    // Tab的内容
    protected void setTabHost() {
        TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("连接")
                .setContent(R.id.tab1));
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("控制")
                .setContent(R.id.tab2));
        tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("命令收发")
                .setContent(R.id.tab3));
    }

    /**
     * 监听器指定的方法们
     */
    void start_server(int port) {
        try {
            nettyServer = new NettyServer(port);
            Toast.makeText(this, "服务器启动成功", Toast.LENGTH_SHORT).show();
            btn_server.setEnabled(false);
            btn_server.setText("服务已开启");
        } catch (InterruptedException e) {
            Toast.makeText(this, "服务器启动失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    void start_client(String ip, int port) {
        nettyClient = new NettyClient();
        try {
            if (nettyClient.startNetty(ip, port)) {
                Toast.makeText(this, "连接服务器成功！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "连接服务器失败！", Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException e) {
            Toast.makeText(this, "连接服务器失败！", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    void btn_send(String text) {
        // 利用线程池直接开启一个线程 & 执行该线程
        if (nettyClient == null) {
            nettyServer.serverSend("#" + text + "$");
        } else {
            nettyClient.clientSend("c#" + text + "$");
        }
    }

    void btn_led1() {
        if (led_en1 == 0) {
            btn_send("open_led1");
            btn_led1.setText("关灯");
            led_en1 = 1;
        } else if (led_en1 == 1) {
            btn_led1.setText("开灯");
            btn_send("close_led1");
            led_en1 = 0;
        }
    }

    void btn_led2() {
        if (led_en2 == 0) {
            btn_send("open_led2");
            btn_led2.setText("关灯");

            led_en2 = 1;
        } else if (led_en2 == 1) {
            btn_led2.setText("开灯");
            btn_send("close_led2");
            led_en2 = 0;
        }
    }

    void btn_led3() {
        if (led_en3 == 0) {
            btn_send("open_led3");
            btn_led3.setText("关灯");
            led_en3 = 1;
        } else if (led_en3 == 1) {
            btn_led3.setText("开灯");
            btn_send("close_led3");
            led_en3 = 0;
        }
    }

    void btn_beep() {
        if (beep_en == 0) {
            btn_send("open_beep");
            btn_beep.setText("关闭");
            beep_en = 1;
        } else if (beep_en == 1) {
            btn_beep.setText("开启");
            btn_send("close_beep");
            beep_en = 0;
        }
    }

    void btn_temp() {
        // 0-1-0-1循环
        if (temp_en == 0) {
            btn_send("open_dht11");
            btn_temp.setText("停止检测");
            temp_en = 1;
        } else if (temp_en == 1) {
            btn_temp.setText("开始检测");
            btn_send("close_dht11");
            temp_en = 0;
        }
    }

    @SuppressLint("SetTextI18n")
    void btn_rst() {
        btn_send("reset_all");
        btn_led1.setText("亮灯");
        btn_led2.setText("亮灯");
        btn_led3.setText("亮灯");
        btn_beep.setText("开启");
        btn_temp.setText("开始检测");
        textView2_temp.setText("0℃");
        textView2_humi.setText("0RH%");
        led_en1 = led_en2 = led_en3 = beep_en = temp_en = 0;
    }

    void btn_clear() {
        Toast.makeText(this, "清空", Toast.LENGTH_SHORT).show();
        textView_receive.setText("");
    }
}
