package com.example.face_detection__app.utils.interfaces;

import android.graphics.Bitmap;

import com.example.face_detection__app.utils.common.FrameMetadata;
import com.example.face_detection__app.utils.common.GraphicOverlay;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;


public interface FrameReturn{
    void onFrame(
            Bitmap image,
            FirebaseVisionFace face,
            FrameMetadata frameMetadata,
            GraphicOverlay graphicOverlay
    );
}