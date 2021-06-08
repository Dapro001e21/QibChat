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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AuthenticationScreen extends AppCompatActivity {
    private EditText text_mail, text_password;
    private Button button, button2;
    private FirebaseAuth mAuth;
    public static FirebaseUser cUser;
    SharedPreferences preferences;
    public static String email;
    public static boolean x = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        loadSave();

        text_mail = findViewById(R.id.text_email);
        text_password = findViewById(R.id.text_password);

        mAuth = FirebaseAuth.getInstance();

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(text_mail.getText().toString()) && !TextUtils.isEmpty(text_password.getText().toString())) {
                    mAuth.createUserWithEmailAndPassword(text_mail.getText().toString(), text_password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "User SignUp Successful ", Toast.LENGTH_SHORT).show();
                                email = text_mail.getText().toString();
                                save();
                                Intent intent = new Intent(AuthenticationScreen.this, AuthenticationNameScreen.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "User SignUp failed ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter email and password ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(text_mail.getText().toString()) && !TextUtils.isEmpty(text_password.getText().toString())) {
                    mAuth.signInWithEmailAndPassword(text_mail.getText().toString(), text_password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "User SignIn Successful ", Toast.LENGTH_SHORT).show();
                                email = text_mail.getText().toString();
                                save();

                                String text = email = email.replace(".", "");

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("UsersGmail").child(text).child("email");
                                DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("UsersGmail").child(text).child("name");

                                ValueEventListener vListener2 = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String str = ds.getValue(String.class);
                                            assert str != null;
                                            if (text.equals(str)){
                                                ValueEventListener vListener2 = new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                            String str = ds.getValue(String.class);
                                                            assert str != null;
                                                            AuthenticationNameScreen.USER_KEY = str;
                                                            Intent intent = new Intent(AuthenticationScreen.this, AuthenticationNameScreen.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                };
                                                ref2.addValueEventListener(vListener2);
                                            }

                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                };
                                ref.addValueEventListener(vListener2);
                            } else {
                                Toast.makeText(getApplicationContext(), "User SignIn failed ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        cUser = mAuth.getCurrentUser();
        if (cUser != null) {
            Intent intent = new Intent(AuthenticationScreen.this, AuthenticationNameScreen.class);
            startActivity(intent);
            finish();
        } else {
            if (x){
                Intent intent = new Intent(AuthenticationScreen.this, StartActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    void save() {
        preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = preferences.edit();
        ed.putString("Email", email);
        ed.commit();

    }

    void loadSave() {
        preferences = getPreferences(MODE_PRIVATE);
        email = preferences.getString("Email", email);
    }
}