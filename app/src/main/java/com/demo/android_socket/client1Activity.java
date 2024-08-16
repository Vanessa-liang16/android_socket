package com.demo.android_socket;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class client1Activity extends AppCompatActivity {

    Button client1_btn;
    EditText client1_name,client1_port,client1_IP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client1);

        client1_btn=findViewById(R.id.client1_btn); //click connect button to chatbox page
        client1_name=findViewById(R.id.client1_name);  //enter client1 name
        client1_IP=findViewById(R.id.client1_IP);     //enter 10.0.2.2
        client1_port=findViewById(R.id.client1_port);  //enter 6100

        client1_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {           //change page
                String name=client1_name.getText().toString();
                String ip=client1_IP.getText().toString();
                int port=Integer.parseInt(client1_port.getText().toString());

                Intent intent=new Intent(client1Activity.this, client1ChatboxActivity.class);
                intent.putExtra("client_name",name);
                intent.putExtra("server_ip",ip);
                intent.putExtra("server_port",port);
                startActivity(intent);
            }
        });
    }

}
