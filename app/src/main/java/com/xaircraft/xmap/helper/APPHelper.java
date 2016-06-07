package com.xaircraft.xmap.helper;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

public class APPHelper extends Application {

	private static APPHelper INSTANCE = null;
	private static CrashHandler CRASH_HANDLER = null;

	public APPHelper() {
		INSTANCE = this;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		//
		CRASH_HANDLER = CrashHandler.getInstance();
		CRASH_HANDLER.init(getApplicationContext());
	}

	public static Context getInstance() {
		if (null == INSTANCE) {
			INSTANCE = new APPHelper();

		}
		// INSTANCE.getResources();
		return INSTANCE;
	}

	public static long getTaskTime() throws ParseException {
		Date currentTime = new Date();
		return currentTime.getTime();
	}

	private static int execRootCmdSilent() {
		try {
			Process localProcess = Runtime.getRuntime().exec("su");
			Object localObject = localProcess.getOutputStream();
			DataOutputStream localDataOutputStream = new DataOutputStream(
					(OutputStream) localObject);

		} catch (Exception localException) {
			localException.printStackTrace();
		}
		return 0;
	}

	ArrayList<Activity> list = new ArrayList<Activity>();

	/**
		 * 
		 */
	public void removeActivity(Activity a) {
		list.remove(a);
	}

	/**
		 * 
		 */
	public void addActivity(Activity a) {
		list.add(a);
	}

	/**
		 * 
		 */
	public void finishActivity() {
		for (Activity activity : list) {
			if (null != activity) {
				activity.finish();
			}
		}
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}
