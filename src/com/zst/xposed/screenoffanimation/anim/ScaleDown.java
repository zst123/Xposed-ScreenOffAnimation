package com.zst.xposed.screenoffanimation.anim;

import com.zst.xposed.screenoffanimation.R;
import com.zst.xposed.screenoffanimation.helpers.ScreenshotUtil;
import com.zst.xposed.screenoffanimation.helpers.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class ScaleDown extends AnimImplementation {
	/**
	 * Scale Down Animation.
	 * Idea originally by XpLoDWilD
	 */
	@Override
	public void animateScreenOff(final Context ctx, WindowManager wm, MethodHookParam param, Resources res) {
		final ImageView view = new ImageView(ctx);
		view.setScaleType(ScaleType.FIT_XY);
		view.setImageBitmap(ScreenshotUtil.takeScreenshot(ctx));
		
		final Animation anim = Utils.loadAnimation(ctx, res, R.anim.scale_down);
		anim.setInterpolator(new AccelerateInterpolator());
		anim.setDuration(anim_speed);
		anim.setFillAfter(true);
		anim.setStartOffset(100);
		
		final ScreenOffAnim holder = new ScreenOffAnim(ctx, wm, param) {
			@Override
			public void animateScreenOffView() {
				view.startAnimation(anim);
			}
		};
		
		anim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				finish(ctx, holder, 100);
			}
			@Override
			public void onAnimationStart(Animation a) {}
			@Override
			public void onAnimationRepeat(Animation a) {}
		});
		holder.mFrame.setBackgroundColor(Color.BLACK);
		holder.showScreenOffView(view);
	}
	
	@Override
	public void animateScreenOn(final Context ctx, WindowManager wm, Resources res) {
		final View view = new View(ctx);
		view.setBackgroundColor(Color.BLACK);
		
		final Animation anim = Utils.loadAnimation(ctx, res, R.anim.scale_down);
		anim.setInterpolator(new AccelerateInterpolator());
		anim.setDuration(anim_speed);
		anim.setFillAfter(true);
		anim.setStartOffset(100);
		
		final ScreenOnAnim on_holder = new ScreenOnAnim(ctx, wm) {
			@Override
			public void animateScreenOnView() {
				view.startAnimation(anim);
			}
		};
		
		anim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				on_holder.finishScreenOnAnim();
			}
			@Override
			public void onAnimationStart(Animation a) {}
			@Override
			public void onAnimationRepeat(Animation a) {}
		});
		
		on_holder.showScreenOnView(view);
	}
}
