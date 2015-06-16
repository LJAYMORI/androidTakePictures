package com.ljaymori.takepicture;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class MainActivity extends ActionBarActivity implements View.OnClickListener, SurfaceHolder.Callback {

    private static final int TAKE_PHOTO_CODE = 0;

    private RecyclerView recyclerView;
    private SurfaceView preview;
    private Button btnChange, btnTake;
    private MyRecyclerAdapter mAdapter;

    private Camera mCamera;
    private int cameraId;
    private SurfaceHolder mHolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preview = (SurfaceView) findViewById(R.id.surfaceView);
        preview.getHolder().addCallback(this);
        preview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

//        mCamera = Camera.open();
//        cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
//        mCamera.setDisplayOrientation(90);

        btnChange = (Button) findViewById(R.id.button_change_picture);
        btnTake = (Button) findViewById(R.id.button_take_picture);
        btnChange.setOnClickListener(this);
        btnTake.setOnClickListener(this);


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        mAdapter = new MyRecyclerAdapter(this);

        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.button_change_picture) {
            changeCamera();
        } else if (id == R.id.button_take_picture) {
            Log.i("takePicture", "click!");
            takePicture();
        }
    }

    private void takePicture() {
        Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                Log.i("takePicture", "onShutter");
            }
        };

        Camera.PictureCallback pictureCallbackRAW = new Camera.PictureCallback(){

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.i("takePicture", "onPictureTakenRAW");
            }
        };

        Camera.PictureCallback pictureCallbackSave = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.i("takePicture", "onPictureTakenSave");
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 4;

                Bitmap bmPhoto = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
                String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/picFolder";
                String filePath = "/picture_" + (System.currentTimeMillis() / 1000) + ".jpg";

                if (saveBitmapToFileCache(bmPhoto, directory, filePath)) {
                    Log.i("file path", directory+filePath);
                    mAdapter.add(directory + filePath, mAdapter.getItemCount());
                    try {
                        String url = MediaStore.Images.Media.insertImage(getContentResolver(), directory+filePath, "my image", "test image");
                        Uri uri = Uri.parse(url);
                        if (uri != null) {
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                        }
                    } catch (FileNotFoundException e) {
                        Log.i("fileNotFount", e.toString());
                        e.printStackTrace();
                    }

                } else {
                    Log.i("save file error", directory + filePath);
                    removeFile(directory + filePath);

                }
            }
        };

        mCamera.takePicture(shutterCallback, pictureCallbackRAW, pictureCallbackSave);
    }

    public void removeFile(String path){
        File f = new File(path);
        if(f.exists()){
            f.delete();
        }
    }

    private void changeCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        cameraId = (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
        mCamera = Camera.open(cameraId);
        mCamera.setDisplayOrientation(90);
        try {
            if (mHolder != null) {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Bitmap imgRotate(Bitmap bmp){
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        bmp.recycle();

        return resizedBitmap;
    }

    private boolean saveBitmapToFileCache(Bitmap bitmap, String directory, String filename) {
        Log.i("saveBitmap - bitmap", bitmap.getConfig().toString());
        boolean isSuccess = true;

        File file = new File(directory);

        // If no folders
        if (!file.exists()) {
            file.mkdirs();
            // Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        }

        File fileCacheItem = new File(directory + filename);
        OutputStream out = null;

        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//            Log.i(TAG, "filesize : " + fileCacheItem.length() / (long) 1024 + "KB");
        } catch (Exception e) {
            isSuccess = false;
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                isSuccess = false;
                e.printStackTrace();
            }
        }

        Log.i("fileCacheItem", fileCacheItem.getAbsolutePath());
        return isSuccess;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        if (mCamera == null) {
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            mCamera = Camera.open(cameraId);

            mCamera.setDisplayOrientation(90);
        }
        try {
            mCamera.setPreviewDisplay(mHolder);

//            Camera.Parameters params = mCamera.getParameters();
//            List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();
//            for(Camera.Size size : pictureSizes) {
//                Log.i("size list", "width:" + size.width + ", height: " + size.height);
//            }
//            params.setPictureSize(1024, 1920);
//            mCamera.setParameters(params);

            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            mCamera.stopPreview();
        } catch(Exception e) {

        }

        mHolder = holder;
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder = null;
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }
}
