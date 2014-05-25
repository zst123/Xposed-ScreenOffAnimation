package com.zst.xposed.screenoffanimation.anim;

import com.zst.xposed.screenoffanimation.helpers.ScreenshotUtil;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class LGOptimusG extends ScreenOffAnim.Implementation {
	
	/**
	 * Based on the LG Optimus G
	 * A closing camera lens screen off animation
	 */
	Runnable mFinishAnimRunnable;
	@Override
	public void animate(final Context ctx, WindowManager wm, MethodHookParam param, Resources res) {
		final Bitmap screenshot = ScreenshotUtil.takeScreenshot(ctx);
		// FIXME find width and height without using screenshot
		
		final int larger_side = screenshot.getWidth() > screenshot.getHeight() ?
				screenshot.getWidth() : screenshot.getHeight();
		final int diameter = (int) (larger_side * 1.3f);
		final int delay = 10;

	    final Handler handler = new Handler(ctx.getMainLooper());
		final InverseCircleView view = new InverseCircleView(ctx, screenshot.getWidth(),
				screenshot.getHeight(), diameter, delay, anim_speed/2) {
			@Override
			public void onFinishAnimation() {
				handler.postDelayed(mFinishAnimRunnable, delay * 2);
			}
		};
		final ScreenOffAnim holder = new ScreenOffAnim(ctx, wm, param) {
			@Override
			public void animateView() {
				view.invalidate();
			}
		};
		
		mFinishAnimRunnable = new Runnable() {
			@Override
			public void run() {
				holder.finishAnimation();
			}
		};
		
		holder.show(view);
	}
	
	private abstract class InverseCircleView extends View {
		final int width;
		final int height;
		final int duration;
		final int delay;
		final int update_factor;
		
		Paint mPaint;
		float mDiameter;

		public InverseCircleView(Context context, int w, int h, float diametr, int delayy, int time) {
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

}
