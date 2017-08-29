package com.qhiehome.ihome.view;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

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

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
