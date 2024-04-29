package it.dhd.oxygencustomizer.xposed.utils.viewpager;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;

public class TabletTransformer extends ABaseTransformer {
    private static final Camera OFFSET_CAMERA = new Camera();
    private static final Matrix OFFSET_MATRIX = new Matrix();
    private static final float[] OFFSET_TEMP_FLOAT = new float[2];

    protected void onTransform(View view, float position) {
        float rotation = (position < 0.0f ? 30.0f : -30.0f) * Math.abs(position);
        view.setTranslationX(getOffsetXForRotation(rotation, view.getWidth(), view.getHeight()));
        view.setPivotX(((float) view.getWidth()) * 0.5f);
        view.setPivotY(0.0f);
        view.setRotationY(rotation);
    }

    protected static final float getOffsetXForRotation(float degrees, int width, int height) {
        OFFSET_MATRIX.reset();
        OFFSET_CAMERA.save();
        OFFSET_CAMERA.rotateY(Math.abs(degrees));
        OFFSET_CAMERA.getMatrix(OFFSET_MATRIX);
        OFFSET_CAMERA.restore();
        OFFSET_MATRIX.preTranslate(((float) (-width)) * 0.5f, ((float) (-height)) * 0.5f);
        OFFSET_MATRIX.postTranslate(((float) width) * 0.5f, ((float) height) * 0.5f);
        OFFSET_TEMP_FLOAT[0] = (float) width;
        OFFSET_TEMP_FLOAT[1] = (float) height;
        OFFSET_MATRIX.mapPoints(OFFSET_TEMP_FLOAT);
        return (degrees > 0.0f ? 1.0f : -1.0f) * (((float) width) - OFFSET_TEMP_FLOAT[0]);
    }
}
