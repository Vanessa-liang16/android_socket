package com.demo.android_socket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.demo.android_socket.R;

public class client2Activity extends AppCompatActivity {

    Button client2_btn;
    EditText client2_name,client2_port,client2_IP;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client2);

        client2_btn=findViewById(R.id.client2_btn); //click connect button to chatbox page
        client2_name=findViewById(R.id.client2_name);  //enter client1 name
        client2_IP=findViewById(R.id.client2_IP);     //enter 10.0.2.2
        client2_port=findViewById(R.id.client2_port);  //enter 6100

        client2_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {           //change page
                String name=client2_name.getText().toString();
                String ip=client2_IP.getText().toString();
                int port=Integer.parseInt(client2_port.getText().toString());

                Intent intent=new Intent(client2Activity.this, client2ChatboxActivity.class);
                intent.putExtra("client_name",name);
                intent.putExtra("server_ip",ip);
                intent.putExtra("server_port",port);
                startActivity(intent);
            }
        });
    }

}
