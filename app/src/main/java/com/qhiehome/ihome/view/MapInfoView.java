package com.qhiehome.ihome.view;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.map.DrivingRouteOverlay;

/**
 * Created by YueMa on 2017/9/26.
 */

public class MapInfoView extends RelativeLayout {

    private Context mContext;
    private View mView;

    private DrivingRouteOverlay overlay;


    public MapInfoView(Context context) {
        this(context, null);
    }

    public MapInfoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mView = LayoutInflater.from(context).inflate(R.layout.layout_estate_info, this, true);
        this.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.transparent));
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE){
            this.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.pop_enter_anim));
        }else if (visibility == View.GONE){
            try {
                this.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.pop_exit_anim));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onAnimationEnd() {
        super.onAnimationEnd();
        if (overlay!= null){
            overlay.zoomToSpan();
        }
    }

    public View getmView() {
        return mView;
    }

    @Override
    public int getVisibility() {
        return super.getVisibility();
    }

    public void setOverlay(DrivingRouteOverlay overlay) {
        this.overlay = overlay;
    }

}
