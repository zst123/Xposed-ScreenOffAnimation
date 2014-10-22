package com.zst.xposed.screenoffanimation.helpers;

import static android.view.WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;

public class ScreenOnBlocker {
	/**
	 * Adds a black layout to prevent the flicker that is shown on ROMs
	 * using the old PowerManagerService.
	 */
	// Hidden in API
	public static final int TYPE_SECURE_SYSTEM_OVERLAY = FIRST_SYSTEM_WINDOW + 15;
	
	@SuppressLint("InlinedApi")
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
	
	private WindowManager mWM;
	private final View mView;
	private final Context mContext;
	
	public ScreenOnBlocker(Context ctx, WindowManager wm) {
		mContext = ctx;
		mWM = wm;
		mView = new View(ctx);
	}
	
	public void start() {
		try {
			mView.setBackgroundColor(Color.BLUE);
			mWM.addView(mView, LAYOUT_PARAM);
			mContext.registerReceiver(mBM, new IntentFilter(Intent.ACTION_SCREEN_ON));
		} catch (IllegalArgumentException e) {
			// View already added
		} catch (Exception e) {
			Utils.logcat("(ScreenOnBlocker) Error adding view", e);
		}
	}
	
	public void hide() {
		try {
			mView.setBackgroundColor(Color.TRANSPARENT);
			mView.invalidate();
			mWM.updateViewLayout(mView, new WindowManager.LayoutParams(0, 0,
					TYPE_SECURE_SYSTEM_OVERLAY, 0x0, PixelFormat.TRANSLUCENT));
			
		} catch (Exception e) {
			// View already removed
		}
	}
	
	public void destroy() {
		try {
			mWM.removeViewImmediate(mView);
			mContext.unregisterReceiver(mBM);
		} catch (IllegalArgumentException e) {
			// View already removed
		} catch (Exception e) {
			Utils.logcat("(ScreenOnBlocker) Error removing view", e);
		}
	}
	
	final BroadcastReceiver mBM = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent i) {
			if (i.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				destroy();
			}
		}
	};
}