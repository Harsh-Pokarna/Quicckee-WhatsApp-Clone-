package com.example.quicckee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView findFriendsRecyclerList;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        findFriendsRecyclerList = findViewById(R.id.find_friends_recycler_list);
        findFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));
        mToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(usersRef, Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull Contacts model) {
                        String visit_user_id = getRef(position).getKey();
                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        DatabaseReference specificContactRef = usersRef.child(visit_user_id);
                        specificContactRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.hasChild("image")){
                                 Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        holder.itemView.setOnClickListener(v -> {
                            Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                            profileIntent.putExtra("visit_user_id",visit_user_id);
                            startActivity(profileIntent);
                        });

                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                        return viewHolder;
                    }
                };
        findFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        TextView userName, userStatus;
        CircleImageView profileImage;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
        }
    }
}