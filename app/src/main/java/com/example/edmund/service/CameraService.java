package com.example.edmund.service;

import android.app.Service;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceView;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.widget.Toast;

import com.example.edmund.business.CameraWindow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by edmund on 2016/2/19.
 */
public class CameraService extends Service implements Camera.PictureCallback {

    private static final String TAG = "CameraService";
    private boolean isRunning; // 是否已在监控拍照
    private Camera mCamera;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startTakePicture(intent);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        releaseCamera();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startTakePicture(Intent intent) {
        if(!isRunning){
            isRunning = true;
            SurfaceView preview = CameraWindow.getDummyCameraView();
            mCamera = getFacingBackCamera();
            if (mCamera == null) {
                Log.w(TAG, "getFacingFrontCamera return null");
                stopSelf();
                return;
            }
            try {
                mCamera.setPreviewDisplay(preview.getHolder());
                mCamera.startPreview();// 开始预览
                Camera.Parameters mParameters = mCamera.getParameters();
                List<Size> list = mParameters.getSupportedPictureSizes();
                int width = 800;
                int heigh = 480;
                for (Size item :
                        list) {
                    if (item.width > 1000 && item.width < 2000) {
                        width = item.width;
                        heigh = item.height;
                        break;
                    }
                }
                mParameters.setPictureSize(width, heigh);
                mCamera.setParameters(mParameters);
                // 防止某些手机拍摄的照片亮度不够
                Thread.sleep(200);

                mCamera.takePicture(null, null, this);
            } catch (Exception e) {
                e.printStackTrace();
                releaseCamera();
                stopSelf();
            }
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            Log.d(TAG, "releaseCamera...");
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera getFacingFrontCamera() {
        CameraInfo cameraInfo = new CameraInfo();
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    return Camera.open(i);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        return null;
    }

    private Camera getFacingBackCamera() {
        CameraInfo cameraInfo = new CameraInfo();
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                try {
                    return Camera.open(i);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG,e.getMessage());
                }
            }
        }
        return null;
    }

    //==========================分割线==implements========================================
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        releaseCamera();
        File pictureFileDir = getDir();
        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
            Log.d(TAG, "Can't create directory to save image.");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
        String photoFile = "Picture_" + date + ".jpg";

        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        File pictureFile = new File(filename);
        System.out.println("filename is " + filename);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.flush();
            fos.close();
            Toast.makeText(getApplicationContext(), "New Image saved:" + photoFile,
                    Toast.LENGTH_LONG).show();
        } catch (Exception error) {
            Log.e(TAG, "Image could not be saved.");
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        stopSelf();

    }

    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "ServiceCamera");
    }

}
