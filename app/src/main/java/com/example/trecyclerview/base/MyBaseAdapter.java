package com.example.trecyclerview.base;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class MyBaseAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<T> mData;
    private int[] mLayoutIds;
    public MyBaseAdapter(List<T> data,int...layoutIds){
        this.mData = data;
        this.mLayoutIds = layoutIds;
    }

    public int getLayoutId(int viewType){
        return mLayoutIds[0];
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return BaseViewHolder.get(parent,getLayoutId(viewType));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                convert((BaseViewHolder) holder,position);
    }

    //子类必须覆写，UI
    public abstract void convert(BaseViewHolder holder, int position);


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public List<T> getmData(){
        return mData;
    }

}
