package com.zst.xposed.screenoffanimation.anim;

import com.zst.xposed.screenoffanimation.helpers.Utils;
import com.zst.xposed.screenoffanimation.widgets.AnimationEndListener;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class VertuSigTouch extends AnimImplementation {
	
	/**
	 * Vertu Signature Touch Animation
	 * 
	 * Similar to CRT animation but in a "V-shaped"
	 * https://www.youtube.com/watch?v=TDuLS7SPe8A&feature=youtu.be&t=2m37s
	 */
	
	@Override
	public void animateScreenOff(final Context c, WindowManager wm, MethodHookParam param, Resources res) {
		anim_speed = anim_speed + 100;
		// adjust the anim_speed to correspond to other animations
		
		final DisplayMetrics display = new DisplayMetrics();
		wm.getDefaultDisplay().getRealMetrics(display);
		
		final PortionView upperPortion = new PortionView(c, display.widthPixels, display.heightPixels, true);
		final PortionView lowerPortion = new PortionView(c, display.widthPixels, display.heightPixels, false);
		
		final FrameLayout layout = new FrameLayout(c);
		layout.addView(upperPortion);
		layout.addView(lowerPortion);
		
		final Animation anim = new Animation() {
			int adjusted_height = (display.heightPixels / 2) + (display.widthPixels / 16);
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				float newHeight = adjusted_height + ((0 - adjusted_height) * interpolatedTime);
				upperPortion.setY(0 - newHeight);
				lowerPortion.setY(newHeight);
			}
		};
		anim.setDuration(anim_speed);
		anim.setInterpolator(new AccelerateInterpolator());
		
		final ScreenOffAnim holder = new ScreenOffAnim(c, wm, param) {
			@Override
			public void animateScreenOffView() {
				layout.startAnimation(anim);
			}
		};
		
		anim.setAnimationListener(new AnimationEndListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				upperPortion.setY(0);
				lowerPortion.setY(0);
				
				final Animation anim1 = new Animation() {
					@Override
					protected void applyTransformation(float interpolatedTime, Transformation t) {
						float newHeight = (upperPortion.mGap * interpolatedTime);
						upperPortion.setY(newHeight);
						lowerPortion.setY(-newHeight);
					}
				};
				anim1.setAnimationListener(new AnimationEndListener() {
					@Override
					public void onAnimationEnd(Animation animation) {
						finish(c, holder, 100);
					}
				});
				anim1.setDuration((long) (anim_speed * 0.8f));
				anim1.setInterpolator(new AccelerateDecelerateInterpolator());
				layout.startAnimation(anim1);
			}
		});
		holder.showScreenOffView(layout);
	}
	
	
	@Override
	public void animateScreenOn(final Context c, WindowManager wm, Resources res) throws Exception {
		anim_speed = anim_speed + 100;
		
		final DisplayMetrics display = new DisplayMetrics();
		wm.getDefaultDisplay().getRealMetrics(display);
		
		final PortionView upperPortion = new PortionView(c, display.widthPixels, display.heightPixels, true);
		final PortionView lowerPortion = new PortionView(c, display.widthPixels, display.heightPixels, false);
		
		final FrameLayout layoutOn = new FrameLayout(c);
		layoutOn.addView(upperPortion);
		layoutOn.addView(lowerPortion);
		
		final Animation anim1 = new Animation() {
			int adjusted_height = (display.heightPixels / 2) + (display.widthPixels / 16) + upperPortion.mGap;
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				float newHeight = adjusted_height * interpolatedTime;
				upperPortion.setY(0 - newHeight);
				lowerPortion.setY(newHeight);
			}
		};
		anim1.setDuration(anim_speed);
		anim1.setInterpolator(new AccelerateDecelerateInterpolator());
		
		final ScreenOnAnim holder = new ScreenOnAnim(c, wm) {
			@Override
			public void animateScreenOnView() {
				layoutOn.startAnimation(anim1);
			}
		};
		anim1.setAnimationListener(new AnimationEndListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				holder.finishScreenOnAnim();
			}
		});
		
		holder.showScreenOnView(layoutOn);
	}
	
	
	
	public class PortionView extends View {
		final Paint mPaint;
		final boolean mTop;
		final int mGap;
		
		Path mPath;
		
		public PortionView(Context context, int screenWidth, int screenHeight, boolean top) {
			super(context);
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setStyle(Paint.Style.FILL);
			mPaint.setColor(Color.BLACK);
			
			mTop = top;
			mGap = Utils.dp(8, getContext());
			mPath = makePortion(top, screenWidth, screenHeight,
					(screenWidth / 8), mGap);
		}
		
		@Override
		public void setY(float y) {
			super.setY(mTop ? (y - mGap) : (y + mGap));
			// undo the offset so what we see is correct.
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawPath(mPath, mPaint);
		}
		
		// http://stackoverflow.com/questions/15150701/
		private Path makePortion(boolean facingDown, int screenWidth, int screenHeight,
				int triangleHeight, int gap) {
			
			final Path path = new Path();
			final int mid_width = screenWidth / 2;
			final int mid_height_top = (screenHeight - triangleHeight) / 2 + gap;
			final int mid_height_bottom = (screenHeight - triangleHeight) / 2 - gap;
			// add gap to the mid_height to offset the final bit.
			
			if (facingDown) {
				path.moveTo(0, -gap); // top-left screen
				path.lineTo(screenWidth, -gap); // top-right screen
				path.lineTo(screenWidth, mid_height_top); // corner-right (where triangle starts)
				path.lineTo(mid_width, mid_height_top + triangleHeight - (gap / 2)); // center point of triangle
				path.lineTo(0, mid_height_top);// corner-left (where triangle starts)
				
			} else {
				path.moveTo(0, screenHeight + gap); // bottom-left screen
				path.lineTo(screenWidth, screenHeight + gap); // bottom-right screen
				path.lineTo(screenWidth, mid_height_bottom); // corner-right (where triangle starts)
				path.lineTo(mid_width, mid_height_bottom + triangleHeight + (gap / 2)); // center point of triangle
				path.lineTo(0, mid_height_bottom);// corner-left (where triangle starts)
			}
			return path;
		}
	}
	
}
