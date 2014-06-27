package com.zst.xposed.screenoffanimation.anim;

import com.zst.xposed.screenoffanimation.helpers.ScreenshotUtil;
import com.zst.xposed.screenoffanimation.widgets.AnimationEndListener;

import android.graphics.Shader;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class TVBurnIn extends AnimImplementation {
	/**
	 * TV Burn In Animation
	 */
	@Override
	public void animateScreenOff(final Context ctx, WindowManager wm, MethodHookParam param, Resources res) {
		BitmapDrawable screenshot = drawable(ctx, ScreenshotUtil.takeScreenshot(ctx));
		
		final ImageView view = new ImageView(ctx);
		view.setScaleType(ScaleType.FIT_XY);
		view.setImageDrawable(vignette(ctx, whiten(screenshot).getBitmap()) );
		
		final Animation anim = new AlphaAnimation(1, 0);
		anim.setDuration(anim_speed);
		anim.setFillAfter(true);
		anim.setStartOffset(50);
		
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
	
	public BitmapDrawable drawable(Context c, Bitmap bmp) {
		return new BitmapDrawable(c.getResources(), bmp);
	}
	
	public BitmapDrawable whiten(BitmapDrawable d) {
		d.setColorFilter(new LightingColorFilter(Color.WHITE, Color.WHITE));
		return d;
	}
	
	// http://stackoverflow.com/questions/10658828/vignette-in-android
	public BitmapDrawable vignette(Context c, Bitmap bitmap) {
		final Bitmap image = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		// http://stackoverflow.com/questions/13119582/android-immutable-bitmap-crash-error
		
		float radius = image.getWidth() * 0.8f;
		final RadialGradient gradient = new RadialGradient(
				image.getWidth() / 2,
				image.getHeight() / 2,
				radius,
				Color.TRANSPARENT, 0xFF000000,
				Shader.TileMode.CLAMP);
		
		final Rect rect = new Rect(0, 0, image.getWidth(), image.getHeight());
		
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.BLACK);
		paint.setShader(gradient);
		
		final Canvas canvas = new Canvas(image);
		canvas.drawARGB(1, 0, 0, 0);
		canvas.drawRect(new RectF(rect), paint);
		
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(image, rect, rect, paint);
		return drawable(c, image);
	}

	@Override
	public boolean supportsScreenOn() {
		return false;
	}
	
	@Override
	public void animateScreenOn(Context ctx, WindowManager wm, Resources res)
			throws Exception {
		throw new Exception("This class doesn't support screen on animation");
	}
}
