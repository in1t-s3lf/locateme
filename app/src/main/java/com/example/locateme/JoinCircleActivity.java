package com.example.locateme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.goodiebag.pinview.Pinview;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class JoinCircleActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    CollectionReference documentReference;
    Button buttonSubmit;
    Pinview pinview;
    String UserId;
    String inviteCode;
    String circleOwner;
    DocumentSnapshot circleOwnerName;
    DocumentSnapshot circleUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_circle);

        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection("users");
        firebaseAuth = FirebaseAuth.getInstance();
        UserId = firebaseAuth.getCurrentUser().getUid();
        buttonSubmit = (Button)findViewById(R.id.btnJoin);
        pinview = (Pinview)findViewById(R.id.pinview);
        //inviteCode =  pinview.getValue();
        pinview.setPinViewEventListener(new Pinview.PinViewEventListener() {
            @Override
            public void onDataEntered(Pinview pinview, boolean fromUser) {
                //Make api calls here or what not
                //Toast.makeText(MyCircleActivity.this, pinview.getValue(), Toast.LENGTH_SHORT).show();
                inviteCode = pinview.getValue();
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("users").whereEqualTo("code", inviteCode)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (final QueryDocumentSnapshot document : task.getResult()) {
                                        //Toast.makeText(MyCircleActivity.this, document.getId() + " => " + document.getData(), Toast.LENGTH_LONG).show();
                                        //Toast.makeText(MyCircleActivity.this, inviteCode, Toast.LENGTH_LONG).show();
                                        circleOwner = document.getId();
                                        if(!circleOwner.isEmpty()) {
                                            //Toast.makeText(MyCircleActivity.this, "working", Toast.LENGTH_LONG).show();
                                            if(!UserId.equals(circleOwner)){
                                                FirebaseFirestore.getInstance().collection("users")
                                                        .document(UserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        DocumentSnapshot circleOwnerName = task.getResult();
                                                        Map<String, Object> myCircle = new HashMap<>();
                                                        myCircle.put("mycircles", UserId);
                                                        myCircle.put("username", circleOwnerName.getString("username"));
                                                        firebaseFirestore.collection("users")
                                                                .document(circleOwner)
                                                                .collection("mycircles")
                                                                .add(myCircle);
                                                        //Send circle owner notification that someone joined their group
                                                    }
                                                });
                                                FirebaseFirestore.getInstance().collection("users")
                                                        .document(circleOwner).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        DocumentSnapshot circleUser = task.getResult();
                                                        Map<String, Object> joinedCircle = new HashMap<>();
                                                        joinedCircle.put("joinedcircles", circleOwner);
                                                        joinedCircle.put("username", circleUser.getString("username"));
                                                        firebaseFirestore.collection("users")
                                                                .document(UserId)
                                                                .collection("joinedcircles")
                                                                .add(joinedCircle);
                                                        Toast.makeText(JoinCircleActivity.this, "You joined "+circleUser.getString("username")+"\'s circle.", Toast.LENGTH_LONG).show();
                                                    }
                                                });


                                            }else{
                                                Toast.makeText(JoinCircleActivity.this, "You can\'t join your own group", Toast.LENGTH_LONG).show();
                                            }


                                        }
                                    }


                                }
                                else{
                                    Toast.makeText(JoinCircleActivity.this, "not working", Toast.LENGTH_LONG).show();
                                }
                            }

                        });
            }
        });
    }
}
