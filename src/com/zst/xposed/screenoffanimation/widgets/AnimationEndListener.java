package com.zst.xposed.screenoffanimation.widgets;

import android.view.animation.Animation;

public abstract class AnimationEndListener implements Animation.AnimationListener {
	@Override
	public void onAnimationRepeat(Animation animation) {
		// ignore
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// ignore
	}
	
}
