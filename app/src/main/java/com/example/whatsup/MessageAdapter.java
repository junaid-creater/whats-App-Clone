package com.example.whatsup;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private Context mContext;

    public MessageAdapter(List<Messages> mMessageList, Context context) {
        this.mMessageList = mMessageList;
        this.mContext = context;
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);

        return new MessageViewHolder(view);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        Messages c = mMessageList.get(position);
        String from_user = c.getFrom();
        final String message_type = c.getType();

        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        String mCurrentUserID = mAuth.getCurrentUser().getUid().toString();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String image = snapshot.child("image").getValue().toString();
                Picasso.get().load(image).placeholder(R.drawable.dp).into(holder.recieverSingleImg);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.recieverMsgText.setVisibility(View.GONE);
        holder.recieverSingleImg.setVisibility(View.GONE);
        holder.senderMsgText.setVisibility(View.GONE);
        holder.senderImgMsg.setVisibility(View.GONE);
        holder.recieverImgMsg.setVisibility(View.GONE);
        holder.recieverDate.setVisibility(View.GONE);
        holder.senderDate.setVisibility(View.GONE);


        if(from_user != null)
        {
            if (message_type.equals("text"))
            {
                if (from_user.equals(mCurrentUserID))
                {
                    holder.senderMsgText.setVisibility(View.VISIBLE);

                    holder.senderMsgText.setText(c.getMessage() + "\n\n" + c.getTime() + " - " + c.getDate());
                }
                else
                {
                    holder.recieverMsgText.setVisibility(View.VISIBLE);
                    holder.recieverSingleImg.setVisibility(View.VISIBLE);

                    holder.recieverMsgText.setText(c.getMessage() + "\n\n" + c.getTime() + " - " + c.getDate());
                }
            }
            else if (message_type.equals("image"))
            {
                if (from_user.equals(mCurrentUserID))
                {
                    holder.senderImgMsg.setVisibility(View.VISIBLE);
                    holder.senderDate.setVisibility(View.VISIBLE);

                    Picasso.get().load(c.getMessage()).placeholder(R.drawable.dp).into(holder.senderImgMsg);
                    holder.senderDate.setText(c.getTime() + " - " + c.getDate());
                }
                else
                {
                    holder.recieverImgMsg.setVisibility(View.VISIBLE);
                    holder.recieverSingleImg.setVisibility(View.VISIBLE);
                    holder.recieverDate.setVisibility(View.VISIBLE);

                    Picasso.get().load(c.getMessage()).placeholder(R.drawable.dp).into(holder.recieverImgMsg);
                    holder.recieverDate.setText(c.getTime() + " - " + c.getDate());
                }
            }
            else if (message_type.equals("pdf") || message_type.equals("docx"))
            {
                if (from_user.equals(mCurrentUserID)){
                    holder.senderImgMsg.setVisibility(View.VISIBLE);
                    holder.senderDate.setVisibility(View.VISIBLE);

                    Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whats-up-f41cb.appspot.com/o/message_images%2Fupload_file.png?alt=media&token=d8e7601e-6ee7-46e5-86d7-6ca41e56f78d")
                            .placeholder(R.drawable.upload_file).into(holder.senderImgMsg);
                    holder.senderDate.setText(c.getTime() + " - " + c.getDate());
                }
                else
                {
                    holder.recieverImgMsg.setVisibility(View.VISIBLE);
                    holder.recieverSingleImg.setVisibility(View.VISIBLE);
                    holder.recieverDate.setVisibility(View.VISIBLE);

                    Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whats-up-f41cb.appspot.com/o/message_images%2Fdownload_file.png?alt=media&token=6dd1cfcd-2b7a-4922-bf0f-9a91f3c8574d")
                            .placeholder(R.drawable.download_file).into(holder.recieverImgMsg);
                    holder.recieverDate.setText(c.getTime() + " - " + c.getDate());;
                }
            }
        }


        if (from_user.equals(mCurrentUserID))
        {

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMessageList.get(position).getType().equals("pdf") || mMessageList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Download and view this Document",
                                        "Delete for Me",
                                        "Delete for Everyone",
                                        "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Select Options");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(mMessageList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(i);
                                }
                                else if (which == 1){
                                    deleteSentMsg(position, holder);
                                }
                                else if (which == 2){
                                    deleteMsgForEveryone(position, holder);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (mMessageList.get(position).getType().equals("text"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for Me",
                                        "Delete for Everyone",
                                        "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Select Options");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSentMsg(position, holder);
                                }
                                else if (which == 1){
                                    deleteMsgForEveryone(position, holder);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (mMessageList.get(position).getType().equals("image"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "View Image",
                                        "Delete for Me",
                                        "Delete for Everyone",
                                        "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Select Options");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    Intent i = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    i.putExtra("url", mMessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(i);
                                }
                                else if (which == 1){
                                    deleteSentMsg(position, holder);
                                }
                                else if (which == 2){
                                    deleteMsgForEveryone(position, holder);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });

        }
        else
        {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMessageList.get(position).getType().equals("pdf") || mMessageList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Download and view this Document",
                                        "Delete for Me",
                                        "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Select Options");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){

                                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(mMessageList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(i);

                                }
                                else if (which == 1){
                                    deleteRecievedMsg(position, holder);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (mMessageList.get(position).getType().equals("text"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for Me",
                                        "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Select Options");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteRecievedMsg(position, holder);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (mMessageList.get(position).getType().equals("image"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "View Image",
                                        "Delete for Me",
                                        "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Select Options");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    Intent i = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    i.putExtra("url", mMessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(i);
                                }
                                else if (which == 1){
                                    deleteRecievedMsg(position, holder);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView recieverMsgText, senderMsgText, recieverDate, senderDate;
        public CircleImageView recieverSingleImg;
        public ImageView senderImgMsg, recieverImgMsg;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            recieverSingleImg =  itemView.findViewById(R.id.reciever_single_image);
            recieverMsgText = itemView.findViewById(R.id.reciever_msg_text);
            senderMsgText = itemView.findViewById(R.id.sender_msg_text);
            senderImgMsg = itemView.findViewById(R.id.sender_img_mesg);
            recieverImgMsg = itemView.findViewById(R.id.reciever_img_mesg);
            recieverDate = itemView.findViewById(R.id.reciever_date);
            senderDate = itemView.findViewById(R.id.sender_date);

        }
    }

    private void deleteSentMsg(final int position, final MessageViewHolder holder)
    {

        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.child("messages")
                .child(mMessageList.get(position).getFrom())
                .child(mMessageList.get(position).getTo())
                .child(mMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    if (mContext instanceof ChatActivity){
                        ((ChatActivity)mContext).deleteMsg(position);
                    }
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(holder.itemView.getContext(), "Error Occured", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteRecievedMsg(final int position, final MessageViewHolder holder)
    {

        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.child("messages")
                .child(mMessageList.get(position).getTo())
                .child(mMessageList.get(position).getFrom())
                .child(mMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    if (mContext instanceof ChatActivity){
                        ((ChatActivity)mContext).deleteMsg(position);
                    }
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(holder.itemView.getContext(), "Error Occured", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteMsgForEveryone(final int position, final MessageViewHolder holder)
    {
            final DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
            mRootRef.child("messages")
                    .child(mMessageList.get(position).getTo())
                    .child(mMessageList.get(position).getFrom())
                    .child(mMessageList.get(position).getMessageID())
                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mRootRef.child("messages")
                                .child(mMessageList.get(position).getFrom())
                                .child(mMessageList.get(position).getTo())
                                .child(mMessageList.get(position).getMessageID())
                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    if (mContext instanceof ChatActivity){
                                        ((ChatActivity)mContext).deleteMsg(position);
                                    }
                                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
    }



}
