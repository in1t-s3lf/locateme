package com.example.locateme;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    Uri imageUri;
    CircleImageView circleImageView;
    TextView username, mAddress, mlogout, mhelp, mEmail, mCode;
    Button mSave, mPassChange;
    Switch mNotifications, mShareLocation;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    FirebaseStorage firebaseStorage;
    StorageReference storageRef;
    private String Sharing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        mCode = (TextView)findViewById( R.id.txtCode );
        mEmail = (TextView)findViewById( R.id.txtEmail );
        mlogout = (TextView)findViewById( R.id.btnstlogout );
        circleImageView = (CircleImageView) findViewById(R.id.circleImgView);
        username = (TextView) findViewById(R.id.txtUsername);
        mAddress = (TextView) findViewById(R.id.txtAddress);
        mPassChange = (Button) findViewById(R.id.btnChangePass);
        mNotifications = (Switch) findViewById(R.id.swchNot);
        mShareLocation = (Switch) findViewById(R.id.swchShare);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageRef = firebaseStorage.getReference();

        final String userId = firebaseAuth.getCurrentUser().getUid();
        final StorageReference userRef = storageRef.child(userId+".jpg");
        final StorageReference userImagesRef = storageRef.child("images/"+userId+".jpg");


        Intent userData = getIntent();
        if(userData!=null){
            username.setText( userData.getStringExtra("username") );
            mEmail.setText( userData.getStringExtra("email") );
            mCode.setText("Circle ID: "+userData.getStringExtra("code"));
            mAddress.setText( userData.getStringExtra("address") );
            if(userData.getBooleanExtra("isSharing", false)){
                //Toast.makeText( SettingsActivity.this,"True", Toast.LENGTH_SHORT ).show();
                mShareLocation.setChecked(true);

            }
            else {
                mShareLocation.setChecked(false);
            }

        }
        mShareLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Sharing = "true";
                    firebaseFirestore.collection("users").document(userId)
                            .update("isSharing", true);
                    Toast.makeText( SettingsActivity.this,"Location sharing: On", Toast.LENGTH_SHORT ).show();
                }else{
                    Sharing = "false";
                    firebaseFirestore.collection("users").document(userId)
                            .update("isSharing", false);
                    Toast.makeText( SettingsActivity.this,"Location sharing: Off", Toast.LENGTH_SHORT ).show();
                }
            }
        });

        mlogout.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder( SettingsActivity.this );
                builder.setMessage( "You\'re about to sign-out. Continue?")
                        .setCancelable( true )
                        .setPositiveButton( "Yes",  new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                                dialog.dismiss();
                            }
                        } )
                        .setNegativeButton( "No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        } );
                final android.app.AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        } );

        mAddress.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);
                View view = getLayoutInflater().inflate( R.layout.address, null );

                final EditText newAddress = (EditText)view.findViewById( R.id.txtResAddress );
                newAddress.setText( mAddress.getText() );
                Button btnCancel = (Button)view.findViewById( R.id.btnCancel );
                Button btnSave = (Button)view.findViewById( R.id.btnSave );


                alert.setView( view );

                final AlertDialog alertDialog = alert.create();
                alertDialog.setCanceledOnTouchOutside( false );

                btnCancel.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                } );

                btnSave.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String address    = newAddress.getText().toString();
                        if(TextUtils.isEmpty(address)){
                            newAddress.setError("Address is Required.");
                            return;
                        }

                        firebaseFirestore.collection("users").document(userId)
                                .update("address", address);
                        mAddress.setText( address );
                        alertDialog.dismiss();
                        Toast.makeText( SettingsActivity.this, "Address updated!", Toast.LENGTH_LONG ).show();


                    }
                } );

                alertDialog.show();

            }
        } );

        /*
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String address    = mAddress.getText().toString();
                if(TextUtils.isEmpty(address)){
                    mAddress.setError("Address is Required.");
                    return;
                }

                final Map<String,Object> userInfo = new HashMap<>();
                userInfo.put("address", address);
                userInfo.put("isSharing", Sharing);

                userRef.getName().equals(userImagesRef.getName());    // true
                userRef.getPath().equals(userImagesRef.getPath());    // false
                userRef.putFile(imageUri);

                firebaseFirestore.collection("users").document(userId)
                        .update(userInfo);

            }
        });
         */


    }
}