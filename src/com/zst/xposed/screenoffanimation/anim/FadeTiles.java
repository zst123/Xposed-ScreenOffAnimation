package com.zst.xposed.screenoffanimation.anim;

import java.util.ArrayList;

import com.zst.xposed.screenoffanimation.R;
import com.zst.xposed.screenoffanimation.helpers.ScreenshotUtil;
import com.zst.xposed.screenoffanimation.helpers.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class FadeTiles extends ScreenOffAnim.Implementation {
	/**
	 * Inspired by iOS Cydia Tweak SleepFX.
	 * 
	 * Thanks to these websites for some codes:
	 * http://androidattop.blogspot.sg/2012/05/splitting-image-into-smaller-chunks-in.html
	 * http://www.functionx.com/java/Lesson22.htm
	 */
	ScreenOffAnim mHolder;
	
	@Override
	public void animate(Context ctx, WindowManager wm, MethodHookParam param, Resources res) {
		final Bitmap screenie = ScreenshotUtil.takeScreenshot(ctx);
		final FadeTilesView view = makeSplitImageView(ctx, res, screenie, 40);
		mHolder = new ScreenOffAnim(ctx, wm, param) {
			@Override
			public void animateView() {
				view.startFade();
			}
		};
		mHolder.mFrame.setBackgroundColor(Color.BLACK);
		mHolder.show(view);
	}
	
	private FadeTilesView makeSplitImageView(Context c, Resources res,
			Bitmap bitmap, int sizeDp) {
		final int size = Utils.dp(sizeDp, c);
		
		int rows = (int) Math.ceil(bitmap.getHeight() / ((double) size)); // horizontal - y
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
		
		return new FadeTilesView(c, res, imageArray, rows, columns) {
			@Override
			public void onFinishAnimation() {
				mHolder.finishAnimation();
			}
		};
	}
	
	abstract class FadeTilesView extends LinearLayout {
		final DecelerateInterpolator sInterpolator = new DecelerateInterpolator();
		final ArrayList<ImageView> mViews;
		
		public FadeTilesView(Context ctx, Resources res, final Bitmap[][] array, int rows, int columns) {
			super(ctx);
			setOrientation(LinearLayout.VERTICAL);
			mViews = new ArrayList<ImageView>();
			
			final float offset_multiplier = (anim_speed * 3.75f) / (rows + columns);
			
			for (int r = 0; r < rows; r++) {
				LinearLayout row = new LinearLayout(ctx);
				row.setOrientation(LinearLayout.HORIZONTAL);
				for (int c = 0; c < columns; c++) {
					final Animation anim = Utils.loadAnimation(ctx, res, R.anim.scale_down);
					anim.setInterpolator(sInterpolator);
					anim.setDuration((int) (anim_speed * 1.75f));
					anim.setFillAfter(true);
					anim.setStartOffset((long) (offset_multiplier * (r + c)));
					if (r == rows - 1 && c == columns - 1) {
						anim.setAnimationListener(new Animation.AnimationListener() {
							@Override
							public void onAnimationEnd(Animation animation) {
								onFinishAnimation();
							}
							@Override public void onAnimationStart(Animation a) {}
							@Override public void onAnimationRepeat(Animation a) {}
						});
					}
					final ImageView view = new ImageView(ctx);
					view.setLayoutParams(new LinearLayout.LayoutParams(
							array[r][c].getWidth(),
							array[r][c].getHeight()));
					view.setScaleType(ScaleType.FIT_CENTER);
					view.setImageBitmap(array[r][c]);
					view.setAnimation(anim);
					
					mViews.add(view);
					row.addView(view);
				}
				addView(row);
			}
		}
		
		public void startFade() {
			for (ImageView iv : mViews) {
				iv.startAnimation(iv.getAnimation());
			}
		}
		
		public abstract void onFinishAnimation();
	}
	
}
