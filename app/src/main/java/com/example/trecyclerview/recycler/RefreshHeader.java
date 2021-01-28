package com.example.trecyclerview.recycler;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.example.trecyclerview.R;



/**
 * 刷新的头部
 */
public class RefreshHeader extends LinearLayout implements BaseRefreshHeader {

    private View mContainer;
    private ImageView mArrow; //刷新箭头图标
    private TextView mText; //刷新文案
    private ProgressBar mProgressBar;
    private int mHeight; //标准高度
    private RotateAnimation mUpRotate; //顺时针向上旋转动画
    private RotateAnimation mDownRotate; //逆时针向下旋转动画
    private int mState = STATE_NORMAL;
    private Handler mHandler;

    public RefreshHeader(Context context) {
        this(context, null);
    }

    public RefreshHeader(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RefreshHeader(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mContainer = LayoutInflater.from(getContext()).inflate(R.layout.refresh_header,this,false);
        mArrow = mContainer.findViewById(R.id.iv_arrow);
        mText = mContainer.findViewById(R.id.tv_refresh_text);
        mProgressBar = mContainer.findViewById(R.id.progress_bar);
        //旋转中心点为当前视图的中心
        mUpRotate = new RotateAnimation(0,-180, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        mUpRotate.setDuration(500);
        mUpRotate.setFillAfter(true);
        mDownRotate = new RotateAnimation(-180,0,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        mDownRotate.setDuration(500);
        mDownRotate.setFillAfter(true);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //初始时header是不显示的，高度为0
        addView(mContainer,new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0));
        measure(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        mHeight = getMeasuredHeight();
        mHandler = new Handler();

    }

    public int getContainerHeight(){
        return mContainer.getLayoutParams().height;
    }

    public void setContainerHeight(float height){
        ViewGroup.LayoutParams params = mContainer.getLayoutParams();
        params.height = (int) height;
        mContainer.setLayoutParams(params);
    }

    @Override
    public void onMove(float height) {
        if (height > 0){
            //动态改变header的高度
            setContainerHeight(getContainerHeight() + height);
            //高度的变化带来两种状态的改变
            //NORMAL -> RELEASE
            if (mState == STATE_NORMAL && getContainerHeight() > mHeight){
                onStateChange(STATE_RELEASE);
            }else if (mState == STATE_RELEASE && getContainerHeight() <= mHeight){
                //RELEASE -> NORMAL
                onStateChange(STATE_NORMAL);
            }

        }
    }

    //平滑改变高度到目标点
    public void scrollTo(int destHeight){
        ValueAnimator valueAnimator = ValueAnimator.ofInt(getContainerHeight(),destHeight);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setContainerHeight((int)animation.getAnimatedValue());
            }
        });
        valueAnimator.setDuration(300).start();
    }

    @Override
    public boolean onRelease() {
        boolean needRefresh = false;
        //只有在Release状态下才需要刷新
        switch (mState) {
            case STATE_RELEASE:
                needRefresh = true;
                onStateChange(STATE_REFRESHING);
                //滑动到标准高度开始刷新
                scrollTo(mHeight);
                break;
            case STATE_NORMAL:
                //无需刷新，高度变为0
                scrollTo(0);
                break;
            case STATE_REFRESHING:
                //正在刷新的状态下，下拉释放回到标准高度继续刷新
                scrollTo(mHeight);
                break;
        }
        return needRefresh;
    }


    @Override
    public void onComplete() {
        //先保留一会刷新成功的提示，再恢复初始状态
        onStateChange(STATE_COMPLETE);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                reset();
            }
        },200);
    }

    //返回到初始状态
    public void reset(){
        //先把视图慢慢隐藏，之后在设置状态为初始值，提升体验
        scrollTo(0);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onStateChange(STATE_NORMAL);
            }
        },200);
    }

    @Override
    public void onStateChange(int state) {
        switch (state){
            case STATE_NORMAL:
                //有两种情况
                //Release  -> Normal
                //Complete -> Normal
                //所有视图回到初始状态
                Log.d("taoke","NORMAL");
                mArrow.setVisibility(View.VISIBLE);
                mArrow.clearAnimation();
                mText.setText(R.string.pull_bottom_refresh);
                mProgressBar.setVisibility(View.GONE);
                //如果前一个状态是release，还需要把箭头从上往下旋转
                if (mState == STATE_RELEASE){
                    mArrow.clearAnimation();
                    mArrow.startAnimation(mDownRotate);
                }
                break;
            case STATE_RELEASE:
                 //只有Normal -> Release这一种情况
                Log.d("taoke","Release");
                mArrow.setVisibility(View.VISIBLE);
                mArrow.clearAnimation();
                mArrow.startAnimation(mUpRotate);
                mText.setText(R.string.release_refresh);
                mProgressBar.setVisibility(View.GONE);
                break;
            case STATE_REFRESHING:
                //Release -> Refreshing
                mArrow.clearAnimation();
                mArrow.setVisibility(View.INVISIBLE);
                mText.setText(R.string.refreshing);
                mProgressBar.setVisibility(View.VISIBLE);
                break;
            case STATE_COMPLETE:
                //Refreshing -> Complete
                Log.d("taoke","complete");
                mArrow.setVisibility(View.GONE);
                mText.setText(R.string.refresh_complete);
                mProgressBar.setVisibility(View.GONE);
                break;

        }
        mState = state;
    }

    public int getState(){
        return mState;
    }
}
