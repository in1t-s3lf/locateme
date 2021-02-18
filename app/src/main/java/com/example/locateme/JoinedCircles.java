package com.example.locateme;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;

public class JoinedCircles extends AppCompatActivity {


    ImageView mStatus;
    GeoPoint geoPoint;
    MembersAdapter adapter;
    DocumentReference statusRef;
    String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    CollectionReference userCollection = FirebaseFirestore.getInstance().collection("users")
            .document(UserId).collection("joinedcircles");




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_circle);
        setUpRecyclerView();
        mStatus =(ImageView)findViewById( R.id.imgRed );


    }


    private void setUpRecyclerView() {
        Query query = userCollection.orderBy("username", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<CreateUser> options = new FirestoreRecyclerOptions.Builder<CreateUser>()
                .setQuery(query, CreateUser.class)
                .build();

        adapter = new MembersAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if(direction==ItemTouchHelper.LEFT){
                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(JoinedCircles.this);
                    builder.setMessage( "Delete user from circle?" )
                            .setCancelable( true )
                            .setPositiveButton( "Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    adapter.deleteItem(viewHolder.getAdapterPosition());
                                    Toast.makeText(JoinedCircles.this, "User deleted.", Toast.LENGTH_LONG).show();
                                }
                            } )
                            .setNegativeButton( "No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            } );
                    androidx.appcompat.app.AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                else if (direction==ItemTouchHelper.RIGHT){
                    Toast.makeText(JoinedCircles.this, "You swiped right"+viewHolder.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnClickListener(new MembersAdapter.onItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                CreateUser createUser = documentSnapshot.toObject(CreateUser.class);
                String path = documentSnapshot.getReference().getPath();
                String id = documentSnapshot.getString("joinedcircles");
                String status = documentSnapshot.getString("status");
                //Toast.makeText(MyCircle.this, id + ": " + uname, Toast.LENGTH_LONG ).show();

                FirebaseFirestore.getInstance().collection("users").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot circleOwnerName = task.getResult();
                        Intent intent = new Intent(JoinedCircles.this, UserActivity.class);
                        intent.putExtra("UNAME", circleOwnerName.getString("username"));
                        intent.putExtra("EMAIL", circleOwnerName.getString("email"));
                        intent.putExtra("ADDR", circleOwnerName.getString("address"));
                        intent.putExtra("PHONE", circleOwnerName.getString("phone"));


                        if (circleOwnerName.get("isSharing").toString().equals("true")){
                            intent.putExtra("geoLat", String.valueOf(circleOwnerName.getGeoPoint("geopoint").getLatitude()));
                            intent.putExtra("geoLong", String.valueOf(circleOwnerName.getGeoPoint("geopoint").getLongitude()));
                            startActivity(intent);

                        }else{
                            startActivity(intent);
                        }
                    }
                });
            }
        });
    }



    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
