package com.qhiehome.ihome.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.ShareAdapter;

public class SharePopupWindow extends BasePopupWindow {
    private Context mContext;

    private View view;

    private RecyclerView mRvShare;

    public SharePopupWindow(Context context, Activity activity, long animationDuration) {
        super(context, activity, animationDuration);

        mContext = context;

        view = LayoutInflater.from(mContext).inflate(R.layout.dialog_share, null);

        mRvShare = (RecyclerView) view.findViewById(R.id.rv_share);

        initRecyclerView();

        // 设置外部可点击
        this.setOutsideTouchable(true);
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        this.view.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = view.findViewById(R.id.layout_share).getTop();

                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

        // 设置视图
        this.setContentView(view);
        // 设置弹出窗体的宽和高
        this.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);

        // 设置弹出窗体可点击
        this.setFocusable(true);

        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        // 设置弹出窗体的背景
        this.setBackgroundDrawable(dw);

        // 设置弹出窗体显示时的动画，从底部向上弹出
        this.setAnimationStyle(R.style.share);

    }

    private void initRecyclerView(){
        mRvShare.setHasFixedSize(true);
        mRvShare.setLayoutManager(new GridLayoutManager(mContext,3));
        mRvShare.setAdapter(new ShareAdapter(mContext));
    }
}
