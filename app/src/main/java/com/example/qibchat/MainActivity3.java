package com.example.qibchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class MainActivity3 extends AppCompatActivity {
    TextView textView;
    Button button;
    EditText editText;
    SharedPreferences preferences;
    public static String name;
    public static String rand;

    public static String USER_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        if (MainActivity4.leave){
            USER_KEY = null;
            name = null;
            rand = null;
            MainActivity4.leave = false;
            save();
            Intent intent = new Intent(MainActivity3.this,MainActivity2.class);
            startActivity(intent);
            finish();
        }

        loadSave();

        textView = findViewById(R.id.textView);
        editText = findViewById(R.id.editText);

        if (USER_KEY != null){
            save();
            Intent intent = new Intent(MainActivity3.this,MainActivity4.class);
            startActivity(intent);
            finish();
        }else {
            Random random = new Random();
            rand = String.valueOf(random.nextInt(10000));
            textView.setText("Ваш ID: " + rand);
        }

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editText.getText().toString())){
                    if(USER_KEY == null){
                        name = editText.getText().toString();
                        USER_KEY = name + "" + rand;
                        save();
                        String text = MainActivity2.email = MainActivity2.email.replace(".", "");
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(text).child("email");
                        ref.push().setValue(text);
                        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference(text).child("name");
                        ref2.push().setValue(USER_KEY);
                        Intent intent = new Intent(MainActivity3.this,MainActivity4.class);
                        startActivity(intent);
                        finish();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Введите имя!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    void save(){
        preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = preferences.edit();
        ed.putString("USER_KEY",USER_KEY);
        ed.commit();

    }
    void loadSave(){
        preferences = getPreferences(MODE_PRIVATE);
        USER_KEY = preferences.getString("USER_KEY", USER_KEY);
    }

}