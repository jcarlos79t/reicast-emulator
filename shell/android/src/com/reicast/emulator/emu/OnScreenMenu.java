package com.reicast.emulator.emu;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.reicast.emulator.MainActivity;
import com.reicast.emulator.R;
import com.reicast.emulator.config.ConfigureFragment;

public class OnScreenMenu {

	private GL2JNIActivity mContext;
	private SharedPreferences prefs;
	LayoutParams params;
	private int frameskip;
	private boolean widescreen;
	private boolean limitframes;
	private boolean audiodisabled;

	private File sdcard = Environment.getExternalStorageDirectory();
	private String home_directory = sdcard + "/dc";

	public OnScreenMenu(Context mContext, SharedPreferences prefs) {
		if (mContext instanceof GL2JNIActivity) {
			this.mContext = (GL2JNIActivity) mContext;
		}
		if (prefs != null) {
			this.prefs = prefs;
			home_directory = prefs.getString("home_directory", home_directory);
			widescreen = ConfigureFragment.widescreen;
			frameskip = ConfigureFragment.frameskip;
		}
	}

	public PopupWindow createPopup() {
		final PopupWindow popUp = new PopupWindow(mContext);

		int p = getPixelsFromDp(60, mContext);
		params = new LayoutParams(p, p);

		LinearLayout hlay = new LinearLayout(mContext);

		hlay.setOrientation(LinearLayout.HORIZONTAL);

		hlay.addView(addbut(R.drawable.close, new OnClickListener() {
			public void onClick(View v) {
				Intent inte = new Intent(mContext, MainActivity.class);
				mContext.startActivity(inte);
				((Activity) mContext).finish();
			}
		}), params);

		if (prefs.getBoolean("debug_profling_tools", false)) {

			hlay.addView(addbut(R.drawable.disk_unknown, new OnClickListener() {
				public void onClick(View v) {
					displayDebugPopup(popUp);
					popUp.dismiss();
				}
			}), params);

		}
		hlay.addView(addbut(R.drawable.vmu_swap, new OnClickListener() {
			public void onClick(View v) {
				JNIdc.vmuSwap();
				popUp.dismiss();
			}
		}), params);

		hlay.addView(addbut(R.drawable.config, new OnClickListener() {
			public void onClick(View v) {
				displayConfigPopup(popUp);
				popUp.dismiss();
			}
		}), params);

		// layout.addView(hlay,params);
		popUp.setContentView(hlay);
		return popUp;
	}

	void displayDebugPopup(final PopupWindow popUp) {
		final PopupWindow popUpDebug = new PopupWindow(mContext);

		int p = getPixelsFromDp(60, mContext);
		LayoutParams debugParams = new LayoutParams(p, p);

		LinearLayout hlay = new LinearLayout(mContext);

		hlay.setOrientation(LinearLayout.HORIZONTAL);
		
		hlay.addView(addbut(R.drawable.close, new OnClickListener() {
			public void onClick(View v) {
				popUpDebug.dismiss();
			}
		}), debugParams);

		hlay.addView(addbut(R.drawable.clear_cache, new OnClickListener() {
			public void onClick(View v) {
				JNIdc.send(0, 0); // Killing texture cache
				popUpDebug.dismiss();
			}
		}), debugParams);

		hlay.addView(addbut(R.drawable.profiler, new OnClickListener() {
			public void onClick(View v) {
				JNIdc.send(1, 3000); // sample_Start(param);
				popUpDebug.dismiss();
			}
		}), debugParams);

		hlay.addView(addbut(R.drawable.profiler, new OnClickListener() {
			public void onClick(View v) {
				JNIdc.send(1, 0); // sample_Start(param);
				popUpDebug.dismiss();
			}
		}), debugParams);

		// hlay.addView(addbut(R.drawable.disk_unknown, new
		// OnClickListener() {
		// public void onClick(View v) {
		// JNIdc.send(0, 1); //settings.pvr.ta_skip
		// popUp.dismiss();
		// }
		// }), debugParams);

		hlay.addView(addbut(R.drawable.print_stats, new OnClickListener() {
			public void onClick(View v) {
				JNIdc.send(0, 2);
				popUpDebug.dismiss(); // print_stats=true;
			}
		}), debugParams);

		hlay.addView(addbut(R.drawable.up, new OnClickListener() {
			public void onClick(View v) {
				popUpDebug.dismiss();
				mContext.displayPopUp(popUp);
			}
		}), debugParams);

		popUpDebug.setContentView(hlay);
		mContext.displayDebug(popUpDebug);
	}

	void displayConfigPopup(final PopupWindow popUp) {
		final PopupWindow popUpConfig = new PopupWindow(mContext);

		int p = getPixelsFromDp(60, mContext);
		LayoutParams configParams = new LayoutParams(p, p);

		LinearLayout hlay = new LinearLayout(mContext);

		hlay.setOrientation(LinearLayout.HORIZONTAL);

		hlay.addView(addbut(R.drawable.close, new OnClickListener() {
			public void onClick(View v) {
				popUpConfig.dismiss();
			}
		}), configParams);

		View fullscreen;
		if (!widescreen) {
			fullscreen = addbut(R.drawable.widescreen, new OnClickListener() {
				public void onClick(View v) {
					JNIdc.widescreen(1);
					popUpConfig.dismiss();
					widescreen = true;
				}
			});
		} else {
			fullscreen = addbut(R.drawable.normal_view, new OnClickListener() {
				public void onClick(View v) {
					JNIdc.widescreen(0);
					popUpConfig.dismiss();
					widescreen = false;
				}
			});
		}
		hlay.addView(fullscreen, params);

		View frames_up = addbut(R.drawable.frames_up, new OnClickListener() {
			public void onClick(View v) {
				frameskip++;
				JNIdc.frameskip(frameskip);
				popUpConfig.dismiss();
				displayConfigPopup(popUp);

			}
		});
		hlay.addView(frames_up, params);

		if (frameskip >= 5) {
			frames_up.setEnabled(false);
		}
		View frames_down = addbut(R.drawable.frames_down,
				new OnClickListener() {
					public void onClick(View v) {
						frameskip--;
						JNIdc.frameskip(frameskip);
						popUpConfig.dismiss();
						displayConfigPopup(popUp);
					}
				});
		hlay.addView(frames_down, params);
		if (frameskip <= 0) {
			frames_down.setEnabled(false);
		}

		View framelimit;
		if (!limitframes) {
			framelimit = addbut(R.drawable.frames_limit_on,
					new OnClickListener() {
						public void onClick(View v) {
							JNIdc.limitfps(1);
							popUpConfig.dismiss();
							limitframes = true;
						}
					});
		} else {
			framelimit = addbut(R.drawable.frames_limit_off,
					new OnClickListener() {
						public void onClick(View v) {
							JNIdc.limitfps(0);
							popUpConfig.dismiss();
							limitframes = false;
						}
					});
		}
		hlay.addView(framelimit, params);

		if (prefs.getBoolean("sound_enabled", true)) {
			View audiosetting;
			if (!audiodisabled) {
				audiosetting = addbut(R.drawable.mute_sound,
						new OnClickListener() {
					public void onClick(View v) {
						mContext.mView.audioDisable(true);
						popUpConfig.dismiss();
						audiodisabled = true;
					}
				});
			} else {
				audiosetting = addbut(R.drawable.enable_sound,
						new OnClickListener() {
					public void onClick(View v) {
						mContext.mView.audioDisable(false);
						popUpConfig.dismiss();
						audiodisabled = false;
					}
				});
			}
			hlay.addView(audiosetting, params);
		}

		hlay.addView(addbut(R.drawable.up, new OnClickListener() {
			public void onClick(View v) {
				popUpConfig.dismiss();
				mContext.displayPopUp(popUp);
			}
		}), configParams);

		popUpConfig.setContentView(hlay);
		mContext.displayConfig(popUpConfig);
	}

	public static int getPixelsFromDp(float dps, Context context) {
		return (int) (dps * context.getResources().getDisplayMetrics().density + 0.5f);
	}

	View addbut(int x, OnClickListener ocl) {
		ImageButton but = new ImageButton(mContext);

		but.setImageResource(x);
		but.setScaleType(ScaleType.FIT_CENTER);
		but.setOnClickListener(ocl);

		return but;
	}
}
