package com.example.quicckee;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.EventListener;
import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {
    private ImageView profileImage;
    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private String  currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootReference;
    private static final int galleryPick =1;
    private StorageReference userProfileImageRef;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootReference = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        Initialisefields();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        updateAccountSettings.setOnClickListener(v -> updateSettings());
        RetreiveUserInfo();
        profileImage.setOnClickListener(v -> {
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, galleryPick);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                Uri resultUri = result.getUri();

                StorageReference filePath = userProfileImageRef.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                    final String downloadUrl = uri.toString();
                    rootReference.child("Users").child(currentUserID).child("image").setValue(downloadUrl)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()){
                                    Toast.makeText(SettingsActivity.this, "Image saved in database", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    String message = task.getException().toString();
                                    Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                                }

                            });
                }));
            }
        }
    }

    private void RetreiveUserInfo() {
        rootReference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("image"))){
                    String retreiveUserName = snapshot.child("name").getValue().toString();
                    String retreiveUserStatus = snapshot.child("status").getValue().toString();
                    String retreiveProfileImage = snapshot.child("image").getValue().toString();

                    userName.setText(retreiveUserName);
                    userStatus.setText(retreiveUserStatus);
                    Picasso.get().load(retreiveProfileImage).into(profileImage);
                }
                else if ((snapshot.exists()) && (snapshot.hasChild("name"))){
                    String retreiveUserName = snapshot.child("name").getValue().toString();
                    String retreiveUserStatus = snapshot.child("status").getValue().toString();
                    userName.setText(retreiveUserName);
                    userStatus.setText(retreiveUserStatus);
                }
                else {
                    Toast.makeText(SettingsActivity.this, "Please update your profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void Initialisefields() {
        profileImage = findViewById(R.id.set_profile_image);
        updateAccountSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        mToolbar = findViewById(R.id.settings_app_bar);
    }
    private void updateSettings() {
        String setUserName = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();
        if (TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setUserStatus)){
            Toast.makeText(this, "Please enter your status", Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, Object > profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName);
            profileMap.put("status", setUserStatus);

        rootReference.child("Users").child(currentUserID).updateChildren(profileMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                sendUserToMainActivity();
                Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();

            }
            else{
                String message = task.getException().toString();
                Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


}