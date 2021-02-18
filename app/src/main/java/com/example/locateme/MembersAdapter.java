package com.example.locateme;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MembersAdapter extends FirestoreRecyclerAdapter<CreateUser, MembersAdapter.MemberHolder> {
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    private  onItemClickListener listener;
    public MembersAdapter(@NonNull FirestoreRecyclerOptions<CreateUser> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull MemberHolder memberHolder, int i, @NonNull CreateUser createUser) {
        memberHolder.userName.setText(createUser.getUsername());
    }

    @NonNull
    @Override
    public MemberHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new MemberHolder(v);
    }

    public  void deleteItem(int position){
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class   MemberHolder extends  RecyclerView.ViewHolder{
        TextView userName;

        public MemberHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.item_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position !=  RecyclerView.NO_POSITION &&  listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface onItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public  void setOnClickListener(onItemClickListener listener){
        this.listener = listener;
    }
}
