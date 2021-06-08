package com.example.qibchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreatChat extends AppCompatActivity {
    private EditText edName;
    private DatabaseReference mDataBase;
    private DatabaseReference mDataBase2;
    private DatabaseReference mDataBase3;
    private DatabaseReference mDataBase4;

    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creat_chat);

        edName = findViewById(R.id.edName);

        button = findViewById(R.id.CreateButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edName.getText().toString();
                User newUser = new User(name);
                String name2 = ChatsScreen.USER_KEY;
                User newUser2 = new User(name2);
                if(!TextUtils.isEmpty(name))
                {
                    UserChat userChat = new UserChat("QibChat", "Chat Создан");
                    mDataBase = FirebaseDatabase.getInstance().getReference("Users").child(ChatsScreen.USER_KEY).child("key");
                    mDataBase.push().setValue(newUser);
                    mDataBase2 = FirebaseDatabase.getInstance().getReference("Users").child(ChatsScreen.USER_KEY).child("chat").child(name);
                    mDataBase2.push().setValue(userChat);

                    mDataBase3 = FirebaseDatabase.getInstance().getReference("Users").child(name).child("key");
                    mDataBase3.push().setValue(newUser2);
                    mDataBase4 = FirebaseDatabase.getInstance().getReference("Users").child(name).child("chat").child(ChatsScreen.USER_KEY);
                    mDataBase4.push().setValue(userChat);

                    Toast.makeText(getApplicationContext(), "Чат создан", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(CreatChat.this, ChatsScreen.class);
                    startActivity(i);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Пустое поле", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}