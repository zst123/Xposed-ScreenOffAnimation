/*
 * Copyright (C) 2011 The Android Open Source Project
 * Contains modifications by zst123, Copyright (C) 2014
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zst.xposed.screenoffanimation.helpers;

import de.robv.android.xposed.XposedHelpers;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

/**
 * These methods were taken from 
 * com.android.systemui.screenshot.GlobalScreenshot
 */
public class ScreenshotUtil {
	
	public static Bitmap takeScreenshot(Context context) {
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();
		// We need to orient the screenshot correctly (and the Surface api seems to take screenshots
		// only in the natural orientation of the device :!)
		
		Matrix displayMatrix = new Matrix();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		display.getRealMetrics(displayMetrics);
		
		float[] dims = { displayMetrics.widthPixels, displayMetrics.heightPixels };
		float degrees = getDegreesForRotation(display.getRotation());
		boolean requiresRotation = (degrees > 0);
		if (requiresRotation) {
			// Get the dimensions of the device in its native orientation
			displayMatrix.reset();
			displayMatrix.preRotate(-degrees);
			displayMatrix.mapPoints(dims);
			dims[0] = Math.abs(dims[0]);
			dims[1] = Math.abs(dims[1]);
		}
		
		// Take the screenshot
		Bitmap screenBitmap;
		if (Build.VERSION.SDK_INT >= 18) {
			Class<?> surface_class = XposedHelpers.findClass("android.view.SurfaceControl", null);
			screenBitmap = (Bitmap) XposedHelpers.callStaticMethod(surface_class, "screenshot",
					(int) dims[0], (int) dims[1]);
		} else {
			screenBitmap = (Bitmap) XposedHelpers.callStaticMethod(Surface.class, "screenshot",
					(int) dims[0], (int) dims[1]);
		}
		
		if (screenBitmap == null) {
			return null;
		}
		
		if (requiresRotation) {
			// Rotate the screenshot to the current orientation
			Bitmap ss = Bitmap.createBitmap(displayMetrics.widthPixels,
					displayMetrics.heightPixels, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(ss);
			c.translate(ss.getWidth() / 2, ss.getHeight() / 2);
			c.rotate(degrees);
			c.translate(-dims[0] / 2, -dims[1] / 2);
			c.drawBitmap(screenBitmap, 0, 0, null);
			c.setBitmap(null);
			screenBitmap = ss;
		}
		
		// Optimizations
		screenBitmap.setHasAlpha(false);
		screenBitmap.prepareToDraw();
		
		return screenBitmap;
	}
	
	private static float getDegreesForRotation(int value) {
		switch (value) {
		case Surface.ROTATION_90:
			return 360f - 90f;
		case Surface.ROTATION_180:
			return 360f - 180f;
		case Surface.ROTATION_270:
			return 360f - 270f;
		}
		return 0f;
	}
}
