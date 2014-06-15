package com.zst.xposed.screenoffanimation;

import com.zst.xposed.screenoffanimation.Common.Pref;
import com.zst.xposed.screenoffanimation.widgets.EffectsListView;
import com.zst.xposed.screenoffanimation.widgets.OnEffectsListView;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {
	final static int SPEED_INTERVAL = 10;
	final static int SPEED_MIN = 100;
	final static int SPEED_MAX = 2000;
	
	final static int MENU_RESET_SETTINGS = 0x100;
	
	private SharedPreferences mPref;
	private int mCurrentAnim;
	private int mOnCurrentAnim;
	
	Switch mSwitchEnabled;
	ViewGroup mOffSettingsLayout;
	TextView mTextSpeed;
	SeekBar mSeekSpeed;
	
	Switch mOnSwitchEnabled;
	ViewGroup mOnSettingsLayout;
	TextView mOnTextSpeed;
	SeekBar mOnSeekSpeed;
	
	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mPref = getSharedPreferences(Pref.PREF_MAIN, MODE_WORLD_READABLE);
		setup();
		loadPref();
	}
	
	/**
	 * This will be overriden in MainXposed.java when the module is active
	 */
	private boolean isXposedRunning() {
		return false;
	}
	
	private void setup() {
		mOffSettingsLayout = (ViewGroup) findViewById(R.id.layout_off_anim);
		
		mSwitchEnabled = (Switch) findViewById(R.id.switch_enable);
		mSwitchEnabled.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mPref.edit().putBoolean(Pref.Key.ENABLED, isChecked).commit();
				updateSettings();
			}
		});
		
		mTextSpeed = (TextView) findViewById(R.id.tV_speed_value);
		mSeekSpeed = (SeekBar) findViewById(R.id.seekBar_speed);
		mSeekSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				updateSettings();
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (!fromUser) return;
				
				int adjusted_progress = (progress * SPEED_INTERVAL) + SPEED_MIN;
				mPref.edit().putInt(Pref.Key.SPEED, adjusted_progress).commit();
				mTextSpeed.setText(adjusted_progress + " ms");
			}
		});
		mSeekSpeed.setMax((SPEED_MAX - SPEED_MIN) / SPEED_INTERVAL);
		
		findViewById(R.id.select_anim_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final AlertDialog dialog = new
						  AlertDialog.Builder(MainActivity.this).create();
				dialog.setView(new EffectsListView(MainActivity.this, mCurrentAnim) {
					@Override
					public void onSelectEffect(int animId) {
						mPref.edit().putInt(Pref.Key.EFFECT, animId).commit();
						dialog.dismiss();
						updateSettings();
					}
				});
				dialog.show();
			}
		});
		
		mOnSettingsLayout = (ViewGroup) findViewById(R.id.layout_on_anim);
		
		mOnSwitchEnabled = (Switch) findViewById(R.id.switch_wake_enable);
		mOnSwitchEnabled.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mPref.edit().putBoolean(Pref.Key.ON_ENABLED, isChecked).commit();
				updateSettings();
			}
		});
		
		mOnTextSpeed = (TextView) findViewById(R.id.tV_wake_speed_value);
		mOnSeekSpeed = (SeekBar) findViewById(R.id.seekBar_wake_speed);
		mOnSeekSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				updateSettings();
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (!fromUser) return;
				
				int adjusted_progress = (progress * SPEED_INTERVAL) + SPEED_MIN;
				mPref.edit().putInt(Pref.Key.ON_SPEED, adjusted_progress).commit();
				mTextSpeed.setText(adjusted_progress + " ms");
			}
		});
		mOnSeekSpeed.setMax((SPEED_MAX - SPEED_MIN) / SPEED_INTERVAL);
		
		findViewById(R.id.select_wake_anim_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final AlertDialog dialog = new
						  AlertDialog.Builder(MainActivity.this).create();
				dialog.setView(new OnEffectsListView(MainActivity.this, mOnCurrentAnim) {
					@Override
					public void onSelectEffect(int animId) {
						mPref.edit().putInt(Pref.Key.ON_EFFECT, animId).commit();
						dialog.dismiss();
						updateSettings();
					}
				});
				dialog.show();
			}
		});
		
		findViewById(R.id.preview_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				previewEffect(false);
			}
		});
		
		findViewById(R.id.preview_wake_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				previewEffect(true);
			}
		});
		
		findViewById(R.id.xposed_inactive).setVisibility(isXposedRunning() ? View.GONE : View.VISIBLE);
	}
	
	private void previewEffect(boolean on) {
		Intent i = new Intent(on ? Common.BROADCAST_TEST_ON_ANIMATION : Common.BROADCAST_TEST_OFF_ANIMATION);
		i.putExtra(Common.EXTRA_TEST_ANIMATION, on ? mOnCurrentAnim : mCurrentAnim);
		sendBroadcast(i);
	}
	
	private void updateSettings() {
		sendBroadcast(new Intent(Common.BROADCAST_REFRESH_SETTINGS));
		loadPref();
	}
	
	private void loadPref() {
		final boolean enabled = mPref.getBoolean(Pref.Key.ENABLED, Pref.Def.ENABLED);
		final int speed = mPref.getInt(Pref.Key.SPEED, Pref.Def.SPEED);
		final int adjusted_speed = (speed - SPEED_MIN) / SPEED_INTERVAL;
		
		mCurrentAnim = mPref.getInt(Pref.Key.EFFECT, Pref.Def.EFFECT);

		mSwitchEnabled.setChecked(enabled);
		mOffSettingsLayout.setVisibility(enabled ? View.VISIBLE : View.GONE);
		mSeekSpeed.setProgress(adjusted_speed);
		mTextSpeed.setText(speed + " ms");
		
		
		
		final boolean on_enabled = mPref.getBoolean(Pref.Key.ON_ENABLED, Pref.Def.ENABLED);
		final int on_speed = mPref.getInt(Pref.Key.ON_SPEED, Pref.Def.SPEED);
		final int on_adjusted_speed = (on_speed - SPEED_MIN) / SPEED_INTERVAL;
		
		mOnCurrentAnim = mPref.getInt(Pref.Key.ON_EFFECT, Pref.Def.EFFECT);

		mOnSwitchEnabled.setChecked(on_enabled);
		mOnSettingsLayout.setVisibility(on_enabled ? View.VISIBLE : View.GONE);
		mOnSeekSpeed.setProgress(on_adjusted_speed);
		mOnTextSpeed.setText(on_speed + " ms");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_RESET_SETTINGS, 0, R.string.menu_reset_settings_title);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_RESET_SETTINGS:
			DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mPref.edit().clear().commit();
					updateSettings();
					dialog.dismiss();
				}
			};
			new AlertDialog.Builder(this)
					.setMessage(getResources().getString(R.string.menu_reset_settings_dialog))
					.setPositiveButton(android.R.string.yes, click)
					.setNegativeButton(android.R.string.no, null)
					.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}