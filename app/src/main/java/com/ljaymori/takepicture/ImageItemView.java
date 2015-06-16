package com.ljaymori.takepicture;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class ImageItemView extends RecyclerView.ViewHolder {

    private ImageView ivPicture;
    private Button btnDel;

    public ImageItemView(View itemView) {
        super(itemView);

        ivPicture = (ImageView) itemView.findViewById(R.id.image_picture);
        btnDel = (Button) itemView.findViewById(R.id.button_del_picture);
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(deleteListener != null) {
                    deleteListener.onDelete();
                }
            }
        });
    }

    public void setImage(Bitmap bm) {
        ivPicture.setImageBitmap(bm);
    }

    public interface OnDeleteListener {
        void onDelete();
    }
    OnDeleteListener deleteListener;
    public void setOnDeleteListener(OnDeleteListener listener) {
        deleteListener = listener;
    }


}
