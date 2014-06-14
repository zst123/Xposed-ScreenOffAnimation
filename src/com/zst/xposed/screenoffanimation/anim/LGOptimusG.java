package com.zst.xposed.screenoffanimation.anim;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class LGOptimusG extends AnimImplementation {
	
	/**
	 * Based on the LG Optimus G
	 * A closing camera lens screen off animation
	 */
	Runnable mFinishAnimRunnable;
	@Override
	public void animateScreenOff(final Context ctx, WindowManager wm, MethodHookParam param, Resources res) {
		DisplayMetrics display = new DisplayMetrics();
		wm.getDefaultDisplay().getRealMetrics(display);
		
		final int larger_side = Math.max(display.widthPixels, display.heightPixels);
		final int diameter = (int) (larger_side * 1.3f);
		final int delay = 10;

	    final Handler handler = new Handler(ctx.getMainLooper());
		final InverseShrinkCircleView view = new InverseShrinkCircleView(ctx, display.widthPixels,
				display.heightPixels, diameter, delay, anim_speed/2) {
			@Override
			public void onFinishAnimation() {
				handler.postDelayed(mFinishAnimRunnable, delay * 2);
			}
		};
		final ScreenOffAnim holder = new ScreenOffAnim(ctx, wm, param) {
			@Override
			public void animateScreenOffView() {
				view.invalidate();
			}
		};
		
		mFinishAnimRunnable = new Runnable() {
			@Override
			public void run() {
				finish(ctx, holder, 0);
			}
		};
		
		holder.showScreenOffView(view);
	}
	
	private abstract class InverseShrinkCircleView extends View {
		final int width;
		final int height;
		final int duration;
		final int delay;
		final int update_factor;
		
		Paint mPaint;
		float mDiameter;

		public InverseShrinkCircleView(Context context, int w, int h, float diametr,
				int delayy, int time) {
			super(context);
			width = w;
			height = h;
			delay = delayy;
			duration = time;
			update_factor = (int) ((diametr / (duration / delay)) + 0.5f);

			mDiameter = diametr;

			mPaint = new Paint();
			mPaint.setStyle(Style.FILL);
			mPaint.setColor(Color.TRANSPARENT);
			mPaint.setAntiAlias(true);
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawColor(Color.BLACK);
			
			if (mDiameter <= 0) {
				invalidate();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
				onFinishAnimation();
				return;
			}
			
			canvas.drawCircle(width / 2, height / 2, mDiameter / 2, mPaint);
			
			mDiameter = mDiameter - update_factor;
			
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {}
			
			invalidate();
		}
		
		public abstract void onFinishAnimation();
	}

	@Override
	public void animateScreenOn(Context ctx, WindowManager wm, Resources res) throws Exception {
		DisplayMetrics display = new DisplayMetrics();
		wm.getDefaultDisplay().getRealMetrics(display);
		
		final int larger_side = Math.max(display.widthPixels, display.heightPixels);
		final int diameter = (int) (larger_side * 1.3f);
		final int delay = 10;
		
		final Handler handler = new Handler(ctx.getMainLooper());
		final InverseGrowCircleView view = new InverseGrowCircleView(ctx, display.widthPixels,
				display.heightPixels, diameter, delay, anim_speed / 2) {
			@Override
			public void onFinishAnimation() {
				handler.postDelayed(mFinishAnimRunnable, delay * 2);
			}
		};
		final ScreenOnAnim holder = new ScreenOnAnim(ctx, wm) {
			@Override
			public void animateScreenOnView() {
				view.invalidate();
			}
		};
		
		mFinishAnimRunnable = new Runnable() {
			@Override
			public void run() {
				holder.finishScreenOnAnim();
			}
		};
		holder.showScreenOnView(view);
	}
	
	private abstract class InverseGrowCircleView extends InverseShrinkCircleView {
		int currentDiameter;
		
		public InverseGrowCircleView(Context context, int w, int h,
				float diametr, int delayy, int time) {
			super(context, w, h, diametr, delayy, time);
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			if (mDiameter <= currentDiameter) {
				invalidate();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
				onFinishAnimation();
				return;
			}
			
			canvas.drawColor(Color.BLACK);
			canvas.drawCircle(width / 2, height / 2, currentDiameter / 2, mPaint);
			
			currentDiameter = currentDiameter + update_factor;
			
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {}
			
			invalidate();
		}
		
	}
}
