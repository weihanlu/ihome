package com.qhiehome.ihome.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.PublishParkingAdapter;
import com.qhiehome.ihome.util.LogUtil;

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
    // 切换按钮
    private SwitchCompat mSwitcher;
    // 重新发布（仅一次）
    TextView mTvRepublishDate;
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
                        return false;
                    }
                    final PublishParkingAdapter.PublishParkingHolder viewHolder = (PublishParkingAdapter.PublishParkingHolder) getChildViewHolder(view);
                    mItemLayout = viewHolder.mLayout;
                    mPosition = viewHolder.getLayoutPosition();
                    mTvRepublishDate = (TextView) mItemLayout.findViewById(R.id.tv_date_selected);
                    mSwitcher = (SwitchCompat) mItemLayout.findViewById(R.id.sc_republish);
                    mSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (mListener != null) {
//                                mListener.onToggleRepublish(buttonView, isChecked, mPosition, mTvRepublishDate);
                            }
                        }
                    });
                    mDelete = (TextView) mItemLayout.findViewById(R.id.tv_item_delete);
                    mMaxLength = mDelete.getWidth();
                    mDelete.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mListener != null) {
                                mListener.onCallbackPublish(v, mPosition);
                                mItemLayout.scrollTo(0, 0);
                                mDeleteBtnState = DeleteBtnState.CLOSED;
                            }
                        }
                    });
                } else if (mDeleteBtnState == DeleteBtnState.OPENED) {
                    mScroller.startScroll(mItemLayout.getScrollX(), 0, -mMaxLength, 0, 200);
                    invalidate();
                    mDeleteBtnState = DeleteBtnState.CLOSED;
                    return false;
                } else {
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = mLastX - x;
                int dy = mLastY - y;

                int scrollX = mItemLayout.getScrollX();
                if (Math.abs(dx) > Math.abs(dy)) {
                    isItemMoving = true;
                    if (scrollX + dx <= 0) {    // 左边界检测
                        mItemLayout.scrollTo(0, 0);
                        return true;
                    } else if (scrollX + dx >= mMaxLength) {    // 右边界检测
                        mItemLayout.scrollTo(mMaxLength, 0);
                        return true;
                    }
                    mItemLayout.scrollBy(dx, 0);   // item随手势滑动
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isItemMoving && !isDragging && mListener != null) {
                    mListener.onItemClick(mItemLayout, mPosition);
                }
                isItemMoving = false;

                mVelocityTracker.computeCurrentVelocity(1000);  // 计算滑动速度
                float xVelocity = mVelocityTracker.getXVelocity(); // 水平速度
                float yVelocity = mVelocityTracker.getYVelocity(); // 垂直速度

                int deltaX = 0;
                int upScrollX = mItemLayout.getScrollX();

                if (Math.abs(xVelocity) > 100 && Math.abs(xVelocity) > Math.abs(yVelocity)) {
                    if (xVelocity <= - 100) { // 左滑速度大于100
                        deltaX = mMaxLength - upScrollX;
                        mDeleteBtnState = DeleteBtnState.OPENING;
                    } else if (xVelocity > 100) { // 右滑速度大于100
                        deltaX = - upScrollX;
                        mDeleteBtnState = DeleteBtnState.CLOSING;
                    }
                } else {
                    if (upScrollX >= mMaxLength / 2) { // item左滑距离大于按钮宽度的一般，则显示删除按钮，否则隐藏
                        deltaX = mMaxLength - upScrollX;
                        mDeleteBtnState = DeleteBtnState.OPENING;
                    } else {
                        deltaX = - upScrollX;
                        mDeleteBtnState = DeleteBtnState.CLOSING;
                    }
                }
                mScroller.startScroll(upScrollX, 0, deltaX, 0, 200);
                isStartScroll = true;
                invalidate();

                mVelocityTracker.clear();
                break;
        }

        mLastX  = x;
        mLastY = y;
        return super.onTouchEvent(e);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mItemLayout.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        } else if (isStartScroll) {
            isStartScroll = false;
            if (mDeleteBtnState == DeleteBtnState.CLOSING) {
                mDeleteBtnState = DeleteBtnState.CLOSED;
            }
            if (mDeleteBtnState == DeleteBtnState.OPENING) {
                mDeleteBtnState = DeleteBtnState.OPENED;
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mVelocityTracker.recycle();
        super.onDetachedFromWindow();
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        isDragging = state == SCROLL_STATE_DRAGGING;
    }

    public void setOnItemClickListneer(PublishParkingAdapter.OnItemClickListener onItemClickListneer) {
        mListener = onItemClickListneer;
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
