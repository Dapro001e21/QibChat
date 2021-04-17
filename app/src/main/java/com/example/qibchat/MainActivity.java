package com.example.qibchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cd = new ConnectionDetector(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (cd.isConnected()){
                    Intent intent = new Intent(MainActivity.this,MainActivity2.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(MainActivity.this,"Нет подключения к интернету",Toast.LENGTH_SHORT).show();
                }
            }
        }, 5 * 1000);

    }
}