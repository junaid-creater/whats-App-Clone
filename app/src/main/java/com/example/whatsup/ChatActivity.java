package com.example.whatsup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    public String mChatUser;
    private Toolbar mChatToolbar;

    private DatabaseReference mUserRef;
    private DatabaseReference mRootRef;
    private StorageReference mImageStorage;
    private FirebaseAuth mAuth;
    private String mCurrentUserID;

    private TextView mTitleName, mLastSeen;
    private CircleImageView mProfileImg;

    private ImageView mChatAddBtn, mChatSendBtn;
    private EditText mChatMsg;

    private RecyclerView mMessagesList;
    private final List<Messages> msgsList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;

    private MessageAdapter mAdapter;

    private String checker = "";
    private Uri fileUri;
    private static final int GALLERY_PICK = 1;

    private ProgressDialog mProgress;

    private String saveCurrentTime, saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mProgress = new ProgressDialog(this);

        mChatAddBtn = findViewById(R.id.chat_add_btn);
        mChatSendBtn  = findViewById(R.id.chat_send_btn);
        mChatMsg = findViewById(R.id.chat_message);
        mMessagesList = findViewById(R.id.messages_list);

        mAdapter = new MessageAdapter(msgsList, ChatActivity.this);

        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);

        mChatUser = getIntent().getStringExtra("user_id");
        String chatUserName = getIntent().getStringExtra("user_name");

        mUserRef = FirebaseDatabase.getInstance().getReference();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);
        mImageStorage = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        //Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());


        mChatToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        mTitleName = findViewById(R.id.custom_bar_title);
        mLastSeen = findViewById(R.id.custom_bar_seen);
        mProfileImg  =findViewById(R.id.custom_bar_image);
        mTitleName.setText(chatUserName);

        loadMessages();

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String image = snapshot.child("image").getValue().toString();
                if(snapshot.hasChild("online"))
                {
                    String online = snapshot.child("online").getValue().toString();
                    mLastSeen.setText(online);
                    String profileImage = snapshot.child("image").getValue().toString();
                    Picasso.get().load(profileImage).placeholder(R.drawable.dp).into(mProfileImg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.hasChild(mChatUser)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserID + "/" + mChatUser , chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserID , chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                            if (error != null){
                                Log.d("CHAT_LOG", error.getMessage().toString());
                            }

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();

            }
        });
        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence options[] = new CharSequence[]{"Images", "PDF Files", "MS Word Files"};

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select Options");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){

                            checker = "image";
                            Intent i = new Intent();
                            i.setType("image/*");
                            i.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(i, "SELECT IMAGE"), 438);

                        }
                        if (which == 1){

                            checker = "pdf";
                            Intent i = new Intent();
                            i.setType("application/pdf");
                            i.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(i, "SELECT PDF FILE"), 438);

                        }
                        if (which == 2){

                            checker = "docx";
                            Intent i = new Intent();
                            i.setType("application/msword");
                            i.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(i, "SELECT MS WORD FILE"), 438);

                        }
                    }
                });
                builder.show();

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        updateStatus("online");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateStatus("offline");
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {

                mProgress.setTitle("Sending File");
                mProgress.setMessage("Please weight while uploading finishes");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();

                fileUri = data.getData();

                if (!checker.equals("image"))
                {
                    final String current_user_ref = "messages/" + mCurrentUserID + "/" + mChatUser;
                    final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserID;

                    DatabaseReference user_message_push = mRootRef.child("messages")
                            .child(mCurrentUserID).child(mChatUser).push();
                    final String push_id = user_message_push.getKey();

                    final StorageReference filePath = mImageStorage.child("document_files").child(push_id + "." + checker);
                    filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if (task.isSuccessful()) {

                                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String link = uri.toString();

                                        Map messageMap = new HashMap();
                                        messageMap.put("message", link);
                                        messageMap.put("seen", false);
                                        messageMap.put("type", checker);
                                        messageMap.put("time", saveCurrentTime);
                                        messageMap.put("date", saveCurrentDate);
                                        messageMap.put("messageID", push_id);
                                        messageMap.put("from", mCurrentUserID);
                                        messageMap.put("to", mChatUser);

                                        Map message_user_map = new HashMap();
                                        message_user_map.put(current_user_ref + "/" + push_id, messageMap);
                                        message_user_map.put(chat_user_ref + "/" + push_id, messageMap);

                                        mRootRef.updateChildren(message_user_map, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                if (error != null) {
                                                    mProgress.dismiss();
                                                    Log.d("CHAT_LOG", error.getMessage().toString());
                                                } else {
                                                    mProgress.dismiss();
                                                    mChatMsg.setText("");
                                                }
                                            }
                                        });

                                    }
                                });

                            } else {
                                mProgress.dismiss();
                                Toast.makeText(ChatActivity.this, "Error in sending Image", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgress.dismiss();
                            Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            mProgress.setMessage((int) p + "% Uploading...");
                        }
                    });

                }
                else if (checker.equals("image"))
                {
                    final String current_user_ref = "messages/" + mCurrentUserID + "/" + mChatUser;
                    final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserID;

                    DatabaseReference user_message_push = mRootRef.child("messages")
                            .child(mCurrentUserID).child(mChatUser).push();
                    final String push_id = user_message_push.getKey();

                    final StorageReference filePath = mImageStorage.child("message_images").child(push_id + ".jpg");
                    filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if (task.isSuccessful()) {

                                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String link = uri.toString();

                                        Map messageMap = new HashMap();
                                        messageMap.put("message", link);
                                        messageMap.put("seen", false);
                                        messageMap.put("type", checker);
                                        messageMap.put("time", saveCurrentTime);
                                        messageMap.put("date", saveCurrentDate);
                                        messageMap.put("messageID", push_id);
                                        messageMap.put("from", mCurrentUserID);
                                        messageMap.put("to", mChatUser);

                                        Map message_user_map = new HashMap();
                                        message_user_map.put(current_user_ref + "/" + push_id, messageMap);
                                        message_user_map.put(chat_user_ref + "/" + push_id, messageMap);

                                        mRootRef.updateChildren(message_user_map, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                if (error != null) {
                                                    mProgress.dismiss();
                                                    Log.d("CHAT_LOG", error.getMessage().toString());
                                                } else {
                                                    mProgress.dismiss();
                                                    mChatMsg.setText("");
                                                }
                                            }
                                        });

                                    }
                                });

                            } else {
                                mProgress.dismiss();
                                Toast.makeText(ChatActivity.this, "Error in sending Image", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }

            }

    }



    public void loadMessages()
    {
        mRootRef.child("messages").child(mCurrentUserID).child(mChatUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages message = snapshot.getValue(Messages.class);
                msgsList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(msgsList.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void deleteMsg(int position)
    {
        msgsList.remove(position);
        mAdapter.notifyDataSetChanged();
    }

    private void sendMessage(){

        String message = mChatMsg.getText().toString();
        if(!TextUtils.isEmpty(message)){
            String current_user_ref = "messages/" + mCurrentUserID + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserID;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserID).child(mChatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", saveCurrentTime);
            messageMap.put("date", saveCurrentDate);
            messageMap.put("messageID", push_id);
            messageMap.put("from", mCurrentUserID);
            messageMap.put("to", mChatUser);

            Map message_user_map = new HashMap();
            message_user_map.put(current_user_ref + "/" + push_id, messageMap);
            message_user_map.put(chat_user_ref + "/" + push_id, messageMap);

            mRootRef.updateChildren(message_user_map, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    if (error != null){
                        Log.d("CHAT_LOG", error.getMessage().toString());
                    }
                    else {
                        mChatMsg.setText("");
                    }
                }
            });
        }
    }

    private void updateStatus(String state)
    {
        HashMap<String, Object> onlineState = new HashMap<>();
        onlineState.put("online", state);

        //currentUserID = mAuth.getCurrentUser().getUid();
        mUserRef.child("Users").child(mCurrentUserID).updateChildren(onlineState);
    }

}
