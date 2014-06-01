package com.zst.xposed.screenoffanimation.anim;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.zst.xposed.screenoffanimation.R;
import com.zst.xposed.screenoffanimation.helpers.ScreenshotUtil;
import com.zst.xposed.screenoffanimation.helpers.Utils;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public abstract class AnimImplementation {
	/**
	 * Extend this class and create your own animation here
	 */
	public int anim_speed;
	
	public void animateScreenOffWithHandler(final Context ctx, final WindowManager wm,
			final MethodHookParam param, final Resources res) {
		final Handler handler = new Handler(ctx.getMainLooper());
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					animateScreenOff(ctx, wm, param, res);
				} catch (Exception e) {
					// So we don't crash system.
					Utils.toast(ctx, res.getString(R.string.error_animating));
					Utils.log("Error in animateScreenOffWithHandler: " + getClass().getName(), e);
				}
			}
		};
		handler.post(runnable);
	}
	
	public abstract void animateScreenOff(final Context ctx, final WindowManager wm,
			final MethodHookParam param, final Resources res) throws Exception;
	
	/**
	 * Helper method to finish the animation after a delay
	 * TODO: Refactor and move
	 */
	public void delayFinishAnimation(Context ctx, final ScreenOffAnim holder, int delay) {
		new Handler(ctx.getMainLooper()).postDelayed(new Runnable() {
			@Override
			public void run() {
				holder.finishScreenOffAnim();
			}
		}, delay);
	}
	
	
	
	public void prepareScreenOnWithHandler(final Context ctx, final WindowManager wm,
			 final Resources res) {
		final Handler handler = new Handler(ctx.getMainLooper());
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					prepareScreenOn(ctx, wm, res);
				} catch (Exception e) {
					// So we don't crash system.
					Utils.toast(ctx, res.getString(R.string.error_animating));
					Utils.log("Error in animateScreenOnWithHandler: " + getClass().getName(), e);
				}
			}
		};
		handler.post(runnable);
	}
	
	public void animateScreenOnWithHandler(final Context ctx, final Resources res) {
		final Handler handler = new Handler(ctx.getMainLooper());
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					animateScreenOn();
				} catch (Exception e) {
					// So we don't crash system.
					Utils.toast(ctx, res.getString(R.string.error_animating));
					Utils.log("Error in animateScreenOnWithHandler: " + getClass().getName(), e);
				}
			}
		};
		handler.post(runnable);
	}
	
	ScreenOnAnim mHolderExtreme;
	void animateScreenOn() {
		mHolderExtreme.animateScreenOnView();
	}
	//abstract
	void prepareScreenOn(final Context ctx, WindowManager wm, Resources res) {
		final View outline = new View(ctx);
		//outline.setBackgroundColor(Color.BLACK);
		outline.setBackground(new android.graphics.drawable.BitmapDrawable(ScreenshotUtil.takeScreenshot(ctx)));
		outline.setRotation(180);
		
		final Animation fadeIn = new AlphaAnimation(1, 0);
		fadeIn.setDuration(anim_speed);
		fadeIn.setStartOffset(200);
		
		mHolderExtreme = new ScreenOnAnim(ctx, wm) {
			@Override
			public void animateScreenOnView() {
				outline.startAnimation(fadeIn);
			}
		};
		fadeIn.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationEnd(Animation a) {
				mHolderExtreme.finishScreenOnAnim();
			}
			@Override
			public void onAnimationStart(Animation a) {}
			@Override
			public void onAnimationRepeat(Animation a) {}
		});
		mHolderExtreme.showScreenOnView(outline);
	}
}
