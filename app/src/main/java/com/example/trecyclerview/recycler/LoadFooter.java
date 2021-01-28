package com.example.trecyclerview.recycler;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.trecyclerview.R;

public class LoadFooter extends LinearLayout implements BaseLoadFooter {

    private View mContainer;
    private ProgressBar mProgressBar;
    private TextView mText;
    private int mState;
    private int mHeight;
    private Handler mHandler;

    public LoadFooter(Context context) {
        this(context, null);
    }

    public LoadFooter(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadFooter(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        mContainer = LayoutInflater.from(context).inflate(R.layout.load_footer, this, false);
        mProgressBar = mContainer.findViewById(R.id.footer_process_bar);
        mText = mContainer.findViewById(R.id.footer_load_text);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //初始状态下footer不可见
        addView(mContainer,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0));
        mState = STATE_NORMAL;
        measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mHeight = getMeasuredHeight();
        Log.d("taoke","height:"+mHeight);
        mHandler = new Handler();
    }

    @Override
    public void onLoading() {
        onStateChange(STATE_LOADING);
    }

    @Override
    public void onComplete() {
        onStateChange(STATE_COMPLETE);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onStateChange(STATE_NORMAL);
            }
        }, 200);
    }

    @Override
    public void onNoMore() {
        onStateChange(STATE_NO_MORE);
    }

    @Override
    public void onStateChange(int state) {
        switch (state) {
            case STATE_NORMAL:
                //Complete -> Normal,加载完成后设置footer高度为0
                mProgressBar.setVisibility(View.VISIBLE);
                mText.setText(R.string.loading);
                break;
            case STATE_LOADING:
                //Normal -> Loading,恢复正常高度
                mText.setText(R.string.loading);
                mProgressBar.setVisibility(View.VISIBLE);
                //setHeight(mHeight);
                break;
            case STATE_COMPLETE:
                //Loading -> Complete
                mProgressBar.setVisibility(View.GONE);
                mText.setText(R.string.load_complete);
                break;
            case STATE_NO_MORE:
                //Loading -> NoMore 加载出错了或者没有更多内容了
                mProgressBar.setVisibility(View.GONE);
                mText.setText(R.string.load_no_more);
                break;
        }
        mState = state;
    }

    public void setHeight(int height) {
        ViewGroup.LayoutParams params = mContainer.getLayoutParams();
        params.height = height;
        mContainer.setLayoutParams(params);
    }

    public void reset(){
        setHeight(mHeight);
    }

    public int getNormalHeight(){
        return mHeight;
    }

    public int getContainerHeight(){
        ViewGroup.LayoutParams params = mContainer.getLayoutParams();
        return params.height;
    }

    public int getState(){
        return mState;
    }


}
