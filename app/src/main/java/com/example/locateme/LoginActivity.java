package com.example.locateme;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;


public class LoginActivity extends AppCompatActivity {
    EditText mEmail,mPassword;
    Button mLoginBtn;
    TextView mCreateBtn;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    FirebaseFirestore firebaseFirestore;
    FusedLocationProviderClient mFusedLocationClient;
    GeoPoint geoPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail = findViewById(R.id.textEmail);
        mPassword = findViewById(R.id.textPassword);
        progressBar = findViewById(R.id.progressBar);
        fAuth = FirebaseAuth.getInstance();
        mLoginBtn = findViewById(R.id.Login);
        mCreateBtn = findViewById(R.id.Register);


        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required.");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is Required.");
                    return;
                }

                if(password.length() < 6){
                    mPassword.setError("Password Must be >= 6 Characters");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                // user authentication
                fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            FirebaseUser firebaseUser = fAuth.getCurrentUser();
                            firebaseFirestore = FirebaseFirestore.getInstance();

                            if(firebaseUser.isEmailVerified()){
                                startLocationService();
                                fetchUserInfo();
                                Toast.makeText(LoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(LoginActivity.this, "Email is not yet verified", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }


                        }else {
                            Toast.makeText(LoginActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }

                    }
                });

            }

            private void startLocationService() {
                if(!isLocationServiceRunning()){
                    Intent serviceIntent = new Intent(LoginActivity.this, LocationService.class);

                    if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                        LoginActivity.this.startForegroundService(serviceIntent);
                    }else{
                        startService(serviceIntent);
                    }
                }

            }

            private boolean isLocationServiceRunning(){
                ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                for (ActivityManager.RunningServiceInfo service: manager.getRunningServices(Integer.MAX_VALUE)){
                    if("com.example.locateme.LocationService".equals(service.service.getClassName())){
                        return true;
                    }
                }
                return false;
            }

            private void getLastKnownLocation() {
                Log.d("mytag", "getLastKnownLocation: called.");


                if (ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
                    @Override
                    public void onComplete(@NonNull Task<android.location.Location> task) {
                        if (task.isSuccessful()) {
                            Location location = task.getResult();
                            geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                            saveUserLocation();
                            startLocationService();
                        }
                    }
                });

            }

            private void saveUserLocation(){

                if(FirebaseAuth.getInstance().getUid() != null){
                    DocumentReference locationRef = FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(FirebaseAuth.getInstance().getUid());

                    locationRef.update("geopoint",geoPoint);
                }
            }


        });



        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
            }
        });


    }

    private void fetchUserInfo() {
        Intent intent = new Intent( LoginActivity.this, NavigationActivity.class );
        firebaseFirestore.collection( "users" ).document(fAuth.getCurrentUser().getUid())
                .get(  ).addOnCompleteListener( new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    intent.putExtra( "username",  task.getResult().getString( "username" ));
                    intent.putExtra( "email",  task.getResult().getString( "email" ));
                    intent.putExtra( "code",  task.getResult().getString( "code" ));
                    intent.putExtra( "isSharing",  task.getResult().getBoolean( "isSharing" ));
                    intent.putExtra( "address",  task.getResult().getString( "address" ));
                    startActivity(intent);
                    finish();
                }
            }
        } );
    }


}