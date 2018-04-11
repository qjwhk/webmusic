package com.lierda.app.music.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;



public class AppUtils {

	/**
	 * @return
	 * @see {@linkplain #getMyCacheDir(String)}
	 */
	public static String getMyCacheDir() {
		return getMyCacheDir(null);
	}

	/**
	 * ????????ache????
	 *
	 * @param bucket
	 *            ???????????ucket = "/cache/" ??????"sdcard/linked-joyrun/cache"; ???bucket=""??ull,??????"sdcard/linked-joyrun/"
	 */
	public static String getMyCacheDir(String bucket) {
		String dir;

		// ?????????????
		if (bucket != null) {
			if (!bucket.equals("")) {
				if (!bucket.endsWith("/")) {
					bucket = bucket + "/";
				}
			}
		} else
			bucket = "";

		String joyrun_default = "/html/";

		if (FileUtils.isSDCardExist()) {
			dir = Environment.getExternalStorageDirectory().toString() + joyrun_default + bucket;
		} else {
			dir = Environment.getDownloadCacheDirectory().toString() + joyrun_default + bucket;
		}

		File f = new File(dir);
		if (!f.exists()) {
			f.mkdirs();
		}
		return dir;
	}

	/**
	 * dp?????x
	 */
	public static int dpToPx(Context context, float adius) {
		float density = context.getResources().getDisplayMetrics().density;
		return (int) (adius * density);
	}

	/**
	 * ???Activity?????????
	 *
	 * @param context
	 * @param activityClass
	 * @return
	 */
	public static boolean isActivityExist(Context context, Class<? extends Activity> activityClass) {
		try {
			context = context.getApplicationContext();
			Intent intent = new Intent(context, activityClass);
			ComponentName cmpName = intent.resolveActivity(context.getPackageManager());

			if (cmpName != null) { // ??????????????ctivity
				ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
				List<RunningTaskInfo> taskInfoList = am.getRunningTasks(70);

				for (RunningTaskInfo taskInfo : taskInfoList) {
					if (taskInfo.baseActivity.equals(cmpName)) { // ???????????????
						return true;
					}
				}
			}
		} catch (Exception e) {}

		return false;
	}

	/**
	 * ???Service???running
	 *
	 * @param context
	 * @param serviceClass
	 * @return
	 */
	public static boolean isServiceRunning(Context context, Class<? extends Service> serviceClass) {
		try {
			context = context.getApplicationContext();

			ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(2000);

			for (ActivityManager.RunningServiceInfo info : serviceList) {
				String name = info.service.getClassName();

				if (name != null && name.contains(serviceClass.getName())) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

}
