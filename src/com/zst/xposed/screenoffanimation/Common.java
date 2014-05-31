package com.zst.xposed.screenoffanimation;

import android.os.Build;

public class Common {
	// Main
	public static final String PACKAGE_THIS = Common.class.getPackage().getName();
	public final static String BROADCAST_REFRESH_SETTINGS = PACKAGE_THIS + ".REFRESH_SETTINGS";
	public final static String BROADCAST_TEST_ANIMATION = PACKAGE_THIS + ".TEST_ANIMATION";
	public final static String EXTRA_TEST_ANIMATION = "animation";
	
	// Animation Numbers
	public static class Anim {
		public final static int UNKNOWN = 0;
		public final static int FADE = 1;
		public final static int CRT = 2;
		public final static int CRT_VERTICAL = 3;
		public final static int SCALE = 4;
		public final static int TV_BURN = 5;
		public final static int LG_OPTIMUS_G = 6;
		public final static int FADE_TILES = 7;
	}
	
	// Preferences
	public static class Pref {
		public final static String PREF_MAIN = "pref_main";
		
		// Preference Keys
		public static class Key {
			public final static String ENABLED = "anim_enabled";
			public final static String EFFECT = "anim_effect";
			public final static String SPEED = "anim_speed";
		}
		
		// Preference Default Values
		public static class Def {
			public final static boolean ENABLED = false;
			public final static int EFFECT = Anim.FADE;
			public final static int SPEED = 300;
		}
	}
	
	// Others
	public final static String LOG_TAG = "Xposed-ScreenOffAnimation" + " (SDK" + Build.VERSION.SDK_INT + ")" + ": ";
}
