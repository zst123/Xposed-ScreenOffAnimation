package com.zst.xposed.screenoffanimation.anim;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.zst.xposed.screenoffanimation.helpers.ScreenshotUtil;
import com.zst.xposed.screenoffanimation.helpers.Utils;
import com.zst.xposed.screenoffanimation.widgets.AnimationEndListener;

import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class FlipTiles extends AnimImplementation {
	/**
	 * 3D Flip Tiles Animation.
	 * By ngxson (NUI)
	 */
	ScreenOffAnim mHolder;
	ScreenOnAnim mHolderOn;

	@Override
	public void animateScreenOff(Context ctx, WindowManager wm, MethodHookParam param, Resources res) {
		final Bitmap screenie = ScreenshotUtil.takeScreenshot(ctx);
		DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
		int width = metrics.widthPixels;
		final FadeTilesView view = makeSplitImageView(ctx, res, screenie, (width)/3);
		mHolder = new ScreenOffAnim(ctx, wm, param) {
			@Override
			public void animateScreenOffView() {
				view.startFade();
			}
		};
		mHolder.mFrame.setBackgroundColor(Color.BLACK);
		mHolder.showScreenOffView(view);
	}

	private FadeTilesView makeSplitImageView(final Context c, Resources res,
			Bitmap bitmap, int size) {
		//final int size = Utils.dp(sizeDp, c);

		int rows = (int) Math.ceil(bitmap.getHeight() / ((double) size)); // horizontal - y
		//int rows = 1;
		int columns = (int) Math.ceil(bitmap.getWidth() / ((double) size)); // vertical - x

		Bitmap[][] imageArray = new Bitmap[rows][columns];

		int y_position = 0; // y-coordinates of the pixel in the bitmap
		for(int iy = 0; iy < rows; iy++) {
			// for each row, create a new column
			int x_position = 0;
			for(int ix =0; ix < columns; ix++){
				// for each column, add image
				boolean xTooLarge = x_position + size > bitmap.getWidth();
				boolean yTooLarge = y_position + size > bitmap.getHeight();
				imageArray[iy][ix] = Bitmap.createBitmap(bitmap, x_position, y_position,
						xTooLarge ? (bitmap.getWidth() - x_position - 1) : size,
						yTooLarge ? (bitmap.getHeight() - y_position - 1) : size);
				x_position = x_position + size;
			}
			y_position = y_position + size;
		}

		return new FadeTilesView(c, res, imageArray, rows, columns, size) {
			@Override
			public void onFinishAnimation() {
				finish(c, mHolder, 0);
			}
		};
	}

	@Override
	public boolean supportsScreenOn() {
		return false;
	}

	@Override
	public void animateScreenOn(Context ctx, WindowManager wm, Resources res) throws Exception {
		throw new Exception("This class doesn't support screen on animation");
	}

	private FadeTilesView makeSplitBlackView(final Context c, DisplayMetrics display, Resources res, int sizeDp) {
		final int size = Utils.dp(sizeDp, c);

		int rows = (int) Math.ceil(display.heightPixels / ((double) size)); // horizontal - y
		int columns = (int) Math.ceil(display.widthPixels / ((double) size)); // vertical - x

		return new FadeTilesView(c, res, null, rows, columns, size) {
			@Override
			public void onFinishAnimation() {
				mHolderOn.finishScreenOnAnim();
			}
		};
	}

	abstract class FadeTilesView extends LinearLayout {
		final DecelerateInterpolator sInterpolator = new DecelerateInterpolator();
		final ArrayList<ImageView> mViews;

		public FadeTilesView(Context ctx, Resources res, final Bitmap[][] array, int rows, int columns, int size) {
			super(ctx);
			setOrientation(LinearLayout.VERTICAL);
			mViews = new ArrayList<ImageView>();

			final float offset_multiplier = (anim_speed * 3.75f) / (rows + columns);

			for (int r = 0; r < rows; r++) {
				LinearLayout row = new LinearLayout(ctx);
				row.setOrientation(LinearLayout.HORIZONTAL);
				for (int c = 0; c < columns; c++) {
					final Animation anim = new FlipAnimation(0, -90,
							(float)(array[r][c].getHeight()/2),
							(float)(array[r][c].getWidth()/2));
					//anim.setInterpolator(sInterpolator);
					anim.setDuration((int) (anim_speed * 1.75f));
					anim.setFillAfter(true);
					anim.setStartOffset((long) (offset_multiplier * (r + c)));
					if (r == rows - 1 && c == columns - 1) {
						anim.setAnimationListener(new AnimationEndListener() {
							@Override
							public void onAnimationEnd(Animation animation) {
								onFinishAnimation();
							}
						});
					}
					final ImageView view = new ImageView(ctx);
					if (array == null) {
						view.setLayoutParams(new LayoutParams(size, size));
						view.setImageResource(android.R.color.black);
					} else {
						view.setLayoutParams(new LayoutParams(
								array[r][c].getWidth(),
								array[r][c].getHeight()));
						view.setScaleType(ScaleType.FIT_CENTER);
						view.setImageBitmap(array[r][c]);
					}
					view.setAnimation(anim);
					
					mViews.add(view);
					row.addView(view);
				}
				addView(row);
			}
		}
		
		public void startFade() {
			for (ImageView iv : mViews) {
				if (iv.getAnimation() != null && !iv.getAnimation().hasStarted()) {
					iv.startAnimation(iv.getAnimation());
				}
			}
		}
		
		public abstract void onFinishAnimation();
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

			camera.rotateX(degrees);
			camera.getMatrix(matrix);

			camera.restore();

			// Adjusted the matrix translation to rotate off-center of the x-axis
			matrix.preTranslate(-mRotateX, -centerY);
			matrix.postTranslate(mRotateX, centerY);
		}
	}
	
}


