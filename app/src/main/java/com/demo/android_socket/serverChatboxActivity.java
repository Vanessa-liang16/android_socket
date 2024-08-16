package com.demo.android_socket;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
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
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class serverChatboxActivity extends AppCompatActivity {

    Button server_leave,server_send;
    private ServerSocket serverSocket;
    private Socket tempClientSocket;
    Thread serverThread=null;
    public static final int SERVER_PORT=6100;
    private Handler handler;
    private int tealColor;
    private List<PrintWriter> clientWriters=new ArrayList<>();
    private TextView server_chatbox;
    private EditText server_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_chatbox); // 設置佈局文件

        tealColor= ContextCompat.getColor(this,R.color.teal_700);
        handler=new Handler();
        startServer();
        showMessage("Server started.",Color.BLACK);

        server_leave=findViewById(R.id.server_leave);
        server_chatbox=findViewById(R.id.server_chatbox);
        server_send=findViewById(R.id.server_send);
        server_message=findViewById(R.id.server_message);


        server_leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {           //change page
                Intent intent=new Intent(serverChatboxActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        server_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = server_message.getText().toString().trim();
                if (!msg.isEmpty()) {
                    String formattedMessage = "Server: " + msg;
                    showMessage(formattedMessage, Color.BLUE);
                    sendMessageToAllClients(formattedMessage); // 发送格式化后的消息
                    sendBroadcastMessage(formattedMessage, "com.demo.android_socket.SERVER_MESSAGE_CLIENT1");
                    sendBroadcastMessage(formattedMessage, "com.demo.android_socket.SERVER_MESSAGE_CLIENT2");
                    server_message.setText(""); // 发送后清空输入框
                }
            }
        });
    }
    private void startServer(){
        serverThread=new Thread(new ServerThread());
        serverThread.start();
    }
    public void showMessage(final String message,final int color){
        handler.post(new Runnable() {
            @Override
            public void run() {
                server_chatbox.setTextColor(color);
                String currentTime=getTime();
                server_chatbox.append(message+"["+currentTime+"]\n");
            }
        });
    }
    private void sendMessageToAllClients(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<PrintWriter> writersToRemove = new ArrayList<>();
                for (PrintWriter writer : clientWriters) {
                    if (writer != null) {
                        try {
                            writer.println(message);
                            writer.flush();  // Ensure the message is sent out
                        } catch (Exception e) {
                            e.printStackTrace();
                            writersToRemove.add(writer);
                        }
                    } else {
                        writersToRemove.add(writer);
                    }
                }
                clientWriters.removeAll(writersToRemove);
            }
        }).start();
    }

    private void sendBroadcastMessage(String message,String clientAction){
        Intent intent = new Intent(clientAction);
        intent.putExtra("message", message);
        sendBroadcast(intent);
    }

    class ServerThread implements Runnable{
        public void run(){
            Socket socket;
            try{
                serverSocket=new ServerSocket(SERVER_PORT);
            }catch (IOException e){
                e.printStackTrace();
                showMessage("Error Starting Server:"+e.getMessage(),Color.RED);
            }
            if(null!=serverSocket){
                while(!Thread.currentThread().isInterrupted()){
                    try{
                        System.out.println("Server is waiting for client connections...");
                        socket=serverSocket.accept();
                        System.out.println("Accepted connection from " + socket.getInetAddress().getHostAddress());
                        CommunicationThread commThread=new CommunicationThread(socket);
                        new Thread(commThread).start();
                    }catch (IOException e){
                        e.printStackTrace();
                        showMessage("Error Communicating to Client: "+e.getMessage(),Color.RED);
                    }
                }
            }
        }
    }
    class CommunicationThread implements Runnable{
        private Socket clientSocket;
        private BufferedReader input;
        private PrintWriter output;
        private String clientName;

        public CommunicationThread(Socket clientSocket){
            this.clientSocket=clientSocket;
            //tempClientSocket=clientSocket;
            try{
                this.input=new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                this.output=new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.clientSocket.getOutputStream())),true);
                clientWriters.add(output);
                System.out.println("Waiting for client name...");
                clientName = input.readLine();
                showMessage("Connected to " + clientName + ".", tealColor);

            }catch (IOException e){
                e.printStackTrace();
                showMessage("Error Connecting to Client!!",Color.RED);
            }

        }
        public void run(){
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    String read = input.readLine();
                    if (read == null || "Disconnect".equals(read)) {
                        Thread.currentThread().interrupt();
                        showMessage(clientName + " disconnected", tealColor);
                        break;
                    }
                    showMessage(read, tealColor);

                    // Broadcast the client's message to all other clients
                    sendMessageToAllClients(read);
                    sendBroadcastMessage(read, "com.demo.android_socket.SERVER_MESSAGE_CLIENT1");
                    sendBroadcastMessage(read, "com.demo.android_socket.SERVER_MESSAGE_CLIENT2");
                }
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("Error: " + e.getMessage(), Color.RED);
            } finally {
                // 确保资源被释放
                clientWriters.remove(output);
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    String getTime(){
        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
        return  sdf.format(new Date());
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(serverThread != null){
            serverThread.interrupt();
            serverThread = null;
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
