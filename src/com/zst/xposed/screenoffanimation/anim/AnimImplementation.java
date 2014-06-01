package com.zst.xposed.screenoffanimation.anim;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.view.WindowManager;

import com.zst.xposed.screenoffanimation.R;
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
		if (Utils.isSamsungSViewCoverClosed() && false) {
			handler.removeCallbacks(runnable);
			try {
				Utils.callOriginal(param);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Handlers are not executed when phone is sleeping.
		}
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
}
