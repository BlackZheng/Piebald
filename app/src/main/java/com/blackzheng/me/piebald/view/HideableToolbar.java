package com.blackzheng.me.piebald.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;

/**
 * Created by BlackZheng on 2016/4/27.
 */
public class HideableToolbar extends Toolbar {

    ObjectAnimator animator;

    public HideableToolbar(Context context, AttributeSet attrs) { super(context, attrs); }
    private ObjectAnimator getAnimator(int translationY){
        animator = ObjectAnimator.ofFloat(this, "translationY", translationY);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(100);
        return animator;
    }
    public void hide(){
        getAnimator(-getHeight()).start();
    }
    public void show(){
        getAnimator(0).start();
    }

}
