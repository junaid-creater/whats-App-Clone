package com.example.whatsup;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment
{
    private View frndsView;
    private RecyclerView mRecyclerView;
    private TextView mEmpityList;

    private DatabaseReference mFriendsDatabaseRef, mUsersDatabaseRef, mChkFrndRef;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public FriendsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        frndsView = inflater.inflate(R.layout.fragment_friends, container, false);

        mEmpityList = frndsView.findViewById(R.id.empity_list);
        mRecyclerView = frndsView.findViewById(R.id.friends_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFriendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUser.getUid());
        mChkFrndRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        mFriendsDatabaseRef.keepSynced(true);
        mUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabaseRef.keepSynced(true);
        return frndsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mChkFrndRef.addValueEventListener(new ValueEventListener() {
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
                .setQuery(mFriendsDatabaseRef, Friends.class)
                .build();
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {

                final String userId = getRef(position).getKey();

                mUsersDatabaseRef.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        String profileImage = snapshot.child("image").getValue().toString();
                        final String profileName = snapshot.child("name").getValue().toString();
                        String profileStatus = snapshot.child("status").getValue().toString();

                        if (snapshot.hasChild("online")){
                            String userOnline = (String) snapshot.child("online").getValue().toString();
                            if (userOnline.equals("online")){
                                holder.userOnlineImg.setVisibility(View.VISIBLE);
                            } else {
                                holder.userOnlineImg.setVisibility(View.INVISIBLE);
                            }
                        }

                        holder.frndName.setText(profileName);
                        holder.frndStatus.setText(profileStatus);
                        Picasso.get().load(profileImage).placeholder(R.drawable.dp).into(holder.frndImage);

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0){

                                            Intent i = new Intent(getContext(),ProfileActivity.class);
                                            i.putExtra("user_id",userId);
                                            startActivity(i);

                                        }
                                        if (which == 1){

                                            Intent i = new Intent(getContext(),ChatActivity.class);
                                            i.putExtra("user_id",userId);
                                            i.putExtra("user_name",profileName);
                                            startActivity(i);

                                        }
                                    }
                                });
                                builder.show();
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
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);
                FriendsViewHolder viewHolder = new FriendsViewHolder(view);
                return viewHolder;
            }
        };

        mRecyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView frndName,frndStatus;
        CircleImageView frndImage;
        ImageView userOnlineImg;
        View mView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            frndName = itemView.findViewById(R.id.user_single_name);
            frndStatus = itemView.findViewById(R.id.user_single_status);
            frndImage = itemView.findViewById(R.id.user_single_image);
            userOnlineImg = itemView.findViewById(R.id.online_img);

        }
    }



}
