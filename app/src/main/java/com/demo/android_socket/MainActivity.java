package com.demo.android_socket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    Button socket_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        socket_btn=findViewById(R.id.socket_btn);
        socket_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {           //change page
                Intent intent=new Intent(MainActivity.this, serverChatboxActivity.class);
                startActivity(intent);
            }
        });

    }



}