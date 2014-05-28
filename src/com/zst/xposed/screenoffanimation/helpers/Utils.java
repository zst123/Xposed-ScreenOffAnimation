package com.zst.xposed.screenoffanimation.helpers;

import java.lang.reflect.InvocationTargetException;

import com.zst.xposed.screenoffanimation.Common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class Utils {
	
	/**
	 * Helper method to check if the ??NoUpdateLocked methods
	 * are valid events. (API 16 and above)
	 * @return
	 */
	public static boolean isValidSleepEvent(Object pms, long eventTime) {
		long mLastSleepTime = XposedHelpers.getLongField(pms, "mLastSleepTime");
		int mWakefulness = XposedHelpers.getIntField(pms, "mWakefulness");
		final int WAKEFULNESS_ASLEEP = 0; // From sources
		boolean mBootCompleted = XposedHelpers.getBooleanField(pms, "mBootCompleted");
		boolean mSystemReady = XposedHelpers.getBooleanField(pms, "mSystemReady");
		
		if (eventTime < mLastSleepTime || mWakefulness == WAKEFULNESS_ASLEEP
				|| !mBootCompleted || !mSystemReady) {
			return false;
		}
		return true;
	}
	
	public static void toast(Context ctx, String text) {
		Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
	}
	
	public static void dialog(Context ctx, String text) {
		new AlertDialog.Builder(ctx)
			.setMessage(text)
			.setPositiveButton(android.R.string.yes, null)
			.show();
	}
	
	public static int dp(int dp, Context c) {
		float scale = c.getResources().getDisplayMetrics().density;
		int pixel = (int) (dp * scale + 0.5f);
		return pixel;
	}
	
	/** Set background drawable based on the API */
	@SuppressWarnings("deprecation")
	public static void setBackgroundDrawable(View view, Drawable drawable) {
		if (Build.VERSION.SDK_INT >= 16) {
			view.setBackground(drawable);
		} else {
			view.setBackgroundDrawable(drawable);
		}
	}
	
	/**
	 * Load an animation from an external resources
	 * @return the animation
	 */
	public static Animation loadAnimation(Context ctx, Resources res, int id) {
		XmlResourceParser parser = res.getAnimation(id);
		return (Animation) XposedHelpers.callStaticMethod(AnimationUtils.class,
				"createAnimationFromXml", ctx, parser);
		// Creating XML from a parser is hidden in APIs.
	}
	
	/**
	 * Helper Method for <b>XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args)</b>
	 * @param param - MethodHookParam
	 */
	public static Object callOriginal(MethodHookParam param) throws NullPointerException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
	}
	
	/**
	 * Log an throwable + text to both Xposed Installer and LogCat
	 * @param s - text to log
	 * @param t - throwable to print
	 */
	public static void log(String s, Throwable t) {
		log(s);
		XposedBridge.log(t);
		t.printStackTrace();
	}
	
	/**
	 * Log text to both Xposed Installer and LogCat
	 * @param s - text to log
	 */
	public static void log(String s) {
		logcat(s);
		XposedBridge.log(Common.LOG_TAG + s);
	}
	
	/**
	 * Log text only to the logcat
	 */
	public static void logcat(String s) {
		Log.d("zst123", Common.LOG_TAG + s);
	}
	
	public static void logcat(String s, Throwable t) {
		Log.d("zst123", Common.LOG_TAG + s);
		t.printStackTrace();
	}
}
