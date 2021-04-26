package com.example.quicckee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private String receiverUserID, senderUserID, current_State;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton, declineChatRequest;

    private DatabaseReference userRef, chatRequestRef, contactsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();


        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        userProfileName = findViewById(R.id.visit_profile_name);
        sendMessageRequestButton = findViewById(R.id.send_message_request_button);
        declineChatRequest = findViewById(R.id.decline_message_request_button);
        current_State = "new";

        retrieveUserInfo();
    }

    private void retrieveUserInfo() {
        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("image"))){
                    String userImage = snapshot.child("image").getValue().toString();
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequest();
                }
                else {
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequest();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void manageChatRequest() {
        chatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(receiverUserID)){
                    String request_type = snapshot.child(receiverUserID).child("request_type").getValue().toString();
                    if (request_type.equals("sent")){
                        current_State = "request_sent";
                        sendMessageRequestButton.setText("Cancel Request");
                    }
                    else if (request_type.equals("received")){
                        current_State = "request_received";
                        sendMessageRequestButton.setText("Accept Request");

                        declineChatRequest.setVisibility(View.VISIBLE);
                        declineChatRequest.setEnabled(true);
                        declineChatRequest.setOnClickListener(v -> cancelChatRequest());
                    }

                }
                else {
                    contactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(receiverUserID)){
                                current_State = "friends";
                                sendMessageRequestButton.setText("Remove Friend");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if (!senderUserID.equals(receiverUserID)){
            sendMessageRequestButton.setOnClickListener(v -> {
                sendMessageRequestButton.setEnabled(false);
                if (current_State.equals("new")){
                    sendChatRequest();
                }
                if (current_State.equals("request_sent")){
                    cancelChatRequest();
                }
                if (current_State.equals("request_received")){
                    acceptChatRequest();
                }
                if (current_State.equals("friends")){
                    removeSpecificContact();
                }
            });
        }
        else {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void removeSpecificContact() {
        contactsRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                contactsRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(task1 -> {
                    if (task.isSuccessful()){
                        sendMessageRequestButton.setEnabled(true);
                        sendMessageRequestButton.setText("Send Request");
                        current_State = "new";

                        declineChatRequest.setVisibility(View.INVISIBLE);
                        declineChatRequest.setEnabled(false);
                    }

                });

            }

        });

    }


    private void acceptChatRequest() {
        contactsRef.child(senderUserID).child(receiverUserID).child("Contacts").setValue("Saved")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        contactsRef.child(receiverUserID).child(senderUserID).child("Contacts").setValue("Saved")
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()){
                                        chatRequestRef.child(senderUserID).child(receiverUserID).removeValue()
                                                .addOnCompleteListener(task2 -> {
                                                    if (task2.isSuccessful()){
                                                        chatRequestRef.child(receiverUserID).child(senderUserID).removeValue()
                                                                .addOnCompleteListener(task3 -> {
                                                                    sendMessageRequestButton.setEnabled(true);
                                                                    current_State = "friends";
                                                                    sendMessageRequestButton.setText("Remove Friend");
                                                                    declineChatRequest.setVisibility(View.INVISIBLE);
                                                                    declineChatRequest.setEnabled(false);

                                                                });
                                                    }

                                                });

                                    }

                                });

                    }

                });

    }

    private void cancelChatRequest() {
        chatRequestRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                chatRequestRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(task1 -> {
                    if (task.isSuccessful()){
                        sendMessageRequestButton.setEnabled(true);
                        sendMessageRequestButton.setText("Send Request");
                        current_State = "new";

                        declineChatRequest.setVisibility(View.INVISIBLE);
                        declineChatRequest.setEnabled(false);
                    }

                });

            }

        });
    }


    private void sendChatRequest() {
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent").addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        chatRequestRef.child(receiverUserID).child(senderUserID)
                                .child("request_type").setValue("received")
                                .addOnCompleteListener(task1 -> {
                                    if (task.isSuccessful()){
                                        sendMessageRequestButton.setEnabled(true);
                                        current_State = "request_sent";
                                        sendMessageRequestButton.setText("Cancel Request");
                                    }
                                });
                    }

                });
    }


}