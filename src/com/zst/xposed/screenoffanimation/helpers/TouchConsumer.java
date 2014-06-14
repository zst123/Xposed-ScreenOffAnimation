package com.zst.xposed.screenoffanimation.helpers;


import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class TouchConsumer {
	/**
	 * Adds a layout to consume all the touches to prevent the user
	 * from interacting with the apps while the animations is playing
	 */
	private final static WindowManager.LayoutParams LAYOUT_PARAM = new WindowManager.LayoutParams(
			WindowManager.LayoutParams.MATCH_PARENT,
			WindowManager.LayoutParams.MATCH_PARENT,
			WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
			0 | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
				WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED |
				WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
			PixelFormat.TRANSLUCENT);
	
	private final WindowManager mWM;
	private final Handler mHandler;
	private final View mView;
	
	public TouchConsumer(Context ctx, WindowManager wm) {
		mWM = wm;
		mHandler = new Handler(ctx.getMainLooper());
		
		mView = new View(ctx);
		mView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Consume the touches
				/* XXX Remove Debugging
				switch (event.getAction()){
				case MotionEvent.ACTION_CANCEL:
					Utils.logcat("(TouchConsumer) MotionEvent.ACTION_CANCEL");
					break;
				case MotionEvent.ACTION_UP:
					Utils.logcat("(TouchConsumer) MotionEvent.ACTION_UP");
					break;
				case MotionEvent.ACTION_DOWN:
					Utils.logcat("(TouchConsumer) MotionEvent.ACTION_Down");
					break;
				case MotionEvent.ACTION_OUTSIDE:
					Utils.logcat("(TouchConsumer) MotionEvent.ACTION_OUTSIDE");
					break;
				case MotionEvent.ACTION_MOVE:
					Utils.logcat("(TouchConsumer) MotionEvent.ACTION_MOVE");
					break;
				default:
					Utils.logcat("(TouchConsumer) MotionEvent OTHER");
					break;
				}
				*/
				return true;
			}
		});
	}
	
	public void start() {
		try {
			mWM.addView(mView, LAYOUT_PARAM);
		} catch (IllegalArgumentException e) {
			// View already added
		} catch (Exception e) {
			Utils.logcat("(TouchConsumer) Error adding view", e);
		}
		mHandler.postDelayed(sStopRunnable, 2000);
	}
	public void stop() {
		mHandler.removeCallbacks(sStopRunnable);
		try {
			mWM.removeView(mView);
		} catch (IllegalArgumentException e) {
			// View already removed
		} catch (Exception e) {
			Utils.logcat("(TouchConsumer) Error removing view", e);
		}
	}
	
	final Runnable sStopRunnable = new Runnable() {
		@Override
		public void run() {
			stop();
		}
	};
}