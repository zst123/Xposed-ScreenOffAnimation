package com.zst.xposed.screenoffanimation.anim;

import com.zst.xposed.screenoffanimation.R;
import com.zst.xposed.screenoffanimation.helpers.ScreenshotUtil;
import com.zst.xposed.screenoffanimation.helpers.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class CRT extends ScreenOffAnim.Implementation {
	/**
	 * Electron Beam (CRT) Animation
	 */
	@Override
	public void animate(final Context ctx, WindowManager wm, MethodHookParam param, Resources res) {
		final ImageView view = new ImageView(ctx);
		view.setScaleType(ScaleType.FIT_XY);
		view.setImageBitmap(ScreenshotUtil.takeScreenshot(ctx));
		view.setBackgroundColor(Color.WHITE);
		
		final AlphaAnimation alpha = new AlphaAnimation(1.25f, 0) {
			@Override
			@SuppressWarnings("deprecation")
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				final float fromAlpha = 1;
				float newAlpha = fromAlpha + ((0 - fromAlpha) * interpolatedTime);
				if (newAlpha > 1)
					newAlpha = 1;
				if (Build.VERSION.SDK_INT >= 16) {
					view.setImageAlpha((int) (newAlpha * 255));
				} else {
					view.setAlpha((int) (newAlpha * 255));
				}
			}
		};		
		final AnimationSet anim = new AnimationSet(false);
		anim.addAnimation(loadCRTAnimation(ctx, res));
		anim.addAnimation(alpha);
		anim.setDuration(anim_speed);
		final float scale = (anim_speed) / 200;
		if (scale >= 1) {
			anim.scaleCurrentDuration(scale);
		}
		anim.setFillAfter(true);
		anim.setStartOffset(100);
		
		final ScreenOffAnim holder = new ScreenOffAnim(ctx, wm, param) {
			@Override
			public void animateView() {
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
		holder.show(view);
	}
	
	public Animation loadCRTAnimation(Context ctx, Resources res) {
		return Utils.loadAnimation(ctx, res, R.anim.crt_tv);
	}
}
