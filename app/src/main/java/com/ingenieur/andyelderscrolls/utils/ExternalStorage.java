package com.ingenieur.andyelderscrolls.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// http://stackoverflow.com/a/15612964/304876

public class ExternalStorage
{
	public static final String TAG = "ExternalStorage: ";

	public static final String SD_CARD = "sdCard";
	public static final String EXTERNAL_SD_CARD = "externalSdCard";
	private static final String APPTAG = "ExternalStorage";

	/**
	 * @return True if the external storage is available. False otherwise.
	 */
	public static boolean isAvailable()
	{
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
		{
			return true;
		}
		return false;
	}

	public static String getSdCardPath()
	{
		return Environment.getExternalStorageDirectory().getPath() + "/";
	}

	/**
	 * @return True if the external storage is writable. False otherwise.
	 */
	public static boolean isWritable()
	{
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state))
		{
			return true;
		}
		return false;

	}

	/**
	 * @return A map of all storage locations available
	 */
	public static Map<String, File> getAllStorageLocations()
	{
		Map<String, File> map = new HashMap<String, File>(10);

		List<String> mMounts = new ArrayList<String>(10);
		List<String> mVold = new ArrayList<String>(10);
		mMounts.add("/mnt/sdcard");
		mVold.add("/mnt/sdcard");

		try
		{
			File mountFile = new File("/proc/mounts");
			if (mountFile.exists())
			{
				Scanner scanner = new Scanner(mountFile);
				while (scanner.hasNext())
				{
					String line = scanner.nextLine();

					//Log.d(APPTAG, TAG + "getAllStorageLocations /proc/mounts " + line);

					if (line.startsWith("/dev/block/vold/") || line.startsWith("/dev/fuse"))
					{
						String[] lineElements = line.split(" ");
						String element = lineElements[1];

						// don't add the default mount path
						// it's already in the list.
						if (!element.equals("/mnt/sdcard"))
							mMounts.add(element);
					}
				}
			}
			else
			{
				Log.d(APPTAG, TAG + "getAllStorageLocations no /proc/mounts");
			}
		}
		catch (Exception e)
		{
			Log.e(APPTAG, TAG + "getAllStorageLocations", e);
		}

		boolean voldExists;
		try
		{
			File voldFile = new File("/system/etc/vold.fstab");
			if (voldFile.exists())
			{
				voldExists = true;
				Scanner scanner = new Scanner(voldFile);
				while (scanner.hasNext())
				{
					String line = scanner.nextLine();

					//Log.d(APPTAG, TAG + "getAllStorageLocations vold.fstab " + line);

					if (line.startsWith("dev_mount"))
					{
						String[] lineElements = line.split(" ");
						String element = lineElements[2];

						if (element.contains(":"))
							element = element.substring(0, element.indexOf(":"));
						if (!element.equals("/mnt/sdcard"))
							mVold.add(element);
					}
				}
			}
			else
			{
				voldExists = false;
				Log.d(APPTAG, TAG + "getAllStorageLocations no /system/etc/vold.fstab");
			}
		}
		catch (Exception e)
		{
			voldExists = false;
			Log.e(APPTAG, TAG + "getAllStorageLocations", e);
		}

		if (voldExists)
		{
			for (int i = 0; i < mMounts.size(); i++)
			{
				String mount = mMounts.get(i);
				if (!mVold.contains(mount))
					mMounts.remove(i--);
			}
			mVold.clear();
		}

		List<String> mountHash = new ArrayList<String>(10);

		for (String mount : mMounts)
		{
			File root = new File(mount);
			if (root.exists()
					&& root.isDirectory()
					&& root.canRead())
			{
				File[] list = root.listFiles();
				String hash = "[";
				if (list != null)
				{
					for (File f : list)
					{
						hash += f.getName().hashCode() + ":" + f.length() + ", ";
					}
				}
				hash += "]";
				if (!mountHash.contains(hash))
				{
					String key = SD_CARD + "_" + map.size();
					if (map.size() == 0)
					{
						key = SD_CARD;
					}
					else if (map.size() == 1)
					{
						key = EXTERNAL_SD_CARD;
					}
					mountHash.add(hash);
					map.put(key, root);
				}
			}
		}

		mMounts.clear();

		if (map.isEmpty())
		{
			map.put(SD_CARD, Environment.getExternalStorageDirectory());
		}

		//Log.d(APPTAG, TAG + "getAllStorageLocations " + map);

		return map;
	}

	public static void copyAsset(Context context, String fromAssetPath, String toFolder) {
		AssetManager assetManager = context.getAssets();
		String[] files = null;
		try {
			files = assetManager.list(fromAssetPath);
		} catch (IOException e) {
			Log.e("tag", "Failed to get asset file list.", e);
		}
		if (files != null) for (String filename : files) {
			InputStream in = null;
			OutputStream out = null;
			try {
				in = assetManager.open(filename);
				File outFile = new File(toFolder, filename);
				if(!outFile.exists()) {
					out = new FileOutputStream(outFile);
					copyFile(in, out);
				}
			} catch(IOException e) {
				Log.e("tag", "Failed to copy asset file: " + filename, e);
			}
			finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						// NOOP
					}
				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						// NOOP
					}
				}
			}
		}
	}
	public static void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}
}