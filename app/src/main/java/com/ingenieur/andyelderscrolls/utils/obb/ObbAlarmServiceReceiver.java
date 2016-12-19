package com.ingenieur.andyelderscrolls.utils.obb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;

/**
 * Created by phil on 12/18/2016.
 */

public class ObbAlarmServiceReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			DownloaderClientMarshaller.startDownloadServiceIfRequired(context,
					intent, ObbDownloaderService.class);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}
}
