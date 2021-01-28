package com.example.trecyclerview.recycler;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;


public class TRecyclerView extends RecyclerView {

    private RefreshHeader mHeader;
    private LoadFooter mFooter;
    private RefreshListener mListener;
    //下拉刷新开关,默认开启
    private boolean mPullRefreshEnable = true;
    //上拉加载开关,默认开启
    private boolean mLoadMoreEnable = true;
    private float mLastY;
    private WarpperAdapter mWarpperAdapter;
    private AdapterDataObserver mObserver;
    private int mLastVisibalPosition;
    private Handler mHandler;
    private Runnable mRefreshTimeoutRunnable; //刷新超时处理
    private Runnable mLoadMoreTimeTimeRunnable; //加载超时处理
    private static final int TYPE_REFRESH_HEADER = 1;
    private static final int TYPE_LOAD_FOOTER = 2;
    private static final int TYPE_ERROR = -1;
    private static final int TIME_OUT = 5000;//刷新，加载默认超时时间
    private int mRefreshTimeOut = TIME_OUT;
    private int mLoadMoreTimeOut = TIME_OUT;

    public TRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public TRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        mHeader = new RefreshHeader(context);
        mFooter = new LoadFooter(context);
        mHandler = new Handler();
    }


    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        mWarpperAdapter = new WarpperAdapter(adapter);
        super.setAdapter(mWarpperAdapter);
        mObserver = new DataObserver();
        adapter.registerAdapterDataObserver(mObserver);
    }

    public Adapter getAdapter() {
        if (mWarpperAdapter != null) {
            return mWarpperAdapter.mAdapter;
        }
        return null;
    }

    public void refreshComplete() {
        mHeader.onComplete();
        mHandler.removeCallbacks(mRefreshTimeoutRunnable);
    }

    public void loadComplete(){
        mFooter.onComplete();
        mHandler.removeCallbacks(mLoadMoreTimeTimeRunnable);
    }

    public void loadNoMore(){
        mFooter.onNoMore();
        mHandler.removeCallbacks(mLoadMoreTimeTimeRunnable);
    }

    //包装适配器类
    class WarpperAdapter extends Adapter<ViewHolder> {

        private Adapter mAdapter;

        private WarpperAdapter(Adapter adapter) {
            this.mAdapter = adapter;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_REFRESH_HEADER) {
                return new SimpleViewHolder(mHeader);
            }
            if (viewType == TYPE_LOAD_FOOTER) {
                return new SimpleViewHolder(mFooter);
            }
            return mAdapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (isRefreshHeader(position) || isLoadFooter(position)) {
                return;
            }
            if (mAdapter != null) {
                mAdapter.onBindViewHolder(holder, getOuterPosition(position));
            }

        }

        @Override
        public int getItemCount() {
            int count = mAdapter.getItemCount();
            count = mPullRefreshEnable ? count + 1 : count;
            count = mLoadMoreEnable ? count + 1 : count;
            return count;
        }

        @Override
        public int getItemViewType(int position) {
            if (isRefreshHeader(position)) {
                return TYPE_REFRESH_HEADER;
            }
            if (isLoadFooter(position)) {
                return TYPE_LOAD_FOOTER;
            }
            //实际的位置，取决于header是否存在
            int readPosition = getOuterPosition(position);
            if (mAdapter != null) {
                return mAdapter.getItemViewType(readPosition);
            }
            return TYPE_ERROR;
        }

        //根据item的位置判断是不是header
        public boolean isRefreshHeader(int position) {
            if (mPullRefreshEnable) {
                return position == 0;
            }
            return false;
        }

        //判断item位置是不是footer
        public boolean isLoadFooter(int position) {
            if (mLoadMoreEnable) {
                return position == getItemCount() - 1;
            }
            return false;
        }

        //网格布局下刷新header/footer填充整个宽度
        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            LayoutManager manager = recyclerView.getLayoutManager();
            if (manager instanceof GridLayoutManager) {
                final GridLayoutManager gridLayoutManager = (GridLayoutManager) manager;
                GridLayoutManager.SpanSizeLookup sizeLookup = new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (isRefreshHeader(position) || isLoadFooter(position)) {
                            //header或footer占满全部
                            return gridLayoutManager.getSpanCount();
                        } else {
                            //正常的item只占一个宽度
                            return 1;
                        }
                    }
                };
                gridLayoutManager.setSpanSizeLookup(sizeLookup);
            } else if (mAdapter != null) {
                mAdapter.onAttachedToRecyclerView(recyclerView);
            }
        }

        //流式布局下刷新header/footer填充整个宽度
        @Override
        public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            if (params instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) params;
                if (isRefreshHeader(holder.getLayoutPosition()) || isLoadFooter(holder.getLayoutPosition())) {
                    layoutParams.setFullSpan(true);
                }
            } else if (mAdapter != null) {
                mAdapter.onViewAttachedToWindow(holder);
            }
        }
    }

    /**
     * wrapperAdapter和外部Adapter间position转换
     *
     * @param position WrapperAdapter item的positon
     * @return 外部Adapter对应item的position
     */
    public int getOuterPosition(int position) {
        return mPullRefreshEnable ? position - 1 : position;
    }

    /**
     * wrapperAdapter和外部Adapter间position转换
     *
     * @param position 外部Adapter对应item的position
     * @return WrapperAdapter对应item的position
     */
    public int getWrapperPosition(int position) {
        return mPullRefreshEnable ? position + 1 : position;
    }

    //适配器数据观察者
    class DataObserver extends AdapterDataObserver {
        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mWarpperAdapter.notifyItemRangeChanged(getWrapperPosition(positionStart), itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mWarpperAdapter.notifyItemRangeInserted(getWrapperPosition(positionStart), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mWarpperAdapter.notifyItemRangeRemoved(getWrapperPosition(positionStart), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mWarpperAdapter.notifyItemMoved(getWrapperPosition(fromPosition), getWrapperPosition(toPosition));
        }
    }


    public boolean isHeaderVisible() {
        return mHeader.getParent() != null;
    }

    //重写onTouchEvent方法用于实现header的动态变化
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = e.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                //滑动更改header的高度
                float gap = e.getRawY() - mLastY;
                mLastY = e.getRawY();
                if (mPullRefreshEnable && isHeaderVisible()) {
                    mHeader.onMove(gap / 3);
                    //直接返回true的话，刚开始尝试从上往下划动时所有move事件都会被消耗，导致不能滑动
                    //Header正在刷新的时候也不能返回true，否则在刷新的时候也不能上下滑动
//                    if (mHeader.getContainerHeight() > 0 && mHeader.getState() != mHeader.STATE_REFRESHING) {
//                        return true;
//                    }
                    return true;
                }
                break;
            default:
                //释放刷新
                if (mPullRefreshEnable && isHeaderVisible() && mHeader.onRelease()) {
                    if (mListener != null) {
                        mListener.onRefresh();
                        //刷新超时处理
                        mHandler.postDelayed(mRefreshTimeoutRunnable,mRefreshTimeOut);
                        return true;
                    }
                }
        }
        return super.onTouchEvent(e);
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        LayoutManager manager = getLayoutManager();
        if (manager.getChildCount() < manager.getItemCount() && mFooter.getContainerHeight() == 0){
            mFooter.reset();
        }
        if (mLoadMoreEnable && state == RecyclerView.SCROLL_STATE_IDLE){
            if (manager instanceof LinearLayoutManager) {
                mLastVisibalPosition = ((LinearLayoutManager) manager).findLastVisibleItemPosition();
            } else {
                //流式布局不能直接得到当前显示的最大item position
                StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) manager;
                int[] lastPositions = new int[layoutManager.getSpanCount()];
                layoutManager.findLastVisibleItemPositions(lastPositions);
                mLastVisibalPosition = findMaxInArray(lastPositions);
            }
            //滑动到底部，且未开始加载中，且当前显示在屏幕中的item数量小于全部数量，避免item数量不足以铺满屏幕的情况下也加载
            if (mLastVisibalPosition >= manager.getItemCount() - 1 &&
                    !isLoading() && manager.getChildCount() > 0 && manager.getChildCount() < manager.getItemCount()) {
                mFooter.onLoading();
                if (mListener != null){
                    mListener.onOnLoad();
                    //加载超时处理
                    mHandler.postDelayed(mLoadMoreTimeTimeRunnable,mLoadMoreTimeOut);
                }
            }
        }
    }

    //是否正在加载中
    public boolean isLoading() {
        return mFooter.getState() == mFooter.STATE_LOADING;
    }

    public int findMaxInArray(int[] array) {
        int max = array[0];
        for (int a : array) {
            if (a > max) {
                max = a;
            }
        }
        return max;
    }

    class SimpleViewHolder extends ViewHolder {

        private SimpleViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public void setPullRefreshEnable(boolean enable) {
        this.mPullRefreshEnable = enable;
    }

    public void setLoadMoreEnable(boolean enable) {
        this.mLoadMoreEnable = enable;
    }

    public boolean isPullRefreshEnable() {
        return mPullRefreshEnable;
    }

    public boolean isLoadMoreEnable() {
        return mLoadMoreEnable;
    }

    public void setRefreshTimeOut(int timeOut){
        this.mRefreshTimeOut = timeOut;
    }

    public void setLoadMoreTimeOut(int timeOut){
        this.mLoadMoreTimeOut = timeOut;
    }

    public void setRefreshListener(RefreshListener listener) {
        this.mListener = listener;
        mRefreshTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                mListener.onRefreshTimeOut();
            }
        };
        mLoadMoreTimeTimeRunnable = new Runnable() {
            @Override
            public void run() {
                mListener.onLoadMoreTimeOut();
            }
        };
    }

}
