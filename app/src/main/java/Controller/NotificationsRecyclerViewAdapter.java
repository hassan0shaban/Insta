package Controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.text.style.AlignmentSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.insta.R;
import com.example.insta.VisitProfile;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import Model.Notification;

public class NotificationsRecyclerViewAdapter extends RecyclerView.Adapter<NotificationsRecyclerViewAdapter.ViewHolder> {

    List<Notification> notifications;
    Context context;

    FirebaseAuth auth;
    FirebaseUser user;
    String currentUID;
    DatabaseReference dbr;
    FirebaseDatabase db;
    StorageReference sr;
    FirebaseStorage fs;


    public NotificationsRecyclerViewAdapter(List<Notification> notifications, Context context) {
        this.notifications = notifications;
        this.context = context;

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        currentUID = user.getUid();
        db = FirebaseDatabase.getInstance();
        dbr = db.getReference();
        fs = FirebaseStorage.getInstance();
        sr = fs.getReference();
    }

    @NonNull
    @Override
    public NotificationsRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new LikeHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.like_view_card, parent, false));
        } else if (viewType == 2) {
            return new FriendRequestHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_request_view_card, parent, false));
        }
        return null;
    }


    @Override
    public int getItemViewType(int position) {
        if (notifications.get(position).getType().equals("Like")) {
            return 1;
        } else if (notifications.get(position).getType().equals("Friend Request")) {
            return 2;
        }
        return 0;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        switch (notifications.get(position).getType()) {
            case "Like":
                final LikeHolder lh = (LikeHolder) holder;
                lh.ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Intent intent = new Intent(context, PostView.class);
//                        intent.putExtra("Post ID", notifications.get(position).getPostID());
//                        context.startActivity(intent);
                    }
                });

                dbr.child("Users").child(notifications.get(position).getUID()).child("Name")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                lh.message.setText(snapshot.getValue().toString() + " Liked your picture");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                sr.child("Users").child(notifications.get(position).getUID()).child("Profile Pic")
                        .getBytes(4096 * 4096)
                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] b) {
                                lh.profilePic.setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.length));
                            }
                        });
                break;

            case "Friend Request":
                final FriendRequestHolder fh = (FriendRequestHolder) holder;
                fh.ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, VisitProfile.class);
                        intent.putExtra("Token", notifications.get(position).getUID());
                        context.startActivity(intent);
                    }
                });
                fh.confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirm(position);
                        notifications.remove(position);
                        notifyDataSetChanged();
                    }
                });
                fh.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        delete(position);
                        notifications.remove(position);
                        notifyDataSetChanged();
                    }
                });

                dbr.child("Users").child(notifications.get(position).getUID()).child("Name")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        fh.message.setText(snapshot.getValue().toString() + "\nSent you a friend request");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                sr.child("Users").child(notifications.get(position).getUID()).child("Profile Pic")
                        .getBytes(4096 * 4096)
                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] b) {
                                fh.profilePic.setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.length));
                            }
                        });
        }


    }

    private void delete(int i) {
        DatabaseReference dRef = dbr.child("Users").child(auth.getUid());

        DatabaseReference dRef2 = dbr.child("Users").child(notifications.get(i).getUID());

        dRef.child("Friend Requests").child(notifications.get(i).getUID()).removeValue();

        dRef2.child("Friend Requests").child(auth.getUid()).removeValue();

        Toast.makeText(context, "Request deleted", Toast.LENGTH_SHORT).show();
    }

    private void confirm(int i) {
        DatabaseReference dRef = dbr.child("Users").child(auth.getUid());
        dRef.child("Friends").child(notifications.get(i).getUID()).setValue(notifications.get(i).getUID());

        DatabaseReference dRef2 = dbr.child("Users").child(notifications.get(i).getUID());
        dRef2.child("Friends").child(auth.getUid()).setValue(auth.getUid());

        dRef.child("Friend Requests").child(notifications.get(i).getUID()).removeValue();

        dRef2.child("Friend Requests").child(auth.getUid()).removeValue();

        Toast.makeText(context, "Request accepted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class LikeHolder extends ViewHolder {
        ImageView profilePic;
        TextView message;
        LinearLayout ll;

        public LikeHolder(@NonNull View v) {
            super(v);
            profilePic = v.findViewById(R.id.profile_pic);
            message = v.findViewById(R.id.request_message);
            ll = v.findViewById(R.id.ll);
        }
    }

    public class FriendRequestHolder extends ViewHolder {
        ImageView profilePic;
        TextView message, confirm, delete;
        LinearLayout ll;

        public FriendRequestHolder(@NonNull View v) {
            super(v);
            profilePic = v.findViewById(R.id.profile_pic);
            message = v.findViewById(R.id.request_message);
            ll = v.findViewById(R.id.ll);
            confirm = v.findViewById(R.id.confirm);
            delete = v.findViewById(R.id.delete);
        }
    }


}