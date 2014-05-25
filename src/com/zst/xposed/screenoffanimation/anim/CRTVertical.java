package com.zst.xposed.screenoffanimation.anim;

import android.content.Context;
import android.content.res.Resources;
import android.view.animation.Animation;

import com.zst.xposed.screenoffanimation.R;
import com.zst.xposed.screenoffanimation.helpers.Utils;

public class CRTVertical extends CRT {
	@Override
	public Animation loadCRTAnimation(Context ctx, Resources res) {
		return Utils.loadAnimation(ctx, res, R.anim.crt_tv_vertical);
	}
}
