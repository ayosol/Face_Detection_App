package com.example.face_detection__app.utils.interfaces;

import com.example.face_detection__app.utils.models.RectModel;

public interface FaceDetectStatus {
    void onFaceLocated(RectModel rectModel);

    void onFaceNotLocated();
}
