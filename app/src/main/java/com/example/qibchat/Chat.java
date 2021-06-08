package com.example.qibchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class Chat extends AppCompatActivity {
    private ListView listView2;
    private List<UserChat> listTemp;
    private DatabaseReference mDataBase2;
    private DatabaseReference mDataBase3;

    FloatingActionButton button2;
    EditText editText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        listView2 = findViewById(R.id.listView2);
        ArrayList<PersonChat> listData2 = new ArrayList<>();
        PersonChatAdapter adapter2 = new PersonChatAdapter(this, R.layout.chat_row, listData2);
        listTemp = new ArrayList<>();
        listView2.setAdapter(adapter2);
        mDataBase2 = FirebaseDatabase.getInstance().getReference("Users").child(ChatsScreen.USER_KEY).child("chat").child(ChatsScreen.name);
        mDataBase3 = FirebaseDatabase.getInstance().getReference("Users").child(ChatsScreen.name).child("chat").child(ChatsScreen.USER_KEY);

        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(listData2.size() > 0)listData2.clear();
                if (listTemp.size() > 0) listTemp.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    UserChat userChat = ds.getValue(UserChat.class);
                    assert userChat != null;
                    listData2.add(new PersonChat(R.drawable.ic_launcher_background, userChat.name, userChat.message));
                    listTemp.add(userChat);
                }
                adapter2.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDataBase2.addValueEventListener(vListener);

        editText2 = findViewById(R.id.editText2);
        button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editText2.getText().toString();
                UserChat userChat = new UserChat(ChatsScreen.USER_KEY, msg);
                mDataBase2.push().setValue(userChat);
                mDataBase3.push().setValue(userChat);
                editText2.setText("");
            }
        });

        listView2.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                new AlertDialog.Builder(Chat.this)
                        .setTitle("Удалить сообщение?")
                        .setMessage("Вы хотите удалить это сообщение?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final UserChat userChat = listTemp.get(position);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(ChatsScreen.USER_KEY);
                                Query query = ref.child("chat").child(ChatsScreen.name).orderByChild("message").equalTo(userChat.message);

                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                            appleSnapshot.getRef().removeValue();

                                            if(ChatsScreen.USER_KEY.equals(userChat.name)){
                                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(ChatsScreen.name);
                                                Query query = ref.child("chat").child(ChatsScreen.USER_KEY).orderByChild("message").equalTo(userChat.message);

                                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                                            appleSnapshot.getRef().removeValue();
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                            Toast.makeText(Chat.this, "Сообщение удалено", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton("Нет", null)
                        .show();
                return true;
            }
        });
    }

    private class PersonChatAdapter extends ArrayAdapter<PersonChat>{
        private Context mContext;
        private int mResource;


        public PersonChatAdapter(@NonNull Context context, int resource, @NonNull ArrayList<PersonChat> objects) {
            super(context, resource, objects);
            this.mContext = context;
            this.mResource = resource;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(mResource, parent, false);

            ImageView imageView = convertView.findViewById(R.id.imageName);
            TextView txtName = convertView.findViewById(R.id.txtName);
            TextView txtMessage = convertView.findViewById(R.id.txtMessage);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(getItem(position).getName()).child("image");
            ValueEventListener vListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    for(DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        String msg = ds.getValue(String.class);
                        assert msg != null;
                        Picasso.get().load(msg).into(imageView);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            ref.addValueEventListener(vListener);

            imageView.setImageResource(getItem(position).getImage());
            txtName.setText(getItem(position).getName());
            txtMessage.setText(getItem(position).getMessage());

            return convertView;
        }
    }
}