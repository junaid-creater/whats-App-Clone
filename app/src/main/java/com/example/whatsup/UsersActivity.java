 package com.example.whatsup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabase;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mUsersList = findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        mToolbar = findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Loading All Users");
        mProgress.setMessage("Weight while we load All Users Data");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

    }

    @Override
    protected void onStart() {
        super.onStart();
            if (isConnected()){
                FirebaseRecyclerOptions<Users> options =
                        new FirebaseRecyclerOptions.Builder<Users>()
                                .setQuery(mUsersDatabase, Users.class)
                                .build();

                FirebaseRecyclerAdapter<Users, UsersViewHolder> adapter =
                        new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model)
                            {
                                holder.userName.setText(model.getName());
                                holder.userStatus.setText(model.getStatus());
                                Picasso.get().load(model.getImage()).placeholder(R.drawable.dp).into(holder.profileImage);

                                mProgress.dismiss();

                                final String user_id = getRef(position).getKey();

                                holder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = new Intent(UsersActivity.this,ProfileActivity.class);
                                        i.putExtra("user_id",user_id);
                                        startActivity(i);
                                    }
                                });
                            }

                            @NonNull
                            @Override
                            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout,parent,false);
                                UsersViewHolder viewHolder = new UsersViewHolder(view);
                                return viewHolder;
                            }
                        };
                mUsersList.setAdapter(adapter);

                adapter.startListening();

            } else{
                mProgress.dismiss();
                Toast.makeText(UsersActivity.this,"No Connection", Toast.LENGTH_LONG).show();
            }

    }

    public boolean isConnected(){
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        }catch (Exception e){
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;
        View mView;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            userName = itemView.findViewById(R.id.user_single_name);
            userStatus = itemView.findViewById(R.id.user_single_status);
            profileImage = itemView.findViewById(R.id.user_single_image);

        }
    }

}
