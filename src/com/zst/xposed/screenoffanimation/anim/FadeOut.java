package com.zst.xposed.screenoffanimation.anim;

import com.zst.xposed.screenoffanimation.widgets.AnimationEndListener;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class FadeOut extends AnimImplementation {
	@Override
	public void animateScreenOff(final Context ctx, WindowManager wm, MethodHookParam param, Resources res) {
		final View outline = new View(ctx);
		outline.setBackgroundColor(Color.BLACK);
		
		final Animation fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setDuration(anim_speed);
		
		final ScreenOffAnim holder = new ScreenOffAnim(ctx, wm, param) {
			@Override
			public void animateScreenOffView() {
				outline.startAnimation(fadeIn);
			}
		};
		fadeIn.setAnimationListener(new AnimationEndListener() {
			@Override
			public void onAnimationEnd(Animation a) {
				holder.mFrame.setBackgroundColor(Color.BLACK);
				finish(ctx, holder, 100);
			}
		});
		holder.showScreenOffView(outline);
	}

	@Override
	public void animateScreenOn(Context ctx, WindowManager wm, Resources res) throws Exception {
		final View outline = new View(ctx);
		outline.setBackgroundColor(Color.BLACK);
		
		final Animation fadeIn = new AlphaAnimation(1, 0);
		fadeIn.setDuration(anim_speed);
		
		final ScreenOnAnim holder = new ScreenOnAnim(ctx, wm) {
			@Override
			public void animateScreenOnView() {
				outline.startAnimation(fadeIn);
			}
		};
		fadeIn.setAnimationListener(new AnimationEndListener() {
			@Override
			public void onAnimationEnd(Animation a) {
				holder.finishScreenOnAnim();
			}
		});
		holder.showScreenOnView(outline);
	}
}
