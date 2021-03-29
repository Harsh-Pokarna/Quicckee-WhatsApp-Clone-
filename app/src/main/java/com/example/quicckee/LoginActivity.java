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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private Button loginButton, phoneLoginButton;
    private EditText userEmail, userPassword;
    private TextView forgetPasswordLink, needNewAccountLink;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        InitializeFields();
        mAuth = FirebaseAuth.getInstance();
        needNewAccountLink.setOnClickListener(v -> sendUserToRegisterActivity());
        loginButton.setOnClickListener(v -> {
            allowUserToLogin();

        });
        phoneLoginButton.setOnClickListener(v -> {
            Intent phoneloginIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
            startActivity(phoneloginIntent);

        });
    }

    private void allowUserToLogin() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter Email..", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter Password..", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Logging you in");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    sendUserToMainActivity();
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                }
                else {
                    String message = task.getException().toString();
                    Toast.makeText(this, "Error:" + message, Toast.LENGTH_SHORT).show();

                }
                loadingBar.dismiss();
            });

        }
    }

    private void InitializeFields() {
        loginButton = findViewById(R.id.login_button);
        phoneLoginButton = findViewById(R.id.phone_login_button);
        userEmail = findViewById(R.id.login_email);
        userPassword = findViewById(R.id.login_password);
        forgetPasswordLink = findViewById(R.id.forget_password_link);
        needNewAccountLink = findViewById(R.id.need_new_account_link);
        loadingBar = new ProgressDialog(this);
    }


    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void sendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}