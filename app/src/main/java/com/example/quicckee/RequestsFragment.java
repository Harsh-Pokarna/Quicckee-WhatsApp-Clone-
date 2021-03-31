package com.example.quicckee;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RequestsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestsFragment extends Fragment {
    private View requestFragmentView;
    private RecyclerView myRequestList;
    private DatabaseReference chatRequestsRef, usersRef, contactsRef;
    private FirebaseAuth mAuth;
    private String  currentUserId;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RequestsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RequestsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestsFragment newInstance(String param1, String param2) {
        RequestsFragment fragment = new RequestsFragment();
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
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        requestFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);
        myRequestList = requestFragmentView.findViewById(R.id.chat_requests_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return requestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(chatRequestsRef.child(currentUserId),Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull RequestsViewHolder holder, int position, @NonNull Contacts model) {
                        holder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.VISIBLE);
                        final String list_user_id = getRef(position).getKey();
                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();
                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    String type = snapshot.getValue().toString();
                                    if (type.equals("received")){
                                        usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.hasChild("image")){
                                                    final String requestProfileImage = snapshot.child("image").getValue().toString();
                                                    Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                                }
                                                final String requestUserName = snapshot.child("name").getValue().toString();
                                                final String requestUserStatus = snapshot.child("status").getValue().toString();

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText(requestUserStatus);
                                                holder.itemView.findViewById(R.id.request_accept_button).setOnClickListener(v -> contactsRef.child(currentUserId).child(list_user_id)
                                                                .child("Contact").setValue("Saved").addOnCompleteListener(task -> {
                                                                    if (task.isSuccessful()){
                                                                        contactsRef.child(list_user_id).child(currentUserId)
                                                                                .child("Contact").setValue("Saved").addOnCompleteListener(task1 -> {
                                                                            if (task1.isSuccessful()){
                                                                                chatRequestsRef.child(currentUserId).child(list_user_id).removeValue().addOnCompleteListener(task2 -> {
                                                                                    if (task2.isSuccessful()){
                                                                                        chatRequestsRef.child(list_user_id).child(currentUserId).removeValue().addOnCompleteListener(task3 -> {
                                                                                            Toast.makeText(getContext(), "Contact Added", Toast.LENGTH_SHORT).show();

                                                                                        });
                                                                                    }

                                                                                });

                                                                            }

                                                                        });
                                                                    }

                                                                }));

                                                holder.itemView.findViewById(R.id.request_cancel_button).setOnClickListener(v -> {
                                                    chatRequestsRef.child(currentUserId).child(list_user_id).removeValue().addOnCompleteListener(task2 -> {
                                                                if (task2.isSuccessful()){
                                                                    chatRequestsRef.child(list_user_id).child(currentUserId).removeValue().addOnCompleteListener(task3 -> {
                                                                        Toast.makeText(getContext(), "Accept Request Denied", Toast.LENGTH_SHORT).show();

                                                                    });
                                                                }

                                                            });



                                                });

//                                                holder.itemView.setOnClickListener(v -> {
//                                                    CharSequence options[] = new CharSequence[]{
//                                                            "Accept",
//                                                            "Cancel"
//                                                    };
//                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                                                    builder.setTitle(requestUserName +" Chat Request");
//                                                    builder.setItems(options, (dialog, which) -> {
//                                                        if (which == 0){
//                                                            contactsRef.child(currentUserId).child(list_user_id)
//                                                                    .child("Contact").setValue("Saved").addOnCompleteListener(task -> {
//                                                                        if (task.isSuccessful()){
//                                                                            contactsRef.child(list_user_id).child(currentUserId)
//                                                                                    .child("Contact").setValue("Saved").addOnCompleteListener(task1 -> {
//                                                                                if (task1.isSuccessful()){
//                                                                                    chatRequestsRef.child(currentUserId).child(list_user_id).removeValue().addOnCompleteListener(task2 -> {
//                                                                                        if (task2.isSuccessful()){
//                                                                                            chatRequestsRef.child(list_user_id).child(currentUserId).removeValue().addOnCompleteListener(task3 -> {
//                                                                                                Toast.makeText(getContext(), "Contact Added", Toast.LENGTH_SHORT).show();
//
//                                                                                            });
//                                                                                        }
//
//                                                                                    });
//
//                                                                                }
//
//                                                                            });
//                                                                        }
//
//                                                                    });
//
//                                                        }
//                                                        if (which == 1){
//                                                            chatRequestsRef.child(currentUserId).child(list_user_id).removeValue().addOnCompleteListener(task2 -> {
//                                                                if (task2.isSuccessful()){
//                                                                    chatRequestsRef.child(list_user_id).child(currentUserId).removeValue().addOnCompleteListener(task3 -> {
//                                                                        Toast.makeText(getContext(), "Accept Request Denied", Toast.LENGTH_SHORT).show();
//
//                                                                    });
//                                                                }
//
//                                                            });
//
//                                                        }
//
//                                                    });
//                                                    builder.show();
//
//                                                });

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        RequestsViewHolder viewHolder = new RequestsViewHolder(view);
                        return viewHolder;

                    }
                };

        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    private static class RequestsViewHolder extends RecyclerView.ViewHolder{
        TextView userName, userStatus;
        CircleImageView profileImage;
        Button acceptButton, cancelButton;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            acceptButton = itemView.findViewById(R.id.request_accept_button);
            cancelButton = itemView.findViewById(R.id.request_cancel_button);


        }
    }
}