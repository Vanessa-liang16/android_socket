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
import android.util.Log;
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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class client1ChatboxActivity extends AppCompatActivity {

    public static final int SERVERPORT=6100;
    public static final String SERVER_IP="YOUR_SERVER_IP";
    private ClientThread clientThread;
    private int clientTextColor;
    private Button client1_leave;
    private Handler handler;
    private PrintWriter out;
    private BufferedReader in;
    private TextView client1_chatbox;
    private EditText client1_message;
    private Button client1_send;
    private String clientName;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client1_chatbox); // 設置佈局文件

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
        client1_leave=findViewById(R.id.client1_leave);
        client1_chatbox=findViewById(R.id.client1_chatbox);
        clientTextColor= ContextCompat.getColor(this,R.color.purple_700);
        client1_message=findViewById(R.id.client1_message);
        client1_send=findViewById(R.id.client1_send);


        client1_leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {           //change page
                if (clientThread != null) {
                    clientThread.sendMessage(clientName + " disconnect");
                }
                Intent intent=new Intent(client1ChatboxActivity.this, client1Activity.class);
                startActivity(intent);
            }
        });

        client1_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = client1_message.getText().toString().trim();
                if (!message.isEmpty()) {
                    String formattedMessage = clientName + ": " + message;
                    //showMessage(formattedMessage, clientTextColor);
                    if (clientThread != null) {
                        clientThread.sendMessage(formattedMessage); // 发送格式化后的消息
                    }
                    client1_message.setText(""); // 发送后清空输入框
                }
            }
        });
        IntentFilter filter1=new IntentFilter("com.demo.android_socket.SERVER_MESSAGE_CLIENT1");
        registerReceiver(serverMessageReceiver,filter1);

    }

    public void showMessage(final String message,final int color){
        handler.post(new Runnable() {
            @Override
            public void run() {
                client1_chatbox.setTextColor(color);
                String currentTime=getTime();
                client1_chatbox.append(message+"["+currentTime+"]\n");
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