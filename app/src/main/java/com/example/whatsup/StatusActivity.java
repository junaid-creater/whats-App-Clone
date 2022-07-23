package com.example.whatsup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.InetAddress;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog mProgress;

    private TextInputLayout mStatus;
    private Button mSaveBtn;

    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar = findViewById(R.id.status_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();
        mStatusDatabase  = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        mStatus = findViewById(R.id.status_input);
        mSaveBtn = findViewById(R.id.status_save_btn);

        String status_value = getIntent().getStringExtra("status_value");
        mStatus.getEditText().setText(status_value);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress = new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("please weight while we save the changes!");
                mProgress.show();
                mProgress.setCanceledOnTouchOutside(false);

                if (isConnected()){
                   // if(isConnection()){

                        String status = mStatus.getEditText().getText().toString();
                        mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    mProgress.dismiss();
                                    Toast.makeText(getApplicationContext(),"Status Updated Successfully",Toast.LENGTH_SHORT).show();
                                } else {
                                    mProgress.hide();
                                    Toast.makeText(getApplicationContext(),"There was some error in saving changes!",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

//                    } else {
//                        mProgress.dismiss();
//                        Toast.makeText(getApplicationContext(),"NO Internet",Toast.LENGTH_LONG).show();
//                    }

                } else {
                    mProgress.dismiss();
                    Toast.makeText(getApplicationContext(),"NO Connection",Toast.LENGTH_LONG).show();
                }
            }
        });
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

//    public boolean isConnection(){
//        try {
//            InetAddress address = InetAddress.getByName("www.google.com");
//            return true;
//        }catch (Exception e){
//            return false;
//        }
//    }
}
