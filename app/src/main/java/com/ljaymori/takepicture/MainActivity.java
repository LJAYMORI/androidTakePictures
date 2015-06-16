package com.ljaymori.takepicture;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


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

        mCamera = Camera.open();
        cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        mCamera.setDisplayOrientation(90);

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
            takePicture();
        }
    }

    private void takePicture() {
        Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                mCamera.stopPreview();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mCamera != null) {
                            mCamera.startPreview();
                        }
                    }
                }, 500);

            }
        };

        Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 4;
                Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length, opts);

                final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picFolder/";
                File newdir = new File(dir);
                newdir.mkdirs();

                String file = dir + (System.currentTimeMillis() / 1000) + ".jpg";
                File newfile = new File(file);

                try{
                    newfile.createNewFile();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Fail - create new file", Toast.LENGTH_SHORT).show();
                    bm.recycle();
                    bm = null;
                    return;
                }

                Uri outputFileUri = Uri.fromFile(newfile);

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

                startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);

                mAdapter.add(bm, mAdapter.getItemCount());

//                String url = MediaStore.Images.Media.insertImage(getContentResolver(), bm, "my image", "test image");
//                Uri uri = Uri.parse(url);
//
//                if (uri != null) {
//                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
//                    mAdapter.add(bm, mAdapter.getItemCount());
//                }
            }
        };


        mCamera.takePicture(shutterCallback, null, pictureCallback);
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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        if (mCamera == null) {
            mCamera = Camera.open(cameraId);
            mCamera.setDisplayOrientation(90);
        }
        try {
            mCamera.setPreviewDisplay(mHolder);
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
