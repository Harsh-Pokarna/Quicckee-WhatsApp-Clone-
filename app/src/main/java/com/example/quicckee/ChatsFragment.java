package com.example.quicckee;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.PrimitiveIterator;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment {
    View privateChatsView;
    private RecyclerView chatList;
    private String userImage = "default_Image";
    private DatabaseReference chatsRef, usersRef;
    private FirebaseAuth mAuth;
    String currentUserID;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);
        chatList = privateChatsView.findViewById(R.id.chats_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));

        return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(chatsRef, Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull Contacts model) {
                String userIDs = getRef(position).getKey();
                final String[] userImage = {"default image"};
                usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            if (snapshot.hasChild("image")){
                                userImage[0] = snapshot.child("image").getValue().toString();
                                Picasso.get().load(userImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }
                            String profileName = snapshot.child("name").getValue().toString();
                            String profileStatus = snapshot.child("status").getValue().toString();

                            holder.userName.setText(profileName);
                            holder.userStatus.setText("Last Seen: ");

                            holder.itemView.setOnClickListener(v -> {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("visit_user_image", userImage);
                                chatIntent.putExtra("visit_user_id", userIDs);
                                chatIntent.putExtra("visit_user_name", profileName);
                                startActivity(chatIntent);

                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                ChatsViewHolder viewHolder = new ChatsViewHolder(view);
                return viewHolder;
            }
        };
        chatList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImage;
        TextView userName, userStatus;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
        }
    }
}