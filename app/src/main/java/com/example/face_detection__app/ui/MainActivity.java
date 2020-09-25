package com.example.face_detection__app.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;

import com.example.face_detection__app.R;
import com.example.face_detection__app.utils.base.BaseActivity;
import com.example.face_detection__app.utils.base.Constants;
import com.example.face_detection__app.utils.base.PublicMethods;
import com.example.face_detection__app.utils.common.CameraSource;
import com.example.face_detection__app.utils.common.CameraSourcePreview;
import com.example.face_detection__app.utils.common.FrameMetadata;
import com.example.face_detection__app.utils.common.GraphicOverlay;
import com.example.face_detection__app.utils.interfaces.FaceDetectStatus;
import com.example.face_detection__app.utils.interfaces.FrameReturn;
import com.example.face_detection__app.utils.models.RectModel;
import com.example.face_detection__app.utils.visions.FaceDetectionProcessor;
import com.google.android.gms.common.annotation.KeepName;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.hsalf.smilerating.BaseRating;
import com.hsalf.smilerating.SmileRating;

import java.io.IOException;

import static com.example.face_detection__app.utils.base.Constants.IMG_EXTRA_KEY;

@KeepName
public final class MainActivity extends BaseActivity
        implements OnRequestPermissionsResultCallback, FrameReturn, FaceDetectStatus {
    private static final String FACE_DETECTION = "Face Detection";
    private static final String TAG = "MLKitTAG";

    Bitmap originalImage = null;
    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    //private ImageView faceFrame;
    private ImageView imageView;
    private Button takePhoto;
    private SmileRating smile_rating;
    private Bitmap croppedImage = null;
    private RectModel rectModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image_captured);
        preview = findViewById(R.id.firePreview);
        takePhoto = findViewById(R.id.takePhoto);
        //faceFrame = findViewById(R.id.faceFrame);
        graphicOverlay = findViewById(R.id.fireFaceOverlay);
        smile_rating = findViewById(R.id.smile_rating);

        if (PublicMethods.allPermissionsGranted(this)) {
            createCameraSource();
        } else {
            PublicMethods.getRuntimePermissions(this);
        }

        takePhoto.setOnClickListener(v -> takePhoto());
    }


    private void createCameraSource() {
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }
        try {
            FaceDetectionProcessor processor = new FaceDetectionProcessor(getResources());
            processor.frameHandler = this;
            processor.faceDetectStatus = this;
            cameraSource.setMachineLearningFrameProcessor(processor);
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: " + FACE_DETECTION, e);
            Toast.makeText(
                    getApplicationContext(),
                    "Can not create image processor: " + e.getMessage(),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }


    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PublicMethods.allPermissionsGranted(this)) {
            createCameraSource();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //calls with each frame includes by face
    @Override
    public void onFrame(Bitmap image, FirebaseVisionFace face, FrameMetadata frameMetadata, GraphicOverlay graphicOverlay) {
        originalImage = image;
        if (face.getLeftEyeOpenProbability() < 0.4) {
            findViewById(R.id.rightEyeStatus).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.rightEyeStatus).setVisibility(View.INVISIBLE);
        }
        if (face.getRightEyeOpenProbability() < 0.4) {
            findViewById(R.id.leftEyeStatus).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.leftEyeStatus).setVisibility(View.INVISIBLE);
        }

        int smile = 0;

        if (face.getSmilingProbability() > .8) {
            smile = BaseRating.GREAT;
        } else if (face.getSmilingProbability() <= .8 && face.getSmilingProbability() > .6) {
            smile = BaseRating.GOOD;
        } else if (face.getSmilingProbability() <= .6 && face.getSmilingProbability() > .4) {
            smile = BaseRating.OKAY;
        } else if (face.getSmilingProbability() <= .4 && face.getSmilingProbability() > .2) {
            smile = BaseRating.BAD;
        }
        smile_rating.setSelectedSmile(smile, true);

    }

    @Override
    public void onFaceLocated(RectModel rectModel) {
        Toast.makeText(getApplicationContext(), "Face Detected", Toast.LENGTH_SHORT).show();
        this.rectModel = rectModel;
        //faceFrame.setColorFilter(ContextCompat.getColor(this, R.color.green));
        takePhoto.setEnabled(true);

        float left = (float) (originalImage.getWidth() * 0.2);
        float newWidth = (float) (originalImage.getWidth() * 0.6);

        float top = (float) (originalImage.getHeight() * 0.2);
        float newHeight = (float) (originalImage.getHeight() * 0.6);
        croppedImage =
                Bitmap.createBitmap(originalImage,
                        ((int) (left)),
                        (int) (top),
                        ((int) (newWidth)),
                        (int) (newHeight));
        imageView.setImageBitmap(croppedImage);
    }

    private void takePhoto() {
        Log.i(TAG, "takePhoto: Clicked");
        if (croppedImage != null) {
            Log.i(TAG, "takePhoto: if part working");
            String path = PublicMethods.saveToInternalStorage(croppedImage, Constants.IMG_FILE, mActivity);
            startActivity(new Intent(MainActivity.this, ImageViewerActivity.class)
                    .putExtra(IMG_EXTRA_KEY, path));
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "Something Went wrong. Image not taken",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onFaceNotLocated() {
        //faceFrame.setColorFilter(ContextCompat.getColor(this, R.color.blue_dark));
        takePhoto.setEnabled(false);
        //Toast.makeText(getApplicationContext(), "No Face Detected", Toast.LENGTH_SHORT).show();
    }
}
