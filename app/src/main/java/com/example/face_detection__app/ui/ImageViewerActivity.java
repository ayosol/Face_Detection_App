package com.example.face_detection__app.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.example.face_detection__app.R;
import com.example.face_detection__app.utils.base.BaseActivity;
import com.example.face_detection__app.utils.base.PublicMethods;

import static com.example.face_detection__app.utils.base.Constants.IMG_EXTRA_KEY;
import static com.example.face_detection__app.utils.base.Constants.IMG_FILE;


public class ImageViewerActivity extends BaseActivity {
    private static final String TAG = "MLKitTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "takePhoto: Taken to ImageViewerActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        if (getIntent().hasExtra(IMG_EXTRA_KEY)) {
            ImageView imageView = findViewById(R.id.image);
            String imagePath = getIntent().getStringExtra(IMG_EXTRA_KEY);
            imageView.setImageBitmap(PublicMethods.getBitmapByPath(imagePath, IMG_FILE));
        }
    }
}
