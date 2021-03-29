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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {
    private Button sendVerificationCodeButton, verifyCodeButton;
    private EditText inputPhoneNumber, inputVerificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        sendVerificationCodeButton = findViewById(R.id.send_verification_code);
        verifyCodeButton = findViewById(R.id.verify_account_button);
        inputPhoneNumber = findViewById(R.id.phone_number_input);
        inputVerificationCode = findViewById(R.id.verification_code_input);
        mAuth = FirebaseAuth.getInstance();
        sendVerificationCodeButton.setOnClickListener(v -> {
            String phoneNumber = inputPhoneNumber.getText().toString();
            if (TextUtils.isEmpty(phoneNumber)){
                Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
            }
            else{
                PhoneAuthOptions options =
                        PhoneAuthOptions.newBuilder(mAuth)
                                .setPhoneNumber(phoneNumber)       // Phone number to verify
                                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                .setActivity(this)                 // Activity (for callback binding)
                                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                .build();
                PhoneAuthProvider.verifyPhoneNumber(options);
            }

        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);


            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                String msg = e.getMessage();
                Toast.makeText(PhoneLoginActivity.this, "Verification Failed" + msg, Toast.LENGTH_SHORT).show();

            }


            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
                Toast.makeText(PhoneLoginActivity.this, "Code has been sent", Toast.LENGTH_SHORT).show();
                verifyCodeButton.setVisibility(View.VISIBLE);
                inputVerificationCode.setVisibility(View.VISIBLE);
            }

        };

        verifyCodeButton.setOnClickListener(v -> {
            String verificationCode = inputVerificationCode.getText().toString();
            if (TextUtils.isEmpty(verificationCode)){
                Toast.makeText(this, "Please enter the code", Toast.LENGTH_SHORT).show();
            }
            else{
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                signInWithPhoneAuthCredential(credential);
            }

        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        sendUserToMainActivity();


                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}