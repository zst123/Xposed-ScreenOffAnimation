package com.zst.xposed.screenoffanimation.anim;

import com.zst.xposed.screenoffanimation.R;
import com.zst.xposed.screenoffanimation.helpers.ScreenshotUtil;
import com.zst.xposed.screenoffanimation.helpers.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.WindowManager;
import android.view.animation.Animation;
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
		
		final Animation anim = loadCRTAnimation(ctx, res);
		anim.setDuration(anim_speed);
		if (anim_speed > 350) {
			// Some weird bug causes the start offset of the
			// second animation in the set to be too fast..
			final float scale = (anim_speed) / 200;
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
				delayFinishAnimation(ctx, holder, 100);
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
