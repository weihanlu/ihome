package com.qhiehome.ihome.behavior;

import android.content.Context;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.baidu.mapapi.map.Circle;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserInfoBehavior extends CoordinatorLayout.Behavior<CircleImageView>{

    private static final String TAG = UserInfoBehavior.class.getSimpleName();
    private int maxScrollDistance;
    private float maxChildWidth;
    private float minChildWidth;

    private int toolbarHeight;
    private int appBarStartPoint;

    public UserInfoBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        //
        minChildWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32,
                context.getResources().getDisplayMetrics());
        toolbarHeight = context.getResources()
                .getDimensionPixelSize(android.support.design.R.dimen.abc_action_bar_default_height_material);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, CircleImageView child, View dependency) {
        // 确定依赖关系，ImageView 依赖的是AppBarLayout
        return dependency instanceof AppBarLayout;
    }

    private int startX;
    private int startY;

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, CircleImageView child, View dependency) {
        // child为头像，dependency是AppBarLayout
        if (maxScrollDistance == 0) {
            maxScrollDistance = dependency.getBottom() - toolbarHeight;
        }
        // 计算出appbar开始时的y坐标
        if (appBarStartPoint == 0) {
            appBarStartPoint = dependency.getBottom();
        }
        // 计算出头像的宽度
        if (maxChildWidth == 0) {
            maxChildWidth = Math.min(child.getWidth(), child.getHeight());
        }
        // 计算出头像的起始x坐标
        if (startX == 0) {
            startX = (int) (dependency.getWidth() /2 - maxChildWidth / 2);
        }
        // 计算出头像的起始y坐标
        if (startY == 0) {
            startY = (int) (dependency.getBottom() - maxScrollDistance / 2
                    - maxChildWidth / 2 - toolbarHeight / 2);
        }
        // 计算出appBar已经变化距离的百分比，起始位置 - 当前位置
        float expandedPercentageFactor = (appBarStartPoint - dependency.getBottom()) * 1.0f /
                (maxScrollDistance * 1.0f);
        float moveY = expandedPercentageFactor * (maxScrollDistance -
                (appBarStartPoint - startY - toolbarHeight / 2 - minChildWidth / 2));
        // 更新头像位置
        child.setY(startY - moveY);
        // 计算当前头像宽度
        float nowWidth = maxChildWidth - ((maxChildWidth - minChildWidth) * expandedPercentageFactor);
        child.setX((dependency.getWidth() - nowWidth) / 2);
        // 更新头像的宽高
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        params.height = params.width = (int) nowWidth;
        child.setLayoutParams(params);
        return true;
    }
}
