package com.qhiehome.ihome.view;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.qhiehome.ihome.R;

/**
 * Base popupWindow
 */

public class BasePopupWindow extends PopupWindow {

    private Activity mActivity;
    private long mAnimatorDuration;

    public BasePopupWindow(Context context, Activity activity, long animatorDuration) {
        super(context);
        mActivity = activity;
        mAnimatorDuration = animatorDuration;
        initBasePopupWindow();
    }

    /**
     * 调整窗口透明度
     * @param from 初始透明度
     * @param to 结束透明度
     */
    private void setOutBackground(float from, float to) {
        final WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(from, to);
        valueAnimator.setDuration(mAnimatorDuration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                lp.alpha = (float) animation.getAnimatedValue();
                mActivity.getWindow().setAttributes(lp);
            }
        });
        valueAnimator.start();
    }

    /**
     * 初始化BasePopupWindow的一些信息
     * */
    private void initBasePopupWindow() {
        setAnimationStyle(R.style.PopWindowAnim);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOutsideTouchable(false);  //默認設置outside點擊無響應
        setFocusable(false);
    }



    @Override
    public void setContentView(View contentView) {
        if(contentView != null) {
            contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            super.setContentView(contentView);
            addKeyListener(contentView);
        }
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    /**
     * 為窗體添加outside點擊事件
     * */
    private void addKeyListener(View contentView) {
        if(contentView != null) {
            contentView.setFocusable(true);
            contentView.setFocusableInTouchMode(true);
            contentView.setOnKeyListener(new View.OnKeyListener() {

                @Override
                public boolean onKey(View view, int keyCode, KeyEvent event) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            dismiss();
                            return true;
                        default:
                            break;
                    }
                    return false;
                }
            });
        }
    }
}
