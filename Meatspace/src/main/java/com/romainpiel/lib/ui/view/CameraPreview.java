package com.romainpiel.lib.ui.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * This class assumes the parent layout is RelativeLayout.LayoutParams.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String CAMERA_PARAM_ORIENTATION = "orientation";
    private static final String CAMERA_PARAM_LANDSCAPE = "landscape";
    private static final String CAMERA_PARAM_PORTRAIT = "portrait";

    protected Context context;
    private SurfaceHolder holder;
    protected Camera camera;
    protected List<Camera.Size> previewSizeList;
    protected List<Camera.Size> pictureSizeList;
    protected Camera.Size previewSize;
    protected Camera.Size pictureSize;
    private int surfaceChangedCallDepth = 0;
    private int cameraId;
    private LayoutMode layoutMode;
    private int centerPosX = -1;
    private int centerPosY;

    /**
     * State flag: true when surface's layout size is set and surfaceChanged()
     * process has not been completed.
     */
    protected boolean mSurfaceConfiguring = false;

    PreviewReadyCallback mPreviewReadyCallback = null;

    public CameraPreview(Context context) {
        super(context);
        init(context, 0, LayoutMode.NoBlank);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, 0, LayoutMode.NoBlank);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, 0, LayoutMode.NoBlank);
    }

    private void init(Context context, int cameraId, LayoutMode mode) {
        this.context = context;
        layoutMode = mode;
        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            if (Camera.getNumberOfCameras() > cameraId) {
                this.cameraId = cameraId;
            } else {
                this.cameraId = 0;
            }
        } else {
            this.cameraId = 0;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            camera = Camera.open(this.cameraId);
        } else {
            camera = Camera.open();
        }
        Camera.Parameters cameraParams = camera.getParameters();
        previewSizeList = cameraParams.getSupportedPreviewSizes();
        pictureSizeList = cameraParams.getSupportedPictureSizes();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(this.holder);
        } catch (IOException e) {
            camera.release();
            camera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        surfaceChangedCallDepth++;
        doSurfaceChanged(width, height);
        surfaceChangedCallDepth--;
    }

    private void doSurfaceChanged(int width, int height) {
        camera.stopPreview();

        Camera.Parameters cameraParams = camera.getParameters();
        boolean portrait = isPortrait();

        // The code in this if-statement is prevented from executed again when surfaceChanged is
        // called again due to the change of the layout size in this if-statement.
        if (!mSurfaceConfiguring) {
            Camera.Size previewSize = determinePreviewSize(portrait, width, height);
            Camera.Size pictureSize = determinePictureSize(previewSize);

            this.previewSize = previewSize;
            this.pictureSize = pictureSize;
            mSurfaceConfiguring = adjustSurfaceLayoutSize(previewSize, portrait, width, height);
            // Continue executing this method if this method is called recursively.
            // Recursive call of surfaceChanged is very special case, which is a path from
            // the catch clause at the end of this method.
            // The later part of this method should be executed as well in the recursive
            // invocation of this method, because the layout change made in this recursive
            // call will not trigger another invocation of this method.
            if (mSurfaceConfiguring && (surfaceChangedCallDepth <= 1)) {
                return;
            }
        }

        configureCameraParameters(cameraParams, portrait);
        mSurfaceConfiguring = false;

        try {
            camera.startPreview();
        } catch (Exception e) {

            // Remove failed size
            previewSizeList.remove(previewSize);
            previewSize = null;

            // Reconfigure
            if (previewSizeList.size() > 0) { // prevent infinite loop
                surfaceChanged(null, 0, width, height);
            } else {
                Toast.makeText(context, "Can't start preview", Toast.LENGTH_LONG).show();
            }
        }

        if (null != mPreviewReadyCallback) {
            mPreviewReadyCallback.onPreviewReady();
        }
    }

    /**
     * @param portrait
     * @param reqWidth  must be the value of the parameter passed in surfaceChanged
     * @param reqHeight must be the value of the parameter passed in surfaceChanged
     * @return Camera.Size object that is an element of the list returned from Camera.Parameters.getSupportedPreviewSizes.
     */
    protected Camera.Size determinePreviewSize(boolean portrait, int reqWidth, int reqHeight) {
        // Meaning of width and height is switched for preview when portrait,
        // while it is the same as user's view for surface and metrics.
        // That is, width must always be larger than height for setPreviewSize.
        int reqPreviewWidth; // requested width in terms of camera hardware
        int reqPreviewHeight; // requested height in terms of camera hardware
        if (portrait) {
            reqPreviewWidth = reqHeight;
            reqPreviewHeight = reqWidth;
        } else {
            reqPreviewWidth = reqWidth;
            reqPreviewHeight = reqHeight;
        }

        // Adjust surface size with the closest aspect-ratio
        float reqRatio = ((float) reqPreviewWidth) / reqPreviewHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size retSize = null;
        for (Camera.Size size : previewSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }

        return retSize;
    }

    protected Camera.Size determinePictureSize(Camera.Size previewSize) {
        Camera.Size retSize = null;
        for (Camera.Size size : pictureSizeList) {
            if (size.equals(previewSize)) {
                return size;
            }
        }

        // if the preview size is not supported as a picture size
        float reqRatio = ((float) previewSize.width) / previewSize.height;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        for (Camera.Size size : pictureSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }

        return retSize;
    }

    protected boolean adjustSurfaceLayoutSize(Camera.Size previewSize, boolean portrait,
                                              int availableWidth, int availableHeight) {
        float tmpLayoutHeight, tmpLayoutWidth;
        if (portrait) {
            tmpLayoutHeight = previewSize.width;
            tmpLayoutWidth = previewSize.height;
        } else {
            tmpLayoutHeight = previewSize.height;
            tmpLayoutWidth = previewSize.width;
        }

        float factH, factW, fact;
        factH = availableHeight / tmpLayoutHeight;
        factW = availableWidth / tmpLayoutWidth;
        if (layoutMode == LayoutMode.FitToParent) {
            // Select smaller factor, because the surface cannot be set to the size larger than display metrics.
            if (factH < factW) {
                fact = factH;
            } else {
                fact = factW;
            }
        } else {
            if (factH < factW) {
                fact = factW;
            } else {
                fact = factH;
            }
        }

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.getLayoutParams();

        int layoutHeight = (int) (tmpLayoutHeight * fact);
        int layoutWidth = (int) (tmpLayoutWidth * fact);

        boolean layoutChanged;
        if ((layoutWidth != this.getWidth()) || (layoutHeight != this.getHeight())) {
            layoutParams.height = layoutHeight;
            layoutParams.width = layoutWidth;
            if (centerPosX >= 0) {
                layoutParams.topMargin = centerPosY - (layoutHeight / 2);
                layoutParams.leftMargin = centerPosX - (layoutWidth / 2);
            }
            this.setLayoutParams(layoutParams); // this will trigger another surfaceChanged invocation.
            layoutChanged = true;
        } else {
            layoutChanged = false;
        }

        return layoutChanged;
    }

    /**
     * @param x X coordinate of center position on the screen. Set to negative value to unset.
     * @param y Y coordinate of center position on the screen.
     */
    public void setCenterPosition(int x, int y) {
        centerPosX = x;
        centerPosY = y;
    }

    protected void configureCameraParameters(Camera.Parameters cameraParams, boolean portrait) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) { // for 2.1 and before
            if (portrait) {
                cameraParams.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_PORTRAIT);
            } else {
                cameraParams.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_LANDSCAPE);
            }
        } else { // for 2.2 and later
            int angle;
            Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
            switch (display.getRotation()) {
                case Surface.ROTATION_0: // This is display orientation
                    angle = 90; // This is camera orientation
                    break;
                case Surface.ROTATION_90:
                    angle = 0;
                    break;
                case Surface.ROTATION_180:
                    angle = 270;
                    break;
                case Surface.ROTATION_270:
                    angle = 180;
                    break;
                default:
                    angle = 90;
                    break;
            }
            camera.setDisplayOrientation(angle);
        }

        cameraParams.setPreviewSize(previewSize.width, previewSize.height);
        cameraParams.setPictureSize(pictureSize.width, pictureSize.height);

        camera.setParameters(cameraParams);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }

    public void stop() {
        if (null == camera) {
            return;
        }
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    public boolean isPortrait() {
        return (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    public void setOneShotPreviewCallback(PreviewCallback callback) {
        if (null == camera) {
            return;
        }
        camera.setOneShotPreviewCallback(callback);
    }

    public void setPreviewCallback(PreviewCallback callback) {
        if (null == camera) {
            return;
        }
        camera.setPreviewCallback(callback);
    }

    public Camera.Size getPreviewSize() {
        return previewSize;
    }

    public void setOnPreviewReady(PreviewReadyCallback cb) {
        mPreviewReadyCallback = cb;
    }

    public static enum LayoutMode {
        FitToParent, // Scale to the size that no side is larger than the parent
        NoBlank // Scale to the size that no side is smaller than the parent
    }

    public interface PreviewReadyCallback {
        public void onPreviewReady();
    }
}