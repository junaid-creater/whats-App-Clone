package com.example.whatsup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.icu.text.DateFormat;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView mProfileImg;
    private TextView mProfileName, mProfileStatus;
    private Button mProfileReqBtn, mProfileDeclBtn;

    private ProgressDialog mProgress;

    private DatabaseReference mUserDatabase;
    private DatabaseReference mFrndReqDatabase;
    private DatabaseReference mFrndsDatabase;
    private DatabaseReference mChatRef;
    private DatabaseReference mNotificationDatabase;

    private FirebaseUser mCurrent_user;

    String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();
        final String user_id = getIntent().getStringExtra("user_id");

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFrndReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFrndsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mChatRef = FirebaseDatabase.getInstance().getReference().child("Chat");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");


        mProfileImg = findViewById(R.id.profile_image);
        mProfileName = findViewById(R.id.profile_displayName);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileReqBtn = findViewById(R.id.profile_send_req_btn);
        mProfileDeclBtn = findViewById(R.id.profile_decl_req_btn);

        mCurrent_state = "not_friends";

        mProfileDeclBtn.setVisibility(View.INVISIBLE);
        mProfileDeclBtn.setEnabled(false);

        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Loading User Data");
        mProgress.setMessage("Weight while we load the User Data.");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.dp).into(mProfileImg);

                //----------------Request Feature-------------------

                mFrndReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)){
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (req_type.equals("recieved")){

                                mProfileReqBtn.setText("accept friend request");
                                mCurrent_state = "req_recieved";

                                mProfileDeclBtn.setVisibility(View.VISIBLE);
                                mProfileDeclBtn.setEnabled(true);

                            } else if (req_type.equals("sent")){

                                mProfileReqBtn.setText("cancel friend request");
                                mCurrent_state = "req_sent";

                                mProfileDeclBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclBtn.setEnabled(false);

                            }
                            mProgress.dismiss();

                        } else {

                            mFrndsDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_id)){
                                        mCurrent_state = "friends";
                                        mProfileReqBtn.setText("Unfriend");

                                        mProfileDeclBtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclBtn.setEnabled(false);
                                    }
                                    mProgress.dismiss();
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProgress.dismiss();
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        if(user_id.equals(mCurrent_user.getUid())){

            mProfileReqBtn.setVisibility(View.INVISIBLE);
            mProfileReqBtn.setEnabled(false);
            mProfileDeclBtn.setVisibility(View.INVISIBLE);
            mProfileDeclBtn.setEnabled(false);

        }

        mProfileReqBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                mProfileReqBtn.setEnabled(false);

                //-----------------when not Friends-------------------

                if (mCurrent_state.equals("not_friends")){
                    mFrndReqDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                      mFrndReqDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type")
                                              .setValue("recieved").addOnSuccessListener(new OnSuccessListener<Void>() {
                                          @Override
                                          public void onSuccess(Void aVoid) {

                                              HashMap<String , String> notificationData = new HashMap<>();
                                              notificationData.put("from", mCurrent_user.getUid());
                                              notificationData.put("type", "request");

                                              mNotificationDatabase.child(user_id).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                  @Override
                                                  public void onSuccess(Void aVoid) {
                                                      mProfileReqBtn.setText("Cancel friend request");
                                                      mCurrent_state = "req_sent";
                                                      mProfileDeclBtn.setVisibility(View.INVISIBLE);
                                                      mProfileDeclBtn.setEnabled(false);
                                                      Toast.makeText(ProfileActivity.this,"Request Sent Successfully", Toast.LENGTH_SHORT).show();
                                                  }
                                              }).addOnFailureListener(new OnFailureListener() {
                                                  @Override
                                                  public void onFailure(@NonNull Exception e) {
                                                      Toast.makeText(ProfileActivity.this,"Request not Sent", Toast.LENGTH_SHORT).show();
                                                  }
                                              });
                                          }
                                      });

                                    } else{
                                        Toast.makeText(ProfileActivity.this,"Failed Sending Request", Toast.LENGTH_SHORT).show();
                                    }
                                    mProfileReqBtn.setEnabled(true);
                                }
                            });
                }

                //---------------cancel request State------------------

                if (mCurrent_state.equals("req_sent")){

                    mFrndReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFrndReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileReqBtn.setEnabled(true);
                                    mProfileReqBtn.setText("send friend request");
                                    mCurrent_state = "not_friends";

                                    mProfileDeclBtn.setVisibility(View.INVISIBLE);
                                    mProfileDeclBtn.setEnabled(false);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mProfileReqBtn.setEnabled(true);
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProfileReqBtn.setEnabled(true);
                        }
                    });

                }

                //-------------Request Recieved State----------------

                if (mCurrent_state.equals("req_recieved")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    mFrndsDatabase.child(mCurrent_user.getUid()).child(user_id).child("date").setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFrndsDatabase.child(user_id).child(mCurrent_user.getUid()).child("date").setValue(currentDate)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mFrndReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            mFrndReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    mProfileReqBtn.setEnabled(true);
                                                                    mProfileReqBtn.setText("Unfriend");
                                                                    mCurrent_state = "friends";

                                                                    mProfileDeclBtn.setVisibility(View.INVISIBLE);
                                                                    mProfileDeclBtn.setEnabled(false);

                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                }
                            });
                }

                //---------------------Unfriend State-----------------------------

                if (mCurrent_state.equals("friends")){

                    mFrndsDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFrndsDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileReqBtn.setEnabled(true);
                                    mProfileReqBtn.setText("send friend request");
                                    mCurrent_state = "not_friends";

                                    mProfileDeclBtn.setVisibility(View.INVISIBLE);
                                    mProfileDeclBtn.setEnabled(false);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mProfileReqBtn.setEnabled(true);
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProfileReqBtn.setEnabled(true);
                        }
                    });
                }
            }
        });
    }

}
