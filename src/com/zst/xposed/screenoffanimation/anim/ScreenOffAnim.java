package com.zst.xposed.screenoffanimation.anim;

import com.zst.xposed.screenoffanimation.MainXposed;
import com.zst.xposed.screenoffanimation.helpers.TouchConsumer;
import com.zst.xposed.screenoffanimation.helpers.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import static android.view.WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW;

public abstract class ScreenOffAnim {
	// Hidden in API
	public static final int TYPE_SECURE_SYSTEM_OVERLAY = FIRST_SYSTEM_WINDOW + 15;
	
	private final static WindowManager.LayoutParams LAYOUT_PARAM = new WindowManager.LayoutParams(
			WindowManager.LayoutParams.MATCH_PARENT,
			WindowManager.LayoutParams.MATCH_PARENT,
			TYPE_SECURE_SYSTEM_OVERLAY,
			0 | WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN |
				WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
				WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED |
				WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
			PixelFormat.TRANSLUCENT);
	
	private final MethodHookParam mMethodParam;
	private final WindowManager mWM;
	private final PowerManager mPM;
	private final Context mContext;
	
	FrameLayout mFrame;
	TouchConsumer mConsumer;
	
	public ScreenOffAnim(Context ctx, WindowManager wm, final MethodHookParam param) {
		mWM = wm;
		mContext = ctx;
		mMethodParam = param;
		mPM = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
		
		mFrame = new FrameLayout(ctx);
		mFrame.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		
		mConsumer = new TouchConsumer(ctx, wm);
		/* The system forces windows of TYPE_SECURE_SYSTEM_OVERLAY to be non-focusable
		 * or touchable. Thus, the user would be able to interact with their apps while
		 * the animation is playing. (Accidental touches such as putting the phone into
		 * the pocket while the animation is playing might be registered too).
		 * 
		 * We thus add another view of TYPE_SYSTEM_ERROR that is completely transparent
		 * to consume all the touches. We will remove them after the animation finishes.
		 */
	}
	
	public void show(View animating_view) {
		mConsumer.start();
		
		mFrame.removeAllViews();
		mFrame.addView(animating_view);
		
		try {
			mWM.addView(mFrame, LAYOUT_PARAM);
			animateView();
		} catch (Exception e) {
			Utils.log("(ScreenOffAnim) Error adding view to WindowManager", e);
		}
	}
	
	public abstract void animateView();
	
	public void finishAnimation() {
		try {
			Utils.callOriginal(mMethodParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (!mPM.isScreenOn() || mMethodParam == null) {
			new Handler(mContext.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					hide();
				}
			}, 100);
			// Give the system enough time to turn off the screen (~100ms)
		} else {
			/* (This only happens on ICS/JB)
			 * If user taps on the screen when the animation has just
			 * finished, some systems would register it & cancel the
			 * screen off event.
			 * 
			 * If screen is still on after calling native method, use
			 * PowerManager's public API to attempt screen of again.
			 */
			
			MainXposed.mDontAnimate = true;
			// set to not animate so the animation hook will not
			// be called again and go in an infinite loop
			
			mPM.goToSleep(SystemClock.uptimeMillis());
			
			new Handler(mContext.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					MainXposed.mDontAnimate = false;
					Utils.logcat("(ScreenOffAnim) Reattempt Screen Off (Removed)");
					hide();
				}
			}, 300);
			// More delay is needed because we are calling through a binder
		}
	}
	
	private void hide() {
		try {
			mWM.removeView(mFrame);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		mConsumer.stop();
	}
	
	/**
	 * Extend this class and create your own animation here
	 */
	public static abstract class Implementation {
		public int anim_speed;
		public void animateOnHandler(final Context ctx, final WindowManager wm,
				final MethodHookParam param, final Resources res) {
			new Handler(ctx.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					animate(ctx, wm, param, res);
				}
			});
		}
		
		public abstract void animate(final Context ctx, final WindowManager wm,
				final MethodHookParam param, final Resources res);
		
		/**
		 * Helper method to finish the animation after a delay
		 * TODO: Refactor and move
		 */
		public void delayFinishAnimation(Context ctx, final ScreenOffAnim holder, int delay) {
			new Handler(ctx.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					holder.finishAnimation();
				}
			}, delay);
		}
	}
}
