package com.zst.xposed.screenoffanimation.anim;

import com.zst.xposed.screenoffanimation.MainXposed;
import com.zst.xposed.screenoffanimation.R;
import com.zst.xposed.screenoffanimation.helpers.TouchConsumer;
import com.zst.xposed.screenoffanimation.helpers.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import static android.view.WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW;

public abstract class ScreenOnAnim {
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
	
	private final WindowManager mWM;
	private final PowerManager mPM;
	private final Context mContext;
	
	FrameLayout mFrame;
	
	public ScreenOnAnim(Context ctx, WindowManager wm) {
		mWM = wm;
		mContext = ctx;
		mPM = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
		
		mFrame = new FrameLayout(ctx);
		mFrame.setLayerType(View.LAYER_TYPE_HARDWARE, null);
	}
	
	public void startWake(View animating_view) {
		mFrame.removeAllViews();
		mFrame.addView(animating_view);
		
		try {
			if (Build.VERSION.SDK_INT >= 19) {
				/* On Kitkat onwards, MATCH_PARENT by default doesn't fill
				 * up the entire screen even though it has the permissions
				 * to overlay everything.
				 * We find the height and width and set them accordingly.
				 * Furthermore, Gravity.CENTER seems to be slightly above 
				 * the actual center. Thus, showing a few pixels of the 
				 * nav bar. Setting to TOP | LEFT fixes that. */
				DisplayMetrics displayMetrics = new DisplayMetrics();
				mWM.getDefaultDisplay().getRealMetrics(displayMetrics);
				
				WindowManager.LayoutParams params = new WindowManager.LayoutParams();
				params.copyFrom(LAYOUT_PARAM);
				params.width = displayMetrics.widthPixels;
				params.height = displayMetrics.heightPixels;
				params.gravity = Gravity.TOP | Gravity.LEFT;
				
				mWM.addView(mFrame, params);
			} else {
				mWM.addView(mFrame, LAYOUT_PARAM);
			}
			animateScreenOn();
		} catch (Exception e) {
			Utils.log("(ScreenOffAnim) Error adding view to WindowManager", e);
		}
		
		try {
			//Utils.callOriginal(mMethodParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public abstract void animateScreenOn();
	
	public void finishWakeAnimation() {
		try {
			mWM.removeView(mFrame);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Extend this class and create your own animation here
	 */
	public static abstract class Implementation {
		public int anim_speed;
		public void animateWakeOnHandler(final Context ctx, final WindowManager wm,
				 final Resources res) {
			final PowerManager mPM = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
			final Handler handler = new Handler(ctx.getMainLooper());
			final Runnable runnable = new Runnable() {
				@Override
				public void run() {
					if (!mPM.isScreenOn()) {
						handler.postDelayed(this, 250);
						Utils.logcat("WAKEUP: Screen not on yet. ");
					}
					
					try {
						animateWAKEEEE(ctx, wm, res);
					} catch (Exception e) {
						// So we don't crash system.
						Utils.toast(ctx, res.getString(R.string.error_animating));
						Utils.log("Error in animateOnHandler: " + getClass().getName(), e);
					}
				}
			};
			handler.postDelayed(runnable, 100);
		}
		
		public void animateWAKEEEE(final Context ctx, WindowManager wm, Resources res) {
			final View outline = new View(ctx);
			outline.setBackgroundColor(Color.BLACK);
			
			final Animation fadeIn = new AlphaAnimation(1, 0);
			fadeIn.setDuration(anim_speed);
			
			final ScreenOnAnim holder = new ScreenOnAnim(ctx, wm) {
				@Override
				public void animateScreenOn() {
					outline.startAnimation(fadeIn);
				}
			};
			fadeIn.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationEnd(Animation a) {
					holder.finishWakeAnimation();
				}
				@Override
				public void onAnimationStart(Animation a) {}
				@Override
				public void onAnimationRepeat(Animation a) {}
			});
			holder.startWake(outline);
		}
		public abstract void animateWake(final Context ctx, final WindowManager wm,
				final MethodHookParam param, final Resources res) throws Exception;
	}
}
