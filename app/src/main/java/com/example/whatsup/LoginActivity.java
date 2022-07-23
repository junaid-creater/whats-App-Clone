package com.example.whatsup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.net.InetAddress;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout mLoginEmail;
    private TextInputLayout mLoginPassword;
    private Button mLoginBtn;

    private Toolbar mToolbar;
    private ProgressDialog mProgress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mProgress = new ProgressDialog(this);
        mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLoginBtn = findViewById(R.id.login_btn);
        mLoginEmail = findViewById(R.id.login_email);
        mLoginPassword = findViewById(R.id.login_password);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.setTitle("Logging In");
                mProgress.setMessage("Please weight while we check your credentials");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();
                if(isConnected()){
                   // if(isConnection()){

                        String email = mLoginEmail.getEditText().getText().toString();
                        String password = mLoginPassword.getEditText().getText().toString();
                        if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
                            login_User(email,password);
                        }

//                    } else {
//                        mProgress.dismiss();
//                        Toast.makeText(LoginActivity.this,"No Internet", Toast.LENGTH_LONG).show();
//                    }

                } else {
                    mProgress.dismiss();
                    Toast.makeText(LoginActivity.this,"No Connection", Toast.LENGTH_LONG).show();
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
//            InetAddress address = InetAddress.getByName("google.com");
//            return !address.equals("");
//        }catch (Exception e){
//            return false;
//        }
//    }

    private void login_User(String email, String password) {

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful())
                {
                    mProgress.dismiss();
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                }
                else
                {
                    mProgress.hide();
                    Toast.makeText(LoginActivity.this,"Cannot Sign In. Please check the form and try again.", Toast.LENGTH_LONG).show();
                }

            }
        });

    }
}
