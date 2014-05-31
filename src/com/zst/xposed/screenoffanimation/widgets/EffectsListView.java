package com.zst.xposed.screenoffanimation.widgets;

import com.zst.xposed.screenoffanimation.Common;
import com.zst.xposed.screenoffanimation.R;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

public abstract class EffectsListView extends ListView {
	final int mCurrentAnimId;
	
	public EffectsListView(Context context, int old_anim_id) {
		super(context);
		mCurrentAnimId = old_anim_id;
		final EffectsAdapter adapter = new EffectsAdapter(context);
		adapter.add(new Effect(context, R.string.anim_fade, Common.Anim.FADE));
		adapter.add(new Effect(context, R.string.anim_crt, Common.Anim.CRT));
		adapter.add(new Effect(context, R.string.anim_crt_vertical, Common.Anim.CRT_VERTICAL));
		adapter.add(new Effect(context, R.string.anim_scale_down, Common.Anim.SCALE));
		adapter.add(new Effect(context, R.string.anim_tv_burn, Common.Anim.TV_BURN));
		adapter.add(new Effect(context, R.string.anim_lgog, Common.Anim.LG_OPTIMUS_G));
		adapter.add(new Effect(context, R.string.anim_fadetiles, Common.Anim.FADE_TILES));
		setAdapter(adapter);
	}
	public abstract void onSelectEffect(int animId);
	
	private void previewEffect(int effect_id) {
		Intent i = new Intent(Common.BROADCAST_TEST_ANIMATION);
		i.putExtra(Common.EXTRA_TEST_ANIMATION, effect_id);
		getContext().sendBroadcast(i);
	}
	
	private class EffectsAdapter extends ArrayAdapter<Effect> {
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
			
			EffectView convertView = (EffectView) v;
			
			final Effect effect = getItem(position);
			if (effect != null) {
				final String title = effect.title;
				final boolean isSelected = effect.anim_id == mCurrentAnimId;
				convertView.title.setText(!isSelected ? title :
					Html.fromHtml("<b><u>" + title + "</u></b>"));
				convertView.button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						previewEffect(effect.anim_id);
					}
				});
				convertView.bg.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onSelectEffect(effect.anim_id);
					}
				});
			}
			return convertView;
		}
		
		class EffectView extends FrameLayout {
			public TextView title;
			public Button button;
			public View bg;
			public EffectView(Context context, LayoutInflater inflator) {
				super(context);
				inflator.inflate(R.layout.list_effects, this);
				title = (TextView) findViewById(android.R.id.title);
				button = (Button) findViewById(android.R.id.button1);
				bg = findViewById(android.R.id.widget_frame);
			}
		}
	}
	
	private class Effect {
		public final int anim_id;
		public final String title;
		public Effect(Context c, int _title_id, int _id) {
			title = c.getResources().getString(_title_id);
			anim_id = _id;
		}
	}
}