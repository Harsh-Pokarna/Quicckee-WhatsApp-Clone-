package com.example.quicckee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private Button createAccountButton;
    private EditText userEmail, userPassword;
    private TextView alreadyHaveAccountLink;

    private FirebaseAuth mAuth;
    private DatabaseReference rootReference;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        rootReference = FirebaseDatabase.getInstance().getReference();
        InitializeFields();
        alreadyHaveAccountLink.setOnClickListener(v -> sendUserToLoginActivity());
        createAccountButton.setOnClickListener(v -> createNewAccount());
    }

    private void createNewAccount() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter Email..", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter Password..", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    String currentUserID = mAuth.getCurrentUser().getUid();
                    rootReference.child("Users").child(currentUserID).setValue("");
                    Toast.makeText(this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    sendUserToMainActivity();
                }
                else {
                    loadingBar.dismiss();
                    String message = task.getException().toString();
                    System.out.println(message);
                    Toast.makeText(this, "Error:" + message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void InitializeFields() {
        createAccountButton = findViewById(R.id.register_button);
        userEmail = findViewById(R.id.register_email);
        userPassword = findViewById(R.id.register_password);
        alreadyHaveAccountLink = findViewById(R.id.already_have_account_link);
        loadingBar = new ProgressDialog(this);
    }
}