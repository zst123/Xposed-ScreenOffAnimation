package com.zst.xposed.screenoffanimation.widgets;

import java.util.LinkedList;
import java.util.List;

import com.zst.xposed.screenoffanimation.Common;
import com.zst.xposed.screenoffanimation.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

public abstract class EffectsCheckList extends ListView {
	List<Integer> mCheckedList;
	
	public EffectsCheckList(Context context, List<Integer> checkedList, boolean on) {
		super(context);
		mCheckedList = checkedList;
		
		final EffectsAdapter adapter = new EffectsAdapter(context);
		adapter.addAll(on ? getOnList(context) : getOffList(context));
		setAdapter(adapter);
	}
	
	public List<CheckEffect> getOffList(Context context) {
		LinkedList<CheckEffect> adapter = new LinkedList<CheckEffect>();
		adapter.add(new CheckEffect(context, R.string.anim_fade, Common.Anim.FADE));
		adapter.add(new CheckEffect(context, R.string.anim_crt, Common.Anim.CRT));
		adapter.add(new CheckEffect(context, R.string.anim_crt_vertical, Common.Anim.CRT_VERTICAL));
		adapter.add(new CheckEffect(context, R.string.anim_scale_down, Common.Anim.SCALE));
		adapter.add(new CheckEffect(context, R.string.anim_tv_burn, Common.Anim.TV_BURN));
		adapter.add(new CheckEffect(context, R.string.anim_lgog, Common.Anim.LG_OPTIMUS_G));
		adapter.add(new CheckEffect(context, R.string.anim_fadetiles, Common.Anim.FADE_TILES));
		adapter.add(new CheckEffect(context, R.string.anim_vertu_sig, Common.Anim.VERTU_SIG_TOUCH));
		adapter.add(new CheckEffect(context, R.string.anim_lollipop_fade_out, Common.Anim.LOLLIPOP_FADE_OUT));
		adapter.add(new CheckEffect(context, R.string.anim_scale_down_bottom, Common.Anim.SCALE_BOTTOM));
		adapter.add(new CheckEffect(context, R.string.anim_bounce, Common.Anim.BOUNCE));
		adapter.add(new CheckEffect(context, R.string.anim_3dflip, Common.Anim.FLIP));
		adapter.add(new CheckEffect(context, R.string.anim_wp8, Common.Anim.WP8));
		adapter.add(new CheckEffect(context, R.string.anim_flip_tiles, Common.Anim.FLIP_TILES));
		return adapter;
	}
	
	public List<CheckEffect> getOnList(Context context) {
		LinkedList<CheckEffect> adapter = new LinkedList<CheckEffect>();
		adapter.add(new CheckEffect(context, R.string.anim_fade, Common.Anim.FADE));
		// adapter.add(new CheckEffect(context, R.string.anim_crt, Common.Anim.CRT));
		// adapter.add(new CheckEffect(context, R.string.anim_crt_vertical, Common.Anim.CRT_VERTICAL));
		adapter.add(new CheckEffect(context, R.string.anim_scale_down, Common.Anim.SCALE));
		// adapter.add(new CheckEffect(context, R.string.anim_tv_burn, Common.Anim.TV_BURN));
		adapter.add(new CheckEffect(context, R.string.anim_lgog, Common.Anim.LG_OPTIMUS_G));
		adapter.add(new CheckEffect(context, R.string.anim_fadetiles, Common.Anim.FADE_TILES));
		adapter.add(new CheckEffect(context, R.string.anim_vertu_sig, Common.Anim.VERTU_SIG_TOUCH));
		// adapter.add(new CheckEffect(context, R.string.anim_lollipop_fade_out, Common.Anim.LOLLIPOP_FADE_OUT));
		// adapter.add(new CheckEffect(context, R.string.anim_scale_down_bottom, Common.Anim.SCALE_BOTTOM));
		// adapter.add(new CheckEffect(context, R.string.anim_bounce, Common.Anim.BOUNCE));
		// adapter.add(new CheckEffect(context, R.string.anim_3dflip, Common.Anim.FLIP));
		// adapter.add(new CheckEffect(context, R.string.anim_wp8, Common.Anim.WP8));
		// adapter.add(new CheckEffect(context, R.string.anim_flip_tiles, Common.Anim.FLIP_TILES));
		return adapter;
	}
	
	public abstract void onChangeCheck(List<Integer> list);
	
	private class EffectsAdapter extends ArrayAdapter<CheckEffect> {
		LayoutInflater mInflator;
		
		public EffectsAdapter(Context context) {
			super(context, 0);
			mInflator = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
		}
		
		public View getView(int position, View v, ViewGroup parent) {
			if (v == null) {
				v = new EffectView(getContext(), mInflator);
			}
			
			final EffectView convertView = (EffectView) v;
			
			final CheckEffect effect = getItem(position);
			if (effect != null) {
				final String title = effect.title;
				convertView.title.setText(title);
				convertView.checkbox.setChecked(effect.isChecked);
				convertView.checkbox.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						effect.isChecked = convertView.checkbox.isChecked();
						if (effect.isChecked && !mCheckedList.contains(effect.anim_id)) {
							mCheckedList.add(effect.anim_id);
						} else {
							mCheckedList.remove((Object) (effect.anim_id));
						}
						onChangeCheck(mCheckedList);
					}
				});
			}
			return convertView;
		}
		
		class EffectView extends FrameLayout {
			public TextView title;
			public CheckBox checkbox;
			public EffectView(Context context, LayoutInflater inflator) {
				super(context);
				inflator.inflate(R.layout.list_multi_effects, this);
				title = (TextView) findViewById(android.R.id.title);
				checkbox = (CheckBox) findViewById(android.R.id.checkbox);
			}
		}
	}
	
	class CheckEffect {
		public boolean isChecked;
		
		public final int anim_id;
		public final String title;
		
		public CheckEffect(Context c, int _title_id, int _id) {
			title = c.getResources().getString(_title_id);
			anim_id = _id;
			isChecked = mCheckedList.contains(_id);
		}
	}
}
