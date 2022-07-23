package com.example.whatsup;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {

    private ImageView mImage;
    private String imgUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        mImage = findViewById(R.id.img_viewer);

        imgUrl = getIntent().getStringExtra("url");

        Picasso.get().load(imgUrl).into(mImage);

    }
}
