package com.zst.xposed.screenoffanimation.anim;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Matrix;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.zst.xposed.screenoffanimation.helpers.ScreenshotUtil;
import com.zst.xposed.screenoffanimation.widgets.AnimationEndListener;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class WP8 extends AnimImplementation {
	/**
	 * 3D Flip Animation.
	 * By ngxson (NUI)
	 */
	@Override
	public void animateScreenOff(final Context ctx, WindowManager wm, MethodHookParam param, Resources res) {
		final ImageView view = new ImageView(ctx);
		final Bitmap scrshot = ScreenshotUtil.takeScreenshot(ctx);
		view.setScaleType(ScaleType.FIT_XY);
		view.setImageBitmap(scrshot);
		view.setBackgroundColor(Color.BLACK);

		final AnimationSet anim = new AnimationSet(false);
		Animation a = new FlipAnimation(0, 90,
				(float)(scrshot.getHeight()/2),
				0);
		a.setInterpolator(new AccelerateInterpolator(2f));
		anim.addAnimation(a);
		anim.setDuration(anim_speed);
		final float scale = (anim_speed) / 200;
		if (scale >= 1) {
			anim.scaleCurrentDuration(scale);
		}
		anim.setFillAfter(true);
		anim.setStartOffset(100);

		final ScreenOffAnim holder = new ScreenOffAnim(ctx, wm, param) {
			@Override
			public void animateScreenOffView() {
				view.startAnimation(anim);
			}
		};

		anim.setAnimationListener(new AnimationEndListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				finish(ctx, holder, 100);
			}
		});

		holder.mFrame.setBackgroundColor(Color.BLACK);
		holder.showScreenOffView(view);
	}

	@Override
	public boolean supportsScreenOn() {
		return false;
	}

	@Override
	public void animateScreenOn(Context ctx, WindowManager wm, Resources res) throws Exception {
		throw new Exception("This class doesn't support screen on animation");
	}

	public class FlipAnimation extends Animation {

		//public final String TAG = this.getClass().getSimpleName();

		private final float mFromDegrees;
		private final float mToDegrees;
		private final float mCenterY;
		private float mRotateX;
		//private int width;
		private Camera mCamera;
		//private boolean out;

		public FlipAnimation(float fromDegrees, float toDegrees,float centerY, float centerX) {
			mFromDegrees = fromDegrees;
			mToDegrees = toDegrees;
			mRotateX = centerX;
			mCenterY = centerY;
			//this.out = out;
		}

		@Override
		public void initialize(int width, int height, int parentWidth, int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
			mCamera = new Camera();
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {

			final float fromDegrees = mFromDegrees;
			float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);

			final float centerY = mCenterY;
			final Camera camera = mCamera;

			final Matrix matrix = t.getMatrix();

			// This is where we determine the amount to translate by
			//int dirAmt = -1;
			//int amt = width;
			//int start = (int) (out ? amt : (width / 2));
			//float centerX = (width / 2 * interpolatedTime * dirAmt) + start;
			//float centerX = (float) width;

			camera.save();

			camera.rotateY(degrees);
			camera.getMatrix(matrix);

			camera.restore();

			// Adjusted the matrix translation to rotate off-center of the x-axis
			matrix.preTranslate(-mRotateX, -centerY);
			matrix.postTranslate(mRotateX, centerY);
		}
	}

}
