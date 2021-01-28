package com.example.trecyclerview.recycler;

public interface RefreshListener {
    void onRefresh();
    void onOnLoad();
    void onRefreshTimeOut();
    void onLoadMoreTimeOut();
}
