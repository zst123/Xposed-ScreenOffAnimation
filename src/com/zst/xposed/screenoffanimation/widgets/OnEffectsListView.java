package com.zst.xposed.screenoffanimation.widgets;

import java.util.LinkedList;
import java.util.List;

import com.zst.xposed.screenoffanimation.Common;
import com.zst.xposed.screenoffanimation.R;

import android.content.Context;
import android.content.Intent;

public abstract class OnEffectsListView extends EffectsListView {
	public OnEffectsListView(Context context, int old_anim_id) {
		super(context, old_anim_id);
	}
	
	@Override
	void previewEffect(int effect_id) {
		Intent i = new Intent(Common.BROADCAST_TEST_ON_ANIMATION);
		i.putExtra(Common.EXTRA_TEST_ANIMATION, effect_id);
		getContext().sendBroadcast(i);
	}
	
	@Override
	public List<Effect> getList(Context context) {
		LinkedList<Effect> adapter = new LinkedList<Effect>();
		adapter.add(new Effect(context, R.string.anim_fade, Common.Anim.FADE));
		// adapter.add(new Effect(context, R.string.anim_crt, Common.Anim.CRT));
		// adapter.add(new Effect(context, R.string.anim_crt_vertical, Common.Anim.CRT_VERTICAL));
		adapter.add(new Effect(context, R.string.anim_scale_down, Common.Anim.SCALE));
		// adapter.add(new Effect(context, R.string.anim_tv_burn, Common.Anim.TV_BURN));
		adapter.add(new Effect(context, R.string.anim_lgog, Common.Anim.LG_OPTIMUS_G));
		adapter.add(new Effect(context, R.string.anim_fadetiles, Common.Anim.FADE_TILES));
		adapter.add(new Effect(context, R.string.anim_vertu_sig, Common.Anim.VERTU_SIG_TOUCH));
		// adapter.add(new Effect(context, R.string.anim_lollipop_fade_out, Common.Anim.LOLLIPOP_FADE_OUT));
		adapter.add(new Effect(context, R.string.anim_random, Common.Anim.RANDOM));
		return adapter;
	}
}