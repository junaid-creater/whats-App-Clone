package com.example.whatsup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    private StorageReference mImageStorage;

    private CircleImageView mImage;
    private TextView mName, mStatus;
    private Button mStatusBtn;
    private Button mChangeImgBtn;

    private ProgressDialog mProgress;

    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mImage = findViewById(R.id.user_single_image);
        mName = findViewById(R.id.setting_display_name);
        mStatus = findViewById(R.id.settings_status);
        mStatusBtn = findViewById(R.id.settings_status_btn);
        mChangeImgBtn = findViewById(R.id.settings_image_btn);

        mImageStorage = FirebaseStorage.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue().toString();
                final String image = snapshot.child("image").getValue().toString();
                String status = snapshot.child("status").getValue().toString();
                String thumb_image = snapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);
                if (!image.equals("default")) {
                   // Picasso.get().load(image).placeholder(R.drawable.dp).into(mImage);
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.dp).into(mImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.dp).into(mImage);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status_value = mStatus.getText().toString();
                Intent i = new Intent(SettingsActivity.this,StatusActivity.class);
                i.putExtra("status_value", status_value);
                startActivity(i);

            }
        });

        mChangeImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(i,"SELECT IMAGE"), GALLERY_PICK);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK){

                mProgress = new ProgressDialog(SettingsActivity.this);
                mProgress.setTitle("Uploading Image...");
                mProgress.setMessage("Weight while we Upload and Process the image");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();


                Uri resultUri = result.getUri();

                String current_user_id = mCurrentUser.getUid();

                final StorageReference filePath = mImageStorage.child("profile_images").child(current_user_id + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String link = uri.toString();

                                    mUserDatabase.child("image").setValue(link).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                mProgress.dismiss();
                                                Toast.makeText(SettingsActivity.this,"working", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mProgress.dismiss();
                                    Toast.makeText(SettingsActivity.this,"Error in Uploading Image", Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            mProgress.dismiss();
                            Toast.makeText(SettingsActivity.this,"not working", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){

                Exception error = result.getError();

            }

        }

    }
}
