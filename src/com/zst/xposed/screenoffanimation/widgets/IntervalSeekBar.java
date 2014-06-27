package com.zst.xposed.screenoffanimation.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class IntervalSeekBar extends SeekBar {
	
	int mInterval = 10;
	int mRealMin = 100;
	int mRealMax = 2000;
	
	public IntervalSeekBar(Context context) {
		super(context);
	}
	
	public IntervalSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setRealProgress(int realProgress) {
		final int adjustedProgress = (realProgress - mRealMin) / mInterval;
		setProgress(adjustedProgress);
	}
	
	public int getRealProgress() {
		return (getProgress() * mInterval) + mRealMin;
	}
	
	public void setAttr(int realMax, int realMin, int interval) {
		mInterval = interval;
		mRealMin = realMin;
		mRealMax = realMax;
		setMax((realMax - realMin) / interval);
	}
}
