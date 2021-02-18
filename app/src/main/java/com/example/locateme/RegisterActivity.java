package com.example.locateme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "EmailPassword";
    EditText mFullName,mEmail,mPhone,mPassword, mAddress;
    Button mRegister;
    TextView mLogin;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore mDb;
    ProgressBar progressBar;
    String  userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFullName   = findViewById(R.id.txtUsr);
        mEmail      = findViewById(R.id.textEmail);
        mPassword   = findViewById(R.id.textPassword);
        mPhone      = findViewById(R.id.txtPhone);
        mAddress    = findViewById( R.id.textAddress );
        mRegister   = findViewById(R.id.Register);
        mLogin      = findViewById(R.id.btnlogin);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);
        firebaseUser = mAuth.getCurrentUser();

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                final String fullName = mFullName.getText().toString();
                final String phone    = mPhone.getText().toString();
                final String address = mAddress.getText().toString();

                if(TextUtils.isEmpty(fullName)){
                    mFullName.setError("Name is Required.");
                    return;
                }
                if(TextUtils.isEmpty(phone)){
                    mPhone.setError("Phone number is Required.");
                    return;
                }
                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required.");
                    return;
                }
                if(TextUtils.isEmpty(address)){
                    mAddress.setError("Address is Required.");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is Required.");
                    return;
                }

                if(password.length() < 6){
                    mPassword.setError("Password Must be at least 6 Characters");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);



                // User Registration
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            Random r = new Random();
                            int n = 100000 + r.nextInt(900000);
                            String code = String.valueOf(n);

                            userID = mAuth.getCurrentUser().getUid();
                            Map<String,Object> user = new HashMap<>();
                            user.put("username",fullName);
                            user.put("email",email);
                            user.put("phone",phone);
                            user.put( "address", address );
                            user.put("code", code);
                            user.put( "online", false );
                            user.put("isSharing", false);
                            user.put("id", userID);

                            mDb.collection("users").document(userID)
                                        .set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                            sendVerificationEmail();
                                            //Log.d(TAG, "onSuccess: user Profile is created for "+ userID);
                                        }

                                private void sendVerificationEmail() {
                                    firebaseUser.sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task ){
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(RegisterActivity.this, "Email sent for verification. Please check your inbox.", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                        mAuth.signOut();
                                                        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                                                    }
                                                    else {

                                                    }
                                                }
                                            });
                                }
                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: " + e.toString());
                                            }
                                    });

                        }else {
                            Toast.makeText(RegisterActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });



        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            }
        });

    }



}


