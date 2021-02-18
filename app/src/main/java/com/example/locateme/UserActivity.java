package com.example.locateme;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends AppCompatActivity {

    TextView Username, Address, Email, Phone;
    CircleImageView dP;
    Button mSend, mRequest;
    String strUser, strEmail, strPhone, strAddress, UserID, geoLat, geoLong;
    FirebaseAuth mAuth;
    FirebaseFirestore mDb;
    final Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mDb = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        Username = (TextView)findViewById(R.id.txtUserProfileName);
        Address = (TextView)findViewById(R.id.txtResAddr);
        Email=(TextView)findViewById(R.id.tUsrEmail );
        Phone = (TextView)findViewById(R.id.txtUsrPhone);
        dP = (CircleImageView) findViewById(R.id.imgContact);
        mSend = (Button) findViewById(R.id.btn_sndMsg);
        mRequest = (Button)findViewById(R.id.btn_requestLocation);

        Intent intent = getIntent();

        strUser = intent.getStringExtra("UNAME");
        strEmail=intent.getStringExtra("EMAIL");
        strPhone=intent.getStringExtra("PHONE");
        strAddress=intent.getStringExtra("ADDR");
        geoLat=intent.getStringExtra( "geoLat" );
        geoLong=intent.getStringExtra( "geoLong" );

        Username.setText(strUser);
        Address.setText(strEmail);
        Phone.setText(strPhone);
        Email.setText(strAddress);

        mRequest.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserLocation();
            }
        } );

    }

    private void getUserLocation() {
        //CollectionReference locationReference = mDb.collection( "users" )
        if(geoLat!=null && geoLong!=null){
            //Toast.makeText(UserActivity.this, geoLat+","+geoLong, Toast.LENGTH_LONG ).show();
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage( "Open Google Maps?" )
                    .setCancelable( true )
                    .setPositiveButton( "Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri gmmIntentUri = Uri.parse("google.navigation:q="+geoLat+","+geoLong+"&mode=w");
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startActivity(mapIntent);
                        }
                    } )
                    .setNegativeButton( "No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    } );
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        }else{
            Toast.makeText(UserActivity.this, "User's doesn't share their location.", Toast.LENGTH_LONG ).show();
        }
    }
}
