package com.romainpiel.lib.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;

import com.romainpiel.Constants;
import com.romainpiel.lib.gif.AnimatedGifEncoder;

import java.io.ByteArrayOutputStream;

/**
 * MeatspaceProject
 * User: romainpiel
 * Date: 10/11/2013
 * Time: 17:03
 */
public class PreviewHelper implements Camera.PreviewCallback {

    private Handler uiHandler;
    private int angle;
    private long lastTick;
    private int delay;
    private boolean capturing;
    private AnimatedGifEncoder gifEncoder;
    private Runnable stopCaptureRunnable;
    private ByteArrayOutputStream gifStream;
    private OnCaptureListener onCaptureListener;

    public PreviewHelper(Handler uiHandler) {
        this.uiHandler = uiHandler;

        this.stopCaptureRunnable = new Runnable() {
            @Override
            public void run() {

                if (delay != -1) {
                    gifEncoder.setDelay(delay);
                }
                gifEncoder.finish();

                if (onCaptureListener != null) {
                    onCaptureListener.onCaptureComplete(gifStream.toByteArray());
                }

                prepareForNextCapture();
            }
        };

        this.gifEncoder = new AnimatedGifEncoder();
        this.gifEncoder.setRepeat(0);

        prepareForNextCapture();
    }

    public boolean isCapturing() {
        return capturing;
    }

    public OnCaptureListener getOnCaptureListener() {
        return onCaptureListener;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public void setOnCaptureListener(OnCaptureListener onCaptureListener) {
        this.onCaptureListener = onCaptureListener;
    }

    private void prepareForNextCapture() {
        this.capturing = false;
        this.lastTick = -1;
        this.delay = -1;
        this.gifStream = null;
    }

    public void capture() {
        if (!capturing) {


            gifStream = new ByteArrayOutputStream();
            gifEncoder.start(gifStream);

            uiHandler.postDelayed(stopCaptureRunnable, Constants.CAPTURE_DURATION);

            capturing = true;

            if (onCaptureListener != null) {
                onCaptureListener.onCaptureStarted();
            }
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (capturing) {

            long now = System.currentTimeMillis();

            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            YuvImage image = new YuvImage(data, parameters.getPreviewFormat(),
                    size.width, size.height, null);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 90, output);

            Bitmap bitmap = BitmapFactory.decodeByteArray(output.toByteArray(), 0, output.size());

            boolean realSized = angle % 180 == 0;
            float realW = realSized? image.getWidth() : image.getHeight();
            float realH = realSized? image.getHeight() : image.getWidth();

            Matrix matrix = new Matrix();
            matrix.postRotate(-angle);

            if (realW > realH) {
                matrix.postScale(Constants.CAPTURE_WIDTH / realW, Constants.CAPTURE_WIDTH / realW);
            } else {
                matrix.postScale(Constants.CAPTURE_HEIGHT / realH, Constants.CAPTURE_HEIGHT / realH);
            }

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    image.getWidth(), image.getHeight(), matrix, true);

            gifEncoder.addFrame(rotatedBitmap);

            rotatedBitmap.recycle();
            bitmap.recycle();

            if (lastTick != -1 && delay == -1) {
                delay = (int) (now - lastTick);
            }

            lastTick = now;
        }
    }

    public interface OnCaptureListener {
        public void onCaptureStarted();
        public void onCaptureComplete(byte[] gifData);
    }
}
