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
public class ChatsFragment extends Fragment {

    private View chatsView;
    private RecyclerView mChatList;
    private TextView mEmpityList;

    private DatabaseReference mChatsDatabaseRef, mUsersDatabaseRef, mChkChatRef, mFriendChk;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        chatsView =  inflater.inflate(R.layout.fragment_chats, container, false);

        mEmpityList = chatsView.findViewById(R.id.empity_list);
        mChatList = chatsView.findViewById(R.id.chats_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mChatList.setHasFixedSize(true);
        mChatList.setLayoutManager(layoutManager);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

//        mFriendChk = FirebaseDatabase.getInstance().getReference().child("Friends");
        mChkChatRef = FirebaseDatabase.getInstance().getReference().child("Chat");
        mChatsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Chat").child(currentUser.getUid());
        mChatsDatabaseRef.keepSynced(true);
        mUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabaseRef.keepSynced(true);

        return chatsView;
    }

    @Override
    public void onStart() {
        super.onStart();


        mChkChatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(currentUser.getUid())){
                    mChatList.setVisibility(View.VISIBLE);
                    mEmpityList.setVisibility(View.GONE);
                } else {
                    mChatList.setVisibility(View.GONE);
                    mEmpityList.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(mChatsDatabaseRef, Friends.class)
                .build();
        FirebaseRecyclerAdapter<Friends, ChatsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Friends, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Friends model) {

                final String userId = getRef(position).getKey();
               // mFriendChk = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUser.getUid());

                mUsersDatabaseRef.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        String profileImage = snapshot.child("image").getValue().toString();
                        final String profileName = snapshot.child("name").getValue().toString();
                        String chatOnline = snapshot.child("online").getValue().toString();

                        if (snapshot.hasChild("online")){
                            String userOnline = (String) snapshot.child("online").getValue().toString();
                            if (userOnline.equals("online")){
                                holder.userOnlineImg.setVisibility(View.VISIBLE);
                            } else {
                                holder.userOnlineImg.setVisibility(View.INVISIBLE);
                            }
                        }

                        holder.chatName.setText(profileName);
                        holder.chatOnline.setText(chatOnline);
                        Picasso.get().load(profileImage).placeholder(R.drawable.dp).into(holder.chatImage);

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent i = new Intent(getContext(), ChatActivity.class);
                                i.putExtra("user_id", userId);
                                i.putExtra("user_name", profileName);
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
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);
                ChatsViewHolder viewHolder = new ChatsViewHolder(view);
                return viewHolder;
            }
        };

        mChatList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        TextView chatName,chatOnline;
        CircleImageView chatImage;
        ImageView userOnlineImg;
        View mView;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            chatName = itemView.findViewById(R.id.user_single_name);
            chatOnline = itemView.findViewById(R.id.user_single_status);
            chatImage = itemView.findViewById(R.id.user_single_image);
            userOnlineImg = itemView.findViewById(R.id.online_img);

        }
    }

}
