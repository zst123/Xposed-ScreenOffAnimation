package com.zst.xposed.screenoffanimation.anim;

import com.zst.xposed.screenoffanimation.MainXposed;
import com.zst.xposed.screenoffanimation.helpers.Utils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
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
	private final Context mContext;
	
	FrameLayout mFrame;
	
	public ScreenOnAnim(Context ctx, WindowManager wm) {
		mWM = wm;
		mContext = ctx;
		
		mFrame = new FrameLayout(ctx);
		mFrame.setLayerType(View.LAYER_TYPE_HARDWARE, null);
	}
	
	@SuppressLint("NewApi")
	public void showScreenOnView(View animating_view) {
		MainXposed.mOnAnimationRunning = true;
		if (MainXposed.mScreenOnBlocker != null) {
			MainXposed.mScreenOnBlocker.destroy();//.hide();
		}
		
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
			new Handler(mContext.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					if (MainXposed.mScreenOnBlocker != null) {
						//MainXposed.mScreenOnBlocker.hide();
						MainXposed.mScreenOnBlocker.destroy();
					}
					try {
						animateScreenOnView();
					} catch (Exception e) {
						Utils.log("(ScreenOnAnim) Error animating view inside WindowManager", e);
					}
				}
			}, 700);
			//TODO: Custom delay
		} catch (Exception e) {
			Utils.log("(ScreenOnAnim) Error adding view to WindowManager", e);
		}
	}
	
	/** Do not call directly */
	public abstract void animateScreenOnView();
	
	public void finishScreenOnAnim() {
		MainXposed.mOnAnimationRunning = false;
		try {
			mWM.removeView(mFrame);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
