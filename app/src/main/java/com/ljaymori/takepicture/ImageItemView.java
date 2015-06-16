package com.ljaymori.takepicture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

public class ImageItemView extends RecyclerView.ViewHolder {

    private ImageView ivPicture;
    private Button btnDel;
    private String imagePath;

    public ImageItemView(View itemView) {
        super(itemView);

        ivPicture = (ImageView) itemView.findViewById(R.id.image_picture);
        btnDel = (Button) itemView.findViewById(R.id.button_del_picture);
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(deleteListener != null) {
                    deleteListener.onDelete(imagePath);
                }
            }
        });
    }

    public void setImage(String path) {
        File imgFile = new  File(path);
        if(imgFile.exists()){
            imagePath = path;
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ivPicture.setImageBitmap(bitmap);
        }
    }

    public interface OnDeleteListener {
        void onDelete(String path);
    }
    OnDeleteListener deleteListener;
    public void setOnDeleteListener(OnDeleteListener listener) {
        deleteListener = listener;
    }


}
