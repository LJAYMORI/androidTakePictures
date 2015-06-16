package com.ljaymori.takepicture;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class MyRecyclerAdapter extends RecyclerView.Adapter<ImageItemView> {

    private ArrayList<String> items = new ArrayList<String>();
    private Context mContext;

    public MyRecyclerAdapter(Context context) {
        mContext = context;
    }

    public void add(String path, int position) {
        items.add(path);
        notifyItemInserted(position);
    }

    public void remove(String path) {
        int position = items.indexOf(path);
        if(position == -1) {

        } else {
            ((MainActivity)mContext).removeFile(items.remove(position));
            notifyItemRemoved(position);
        }
    }

    @Override
    public ImageItemView onCreateViewHolder(ViewGroup viewGroup, final int position) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_image, viewGroup, false);

        ImageItemView itemView = new ImageItemView(v);
        itemView.setOnDeleteListener(new ImageItemView.OnDeleteListener() {
            @Override
            public void onDelete(String path) {
                remove(path);
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
