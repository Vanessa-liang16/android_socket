package com.demo.android_socket;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class client2ChatboxActivity extends AppCompatActivity {

    public static final int SERVERPORT=6100;
    public static final String SERVER_IP="YOUR_SERVER_IP";
    private ClientThread clientThread;
    private int clientTextColor;
    private Button client2_leave;
    private Handler handler;
    private PrintWriter out;
    private BufferedReader in;
    private TextView client2_chatbox;
    private EditText client2_message;
    private Button client2_send;
    private String clientName;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client2_chatbox); // 設置佈局文件

        //get the IP,port,client name from the intent
        Intent intent=getIntent();
        String serverIP=intent.getStringExtra("server_ip");
        int serverPort=intent.getIntExtra("server_port",6100);
        clientName=intent.getStringExtra("client_name");

        //connect to server
        clientThread=new ClientThread(serverIP,serverPort);
        thread=new Thread(clientThread);
        thread.start();
        //showMessage("connected to server successfully",clientTextColor);


        handler=new Handler();
        client2_leave=findViewById(R.id.client2_leave);
        client2_chatbox=findViewById(R.id.client2_chatbox);
        clientTextColor= ContextCompat.getColor(this,R.color.purple_700);
        client2_message=findViewById(R.id.client2_message);
        client2_send=findViewById(R.id.client2_send);


        client2_leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {           //change page
                if (clientThread != null) {
                    clientThread.sendMessage(clientName + " disconnect");
                }
                Intent intent=new Intent(client2ChatboxActivity.this, client2Activity.class);
                startActivity(intent);
            }
        });

        client2_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = client2_message.getText().toString().trim();
                if (!message.isEmpty()) {
                    String formattedMessage = clientName + ": " + message;
                    //showMessage(formattedMessage, clientTextColor);
                    if (clientThread != null) {
                        clientThread.sendMessage(formattedMessage); // 发送格式化后的消息
                    }
                    client2_message.setText(""); // 发送后清空输入框
                }
            }
        });
        IntentFilter filter2=new IntentFilter("com.demo.android_socket.SERVER_MESSAGE_CLIENT2");
        registerReceiver(serverMessageReceiver,filter2);

    }

    public void showMessage(final String message,final int color){
        handler.post(new Runnable() {
            @Override
            public void run() {
                client2_chatbox.setTextColor(color);
                String currentTime=getTime();
                client2_chatbox.append(message+"["+currentTime+"]\n");
            }
        });
    }
    class ClientThread implements Runnable{
        private Socket socket;
        private String serverIP;
        private int serverPort;
        private PrintWriter out;

        ClientThread(String serverIP, int serverPort) {
            this.serverIP = serverIP;
            this.serverPort = serverPort;
        }

        @Override
        public void run() {
            try{
                InetAddress serverAddr=InetAddress.getByName(serverIP);
                socket=new Socket(serverAddr,serverPort);
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.println(clientName);
                showMessage("Connected to server successfully", clientTextColor);

                while(!Thread.currentThread().isInterrupted()){
                    String message=input.readLine();
                    if(message==null||"Disconnect".contentEquals(message)){
                        Thread.currentThread().interrupt();
                        message="Server Disconnected.";
                        showMessage(message,Color.RED);
                        break;
                    }
                    showMessage(message,clientTextColor);
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        void sendMessage(final String message){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        if(socket != null && out != null){
                            out.println(message);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }
    private BroadcastReceiver serverMessageReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message=intent.getStringExtra("message");
            showMessage(message,clientTextColor);

        }
    };
    public final String getTime(){
        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(null!=clientThread){
            clientThread.sendMessage("Disconnect");
            clientThread=null;
        }
        unregisterReceiver(serverMessageReceiver);
    }
}