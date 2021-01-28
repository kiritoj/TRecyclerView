package com.example.trecyclerview.base;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class BaseViewHolder extends RecyclerView.ViewHolder {
    private SparseArray<View> mViews;
    private View mItemView;

    private BaseViewHolder(@NonNull View itemView) {
        super(itemView);
        this.mItemView = itemView;
        mViews = new SparseArray<>();
    }

    public static BaseViewHolder get(ViewGroup parent,int layoutId){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layoutId,parent,false);
        return new BaseViewHolder(itemView);
    }

    public <T extends View> T getView(int viewId){
        View view = mViews.get(viewId);
        if (view == null){
            view = mItemView.findViewById(viewId);
            mViews.put(viewId,view);
        }
        return (T) view;
    }

    public void setText(int viewId,String text){
        TextView textView = getView(viewId);
        textView.setText(text);
    }

    public BaseViewHolder setImage(int viewId, int sourceId){
        ImageView imageView = getView(viewId);
        imageView.setImageResource(sourceId);
        return this;
    }

//    public BaseViewHolder setImageWithNet(int viewId, String url,Context context){
//        ImageView imageView = getView(viewId);
//        Glide.with(context).load(url).into(imageView);
//        return this;
//    }

    public BaseViewHolder setOnClickListener(int viewId,View.OnClickListener listener){
        View view = getView(viewId);
        view.setOnClickListener(listener);
        return this;
    }

    public BaseViewHolder setOnLongClickListener(int viewId,View.OnLongClickListener listener){
        View view = getView(viewId);
        view.setOnLongClickListener(listener);
        return this;
    }

}
