package com.example.whatsup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {


    private View reqstsView;
    private RecyclerView mRecyclerView;
    private TextView mEmpityList;

    private DatabaseReference mReqDatabaseRef, mUsersDatabaseRef, mChkReqRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        reqstsView = inflater.inflate(R.layout.fragment_requests, container, false);

        mEmpityList = reqstsView.findViewById(R.id.empity_list);
        mRecyclerView = reqstsView.findViewById(R.id.request_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mChkReqRef = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mReqDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(currentUser.getUid());
        mReqDatabaseRef.keepSynced(true);
        mUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabaseRef.keepSynced(true);
        return reqstsView;

    }

    @Override
    public void onStart() {
        super.onStart();
        mChkReqRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(currentUser.getUid())){
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mEmpityList.setVisibility(View.GONE);
                } else {
                    mRecyclerView.setVisibility(View.GONE);
                    mEmpityList.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

            FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Friends>()
                    .setQuery(mReqDatabaseRef, Friends.class)
                    .build();
            FirebaseRecyclerAdapter<Friends, RequestsViewHolder> adapter
                    = new FirebaseRecyclerAdapter<Friends, RequestsViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Friends model) {

                    final String userId = getRef(position).getKey();
                    mReqDatabaseRef.child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild("request_type")) {
                                String request_type = snapshot.child("request_type").getValue().toString();
                                holder.reqStatus.setText(request_type);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    mUsersDatabaseRef.child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String profileImage = snapshot.child("image").getValue().toString();
                            final String profileName = snapshot.child("name").getValue().toString();
                            //String profileStatus = snapshot.child("status").getValue().toString();

                            holder.reqName.setText(profileName);
                            //holder.reqStatus.setText(profileStatus);
                            Picasso.get().load(profileImage).placeholder(R.drawable.dp).into(holder.reqImage);

                            holder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(getContext(), ProfileActivity.class);
                                    i.putExtra("user_id", userId);
                                    startActivity(i);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                @NonNull
                @Override
                public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);
                    RequestsViewHolder viewHolder = new RequestsViewHolder(view);
                    return viewHolder;
                }
            };
            mRecyclerView.setAdapter(adapter);
            adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder
    {
        TextView reqName,reqStatus;
        CircleImageView reqImage;
        //ImageView userOnlineImg;
        View mView;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            reqName = itemView.findViewById(R.id.user_single_name);
            reqStatus = itemView.findViewById(R.id.user_single_status);
            reqImage = itemView.findViewById(R.id.user_single_image);
            //userOnlineImg = itemView.findViewById(R.id.online_img);

        }
    }

}
