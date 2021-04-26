package com.example.quicckee;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessagesViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    public MessageAdapter(List<Messages> userMessagesList){
        this.userMessagesList = userMessagesList;
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder{
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;

        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
        }
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout, parent,false);
        mAuth = FirebaseAuth.getInstance();
        MessagesViewHolder viewHolder = new MessagesViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesViewHolder holder, int position) {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);
        String  fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("image")){
                    String receiverProfileImage = snapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverProfileImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if (fromMessageType.equals("text")){
            holder.receiverMessageText.setVisibility(View.INVISIBLE);
            holder.receiverProfileImage.setVisibility(View.INVISIBLE);
            if (fromUserID.equals(messageSenderID)){
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageText.setText(messages.getMessage());
            }
            else{
                holder.senderMessageText.setVisibility(View.INVISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE );
                holder.receiverMessageText.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessageText.setText(messages.getMessage());

            }
        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }



}
