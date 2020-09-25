package com.example.insta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import Model.Post;

public class ViewPost extends AppCompatActivity {

    DatabaseReference databaseReference;
    FirebaseFirestore fStore;

    String UID, postID;

    TextView date, profileName, caption, love_number;
    ImageView love, postImage, profileImage;
    public ProgressBar bar;


    List<String> names;
    ListView listView;
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);


        if (databaseReference == null) {
            databaseReference = FirebaseDatabase.getInstance().getReference();
        }

        fStore = FirebaseFirestore.getInstance();

        names = new ArrayList<>();
        listView = findViewById(R.id.likes_list);
        adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, names);
        listView.setAdapter(adapter);


        Bundle bundle = getIntent().getExtras();
        UID = bundle.getString("UID");
        postID = bundle.getString("Post ID");

        caption = findViewById(R.id.caption);
        postImage = findViewById(R.id.post_image);
        love_number = findViewById(R.id.love_number);
        bar = findViewById(R.id.progress_bar);
        profileImage = findViewById(R.id.profile_image);
        date = findViewById(R.id.date);
        profileName = findViewById(R.id.profile_name);
        love = findViewById(R.id.love);

        getPost();
    }

    @SuppressLint("SetTextI18n")
    private void getPost() {
        fStore.collection("Users")
                .document(UID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(DocumentSnapshot ds) {
                        byte[] bytes = ds.getBlob("Profile Pic").toBytes();
                        profileImage.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                        profileName.setText(ds.getString("Name"));
                    }
                });

        fStore.collection("Users")
                .document(UID)
                .collection("Posts")
                .document(postID)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot ds) {
                Post post = ds.toObject(Post.class);
                postImage.setImageBitmap(BitmapFactory.decodeByteArray(post.getPostImage()
                        .toBytes(), 0, post.getPostImage().toBytes().length));
                date.setText(post.getTime().toDate().toString());
                caption.setText(post.getCaption());
                love_number.setText(String.valueOf(post.getLikes().size()));
                love_number.append(" Loves");
                love.setImageResource(post.getLiked() ? R.drawable.loved : R.drawable.love);
                bar.setVisibility(View.GONE);
            }
        });

    }
}
