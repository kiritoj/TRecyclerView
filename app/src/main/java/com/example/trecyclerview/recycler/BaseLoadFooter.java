package com.example.trecyclerview.recycler;

/**
 * 加载更多的footer
 */
public interface BaseLoadFooter {
    int STATE_NORMAL = 0; //初始状态，高度为0，是不显示的
    int STATE_LOADING = 1; //加载状态，可见
    int STATE_COMPLETE = 2; //加载完成，可见
    int STATE_NO_MORE = 3; //没有更多数据，可见

    void onLoading();
    void onComplete();
    void onNoMore();
    void onStateChange(int state);
}
