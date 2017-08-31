package com.qhiehome.ihome.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.qhiehome.ihome.adapter.PublishParkingAdapter;

/**
 * extends all properties of recyclerView and support empty view if dataset is empty.
 */

public class RecyclerViewEmptySupport extends RecyclerView {

    private static final String TAG = "RecyclerViewEmptySupport";

    private Context mContext;

    // 上一次的触摸点
    private int mLastX, mLastY;
    // 当前触摸的item的位置
    private int mPosition;
    // item对应的布局
    private LinearLayout mItemLayout;
    // 删除对应的按钮
    private TextView mDelete;
    // 最大滑动距离（删除按钮的宽度）
    private int mMaxLength;
    // 是否垂直滑动列表
    private boolean isDragging;
    // item是否跟随手指滑动
    private boolean isItemMoving;
    // item是否开始滑动
    private boolean isStartScroll;

    private DeleteBtnState mDeleteBtnState = DeleteBtnState.CLOSED;

    private enum DeleteBtnState {
        CLOSED,
        CLOSING,
        OPENING,
        OPENED
    }
    // 检测手指滑动速度
    private VelocityTracker mVelocityTracker;
    // 实现滑动效果
    private Scroller mScroller;
    private PublishParkingAdapter.OnItemClickListener mListener;

    private View emptyView;

    private AdapterDataObserver emptyObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            Adapter<?> adapter = getAdapter();
            if (adapter != null && emptyView != null) {
                if (adapter.getItemCount() == 0) {
                    emptyView.setVisibility(VISIBLE);
                    RecyclerViewEmptySupport.this.setVisibility(GONE);
                } else {
                    emptyView.setVisibility(GONE);
                    RecyclerViewEmptySupport.this.setVisibility(VISIBLE);
                }
            }
        }
    };

    public RecyclerViewEmptySupport(Context context) {
        this(context, null);
    }

    public RecyclerViewEmptySupport(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerViewEmptySupport(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mScroller = new Scroller(context, new LinearInterpolator());
        mVelocityTracker = VelocityTracker.obtain();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        mVelocityTracker.addMovement(e);

        int x = (int) e.getX();
        int y = (int) e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mDeleteBtnState == DeleteBtnState.CLOSED) {
                    View view = findChildViewUnder(x, y);
                    if (view == null) {

                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        mLastX  = x;
        mLastY = y;
        return super.onTouchEvent(e);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(emptyObserver);
        }
        emptyObserver.onChanged();
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
    }
}
