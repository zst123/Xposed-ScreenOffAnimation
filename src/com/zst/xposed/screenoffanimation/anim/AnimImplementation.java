package com.zst.xposed.screenoffanimation.anim;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.PowerManager;
import android.view.WindowManager;

import com.zst.xposed.screenoffanimation.Common;
import com.zst.xposed.screenoffanimation.MainXposed;
import com.zst.xposed.screenoffanimation.R;
import com.zst.xposed.screenoffanimation.helpers.Utils;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public abstract class AnimImplementation {
	/**
	 * Extend this class and create your own animation here
	 */
	public PowerManager.WakeLock mWakelock;
	public int anim_speed;
	
	public boolean supportsScreenOn() {
		return true;
	}
	
	public boolean supportsScreenOff(){
		return true;
	}
	
	public void animateScreenOffWithHandler(final Context ctx, final WindowManager wm,
			final MethodHookParam param, final Resources res) {
		MainXposed.mAnimationRunning = true;

		mWakelock = ((PowerManager) ctx.getSystemService(Context.POWER_SERVICE))
				.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Common.LOG_TAG);
		mWakelock.acquire(1000 * 20);
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
	 * TODO: Rename method to reduce confusion with screen on
	 */
	public void finish(Context ctx, final ScreenOffAnim holder, int delay) {
		if (delay <= 0) {
			holder.finishScreenOffAnim();
			if (mWakelock != null && mWakelock.isHeld()) {
				mWakelock.release();
			}
		} else {
			new Handler(ctx.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					holder.finishScreenOffAnim();
					if (mWakelock != null && mWakelock.isHeld()) {
						mWakelock.release();
					}
				}
			}, delay);
		}
	}
	
	public void animateScreenOnWithHandler(final Context ctx, final WindowManager wm,
			final Resources res) {
		new Handler(ctx.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				try {
					animateScreenOn(ctx, wm, res);
				} catch (Exception e) {
					// So we don't crash system.
					Utils.toast(ctx, res.getString(R.string.error_animating));
					Utils.log("Error in animateScreenOnWithHandler: " + getClass().getName(), e);
				}
			}
		});
	}
	
	public abstract void animateScreenOn(final Context ctx, final WindowManager wm,
			final Resources res) throws Exception;
}
