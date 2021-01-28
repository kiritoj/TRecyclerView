package com.example.trecyclerview.recycler;

/**
 *刷新header的状态以及回调方法
 */
public interface BaseRefreshHeader {
    //正常状态，header的整体高度 <= 预设的标准高度
    int STATE_NORMAL = 0;
    //可以释放刷新的状态，header的整体高度 > 标准高度
    int STATE_RELEASE = 1;
    //正在刷新的状态，此时header高度 == 标准高度
    int STATE_REFRESHING = 2;
    //刷新完成
    int STATE_COMPLETE = 3;

    //改变header高度
    void onMove(float distance);

    /**
     * @return 释放时是否需要刷新
     */
    boolean onRelease();
    //刷新完成
    void onComplete();
    //状态改变
    void onStateChange(int state);
}
