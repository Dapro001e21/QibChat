package com.example.qibchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.service.autofill.UserData;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity4 extends AppCompatActivity {
    private ListView listView;
    private List<User> listTemp;

    public static final int IMAGE_CODE = 1;
    private CircleImageView imageProfile;
    private Uri uploadUri;
    private DatabaseReference mDataBase;
    private StorageReference mStorageRef;
    public static String name;
    public static boolean leave = false;
    public static User user;
    public static String USER_KEY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        USER_KEY = MainActivity3.USER_KEY;
        mStorageRef = FirebaseStorage.getInstance().getReference("ImageDB");

        listView = findViewById(R.id.listView);
        ArrayList<Person> listData = new ArrayList<>();
        PersonAdapter personAdapter = new PersonAdapter(this, R.layout.list_row, listData);
        listTemp = new ArrayList<>();
        listView.setAdapter(personAdapter);
        mDataBase = FirebaseDatabase.getInstance().getReference(USER_KEY).child("key");

        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (listData.size() > 0) listData.clear();
                if (listTemp.size() > 0) listTemp.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    assert user != null;
                    listData.add(new Person(R.drawable.ic_launcher_background, user.name));
                    listTemp.add(user);
                }
                personAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDataBase.addValueEventListener(vListener);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                user = listTemp.get(position);
                Intent i = new Intent(MainActivity4.this, Chat.class);
                name = user.name;
                startActivity(i);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final int which_item = position;

                new AlertDialog.Builder(MainActivity4.this)
                        .setTitle("Удалить чат?")
                        .setMessage("Вы хотите удалить этот чат?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                User user = listTemp.get(which_item);
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference(USER_KEY);
                                Query query = ref.child("key").orderByChild("name").equalTo(user.name);

                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                            appleSnapshot.getRef().removeValue();
                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(MainActivity4.USER_KEY).child("chat").child(user.name);
                                            ref.removeValue();
                                            Toast.makeText(MainActivity4.this, "Чат удален", Toast.LENGTH_SHORT).show();
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


        imageProfile = findViewById(R.id.imageProfile);
        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, IMAGE_CODE);
            }
        });


        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        Home();
        downloadImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            Uri uri = data.getData();
            imageProfile.setImageURI(uri);
            uploadImage();
        }
    }

    private void uploadImage(){
        Bitmap bitmap = ((BitmapDrawable) imageProfile.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        StorageReference mRef = mStorageRef.child(USER_KEY + "my_image");
        UploadTask up = mRef.putBytes(byteArray);
        Task<Uri> task = up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                uploadUri = task.getResult();
                DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference(USER_KEY).child("image").child("image");
                mRef2.setValue(uploadUri.toString());
            }
        });

    }

    private void downloadImage() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(USER_KEY).child("image");
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    String msg = ds.getValue(String.class);
                    assert msg != null;
                    Picasso.get().load(msg).into(imageProfile);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        ref.addValueEventListener(vListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()){
                        case R.id.nav_home:
                            Home();
                            break;
                        case R.id.nav_info:
                            Test();
                            break;
                    }
                    return true;
                }
            };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.Create_group:
                Intent create = new Intent(MainActivity4.this, CreatChat.class);
                startActivity(create);
                break;
            case R.id.leave:
                FirebaseAuth.getInstance().signOut();
                leave = true;
                Intent leave = new Intent(MainActivity4.this, MainActivity3.class);
                startActivity(leave);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void Home(){
        listView.setVisibility(View.VISIBLE);
        TextView textView = findViewById(R.id.textProfile);
        ImageView imageView = findViewById(R.id.imageProfile);
        textView.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
    }
    private void Test(){
        listView.setVisibility(View.GONE);
        ImageView imageView = findViewById(R.id.imageProfile);
        TextView textView = findViewById(R.id.textProfile);
        textView.setText(USER_KEY);
        textView.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
    }
    private class PersonAdapter extends ArrayAdapter<Person>{
        private Context mContext;
        private int mResource;


        public PersonAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Person> objects) {
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

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(getItem(position).getName()).child("image");
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

            return convertView;
        }
    }
}