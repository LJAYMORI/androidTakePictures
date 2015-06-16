package com.ljaymori.takepicture;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class MyRecyclerAdapter extends RecyclerView.Adapter<ImageItemView> {

    private ArrayList<Bitmap> items = new ArrayList<Bitmap>();
    private Context mContext;

    public MyRecyclerAdapter(Context context) {
        mContext = context;
    }

    public void add(Bitmap bm, int position) {
        items.add(bm);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public ImageItemView onCreateViewHolder(ViewGroup viewGroup, final int position) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_image, viewGroup, false);

        ImageItemView itemView = new ImageItemView(v);
        itemView.setOnDeleteListener(new ImageItemView.OnDeleteListener() {
            @Override
            public void onDelete() {
                remove(position);
            }
        });

        return itemView;
    }

    @Override
    public void onBindViewHolder(ImageItemView itemView, int position) {
        itemView.setImage(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
