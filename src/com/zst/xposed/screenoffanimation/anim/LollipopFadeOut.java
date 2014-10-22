package com.zst.xposed.screenoffanimation.anim;

import com.zst.xposed.screenoffanimation.helpers.ScreenshotUtil;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.view.WindowManager;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class LollipopFadeOut extends AnimImplementation {
	/**
	 * Android 5.0 Lollipop Desaturate-Fade Effect
	 * 
	 * As seen here:
	 * http://www.androidpolice.com/2014/10/21/lollipop-feature
	 * -spotlight-the-new-screen-of-animation-actually-fades-to-black-and-white/
	 */
	
	LollipopAnimationView mView;
	@Override
	public void animateScreenOff(final Context ctx, WindowManager wm, MethodHookParam param,
			Resources res) {
		Bitmap screenshot = ScreenshotUtil.takeScreenshot(ctx);
		
		final ScreenOffAnim holder = new ScreenOffAnim(ctx, wm, param) {
			@Override
			public void animateScreenOffView() {
				mView.startAnim(anim_speed);
			}
		};
		mView = new LollipopAnimationView(ctx, screenshot) {
			@Override
			public void onFinishAnimation() {
				finish(ctx, holder, 300);
			}
			
		};
		holder.mFrame.setBackgroundColor(Color.BLACK);
		holder.showScreenOffView(mView);
	}
	
	abstract class LollipopAnimationView extends View {
		
		final Bitmap mImage;
		final Paint mPaint;
		final ColorMatrix mMatrix;
		
		ColorMatrixColorFilter colorFilter;
		Rect drawDst;
		int alpha;
		
		public LollipopAnimationView(Context context, Bitmap image) {
			super(context);
			
			mImage = image;
			
			mPaint = new Paint();
			mPaint.setFilterBitmap(false);
			mPaint.setDither(false);
			
			mMatrix = new ColorMatrix();
		}
		
		public void startAnim(int duration) {
			final ObjectAnimator animator = ObjectAnimator.ofFloat(this, "progress", 0f, 1.1f);
			animator.setDuration(duration);
			animator.start();
		}
		
		public void setProgress(final float progress) {
			if (progress < 1f) {
				float inverseProgress = 1f - progress;
				
				// Saturation
				// http://stackoverflow.com/questions/8381514/android-converting-color-image-to-grayscale
				float adjustedSaturation = Math.abs(1f - (progress / 40 * 100));
				// Fully desaturated when progress is 40%
				mMatrix.setSaturation(Math.min(adjustedSaturation, 0f));
				colorFilter = new ColorMatrixColorFilter(mMatrix);
				
				// Image Size
				int origWidth = mImage.getWidth();
				int origHeight = mImage.getHeight();
				
				float customPercentage = 1f - (progress * 0.10f); // 100% to 90%
				int newWidth = Math.round(origWidth * customPercentage);
				int newHeight = Math.round(origHeight * customPercentage);
				
				int borderX = (origWidth - newWidth) / 2;
				int borderY = (origHeight - newHeight) / 2;
				
				drawDst = new Rect(borderX, borderY, borderX + newWidth, borderY + newHeight);
				
				// Alpha
				alpha = Math.round(inverseProgress * 255);
			} else {
				alpha = 0;
			}
			
			invalidate();

			if (progress == 1.1f) {
				onFinishAnimation();
			}
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			if (alpha == 0) {
				canvas.drawColor(Color.BLACK);
			} else {
				mPaint.setAlpha(alpha);
				mPaint.setColorFilter(colorFilter);
				canvas.drawBitmap(mImage, null, drawDst, mPaint);
			}
		}
		
		public abstract void onFinishAnimation();
	}
	
	@Override
	public boolean supportsScreenOn() {
		return false;
	}
	
	@Override
	public void animateScreenOn(Context ctx, WindowManager wm, Resources res) throws Exception {
		throw new Exception("This class doesn't support screen on animation");
	}
}
