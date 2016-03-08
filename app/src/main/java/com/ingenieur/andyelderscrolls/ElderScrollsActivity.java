package com.ingenieur.andyelderscrolls;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ingenieur.andyelderscrolls.nifdisplay.NifDisplayActivity;
import com.ingenieur.andyelderscrolls.utils.ExternalStorage;
import com.ingenieur.andyelderscrolls.utils.SopInterceptor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.DataFormatException;

import esmmanager.common.PluginException;
import esmmanager.common.data.plugin.PluginRecord;
import esmmanager.loader.ESMManager;
import esmmanager.loader.IESMManager;

import static android.widget.Toast.LENGTH_LONG;

public class ElderScrollsActivity extends Activity
{
	public final static String SELECTED_GAME = "SELECTED_GAME";
	public final static String ANDY_ROOT = "ANDY_ROOT";
	public final static String ROOT_FOLDER_NAME = "AndyElderScrolls";

	public static File andyRoot;
	private String gameDir;


	ArrayList<String> listItems = new ArrayList<String>();
	ArrayAdapter<String> adapter;

	@Override
	public void onCreate(final Bundle state)
	{
		super.onCreate(state);

		// get system out to log
		PrintStream interceptor = new SopInterceptor(System.out, "sysout");
		System.setOut(interceptor);
		PrintStream interceptor2 = new SopInterceptor(System.err, "syserr");
		System.setErr(interceptor2);

		int hasWriteExternalStorage = checkSelfPermission(permission.WRITE_EXTERNAL_STORAGE);
		if (hasWriteExternalStorage != PackageManager.PERMISSION_GRANTED)
		{
			requestPermissions(new String[]{permission.WRITE_EXTERNAL_STORAGE},
					REQUEST_CODE_ASK_PERMISSIONS);
		}
		else
		{
			permissionGranted();
		}
	}


	private void permissionGranted()
	{
		setContentView(R.layout.main);

		final ListView gameSelectorList = (ListView) findViewById(R.id.gameSelectView);

		gameSelectorList.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int which, long id)
			{
				gameDir = (String) gameSelectorList.getItemAtPosition(which);
			}
		});

		Map<String, File> externalLocations = ExternalStorage.getAllStorageLocations();
		File sdCard = externalLocations.get(ExternalStorage.SD_CARD);
		File externalSdCard = externalLocations.get(ExternalStorage.EXTERNAL_SD_CARD);

		if (sdCard != null && andyRoot == null)
		{
			File andyRootTest = new File(sdCard, ROOT_FOLDER_NAME);
			if (andyRootTest.exists())
			{
				andyRoot = andyRootTest;
			}
		}
		if (externalSdCard != null && andyRoot == null)
		{
			File andyRootTest = new File(externalSdCard, ROOT_FOLDER_NAME);
			if (andyRootTest.exists())
			{
				andyRoot = andyRootTest;
			}
		}


		if (andyRoot != null)
		{
			File[] files = andyRoot.listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File file)
				{
					return file.isDirectory();
				}
			});
			String[] fileList = new String[files.length];
			int i = 0;
			for (File dir : files)
			{
				fileList[i++] = dir.getName();
			}
			gameSelectorList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileList)
			{
				@Override
				public View getView(int pos, View view, ViewGroup parent)
				{
					view = super.getView(pos, view, parent);
					((TextView) view).setSingleLine(true);
					return view;
				}
			});
		}


		// now await clicky clicky
	}

	public void showNifDisplay(View view)
	{
		if (gameDir != null)
		{
			Intent intent = new Intent(this, NifDisplayActivity.class);
			intent.putExtra(SELECTED_GAME, gameDir);
			intent.putExtra(ANDY_ROOT, andyRoot.getAbsolutePath());
			startActivity(intent);
		}
		else
		{
			Toast.makeText(ElderScrollsActivity.this, "Please select a game root folder", Toast.LENGTH_SHORT)
					.show();
		}
	}

	public void showKfDisplay(View view)
	{
		if (gameDir != null)
		{
			//Intent intent = new Intent(this, NifDisplayActivity.class);
			//intent.putExtra(SELECTED_GAME, gameDir);
			//intent.putExtra(ANDY_ROOT, andyRoot);
			//startActivity(intent);
		}
		else
		{
			Toast.makeText(ElderScrollsActivity.this, "Please select a game root folder", Toast.LENGTH_SHORT)
					.show();
		}
	}

	public void showJBulletDisplay(View view)
	{
		if (gameDir != null)
		{
			//Intent intent = new Intent(this, NifDisplayActivity.class);
			//intent.putExtra(SELECTED_GAME, gameDir);
			//intent.putExtra(ANDY_ROOT, andyRoot);
			//startActivity(intent);
		}
		else
		{
			Toast.makeText(ElderScrollsActivity.this, "Please select a game root folder", Toast.LENGTH_SHORT)
					.show();
		}
	}

	public void showESExplorer(View view)
	{
		if (gameDir != null)
		{
			//Intent intent = new Intent(this, NifDisplayActivity.class);
			//intent.putExtra(SELECTED_GAME, gameDir);
			//intent.putExtra(ANDY_ROOT, andyRoot);
			//startActivity(intent);
		}
		else
		{
			Toast.makeText(ElderScrollsActivity.this, "Please select a game root folder", Toast.LENGTH_SHORT)
					.show();
		}
	}

	final private int REQUEST_CODE_ASK_PERMISSIONS = 123;//just has to match from request to response below

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		switch (requestCode)
		{
			case REQUEST_CODE_ASK_PERMISSIONS:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					// Permission Granted
					permissionGranted();
				}
				else
				{
					// Permission Denied
					Toast.makeText(ElderScrollsActivity.this, "WRITE_EXTERNAL_STORAGE Denied", Toast.LENGTH_SHORT)
							.show();
				}
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}


	void readExternalStoragePublicDcimESE()
	{
		File path = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DCIM);
		File file = new File(path, "/ese/morrowind/morrowind.esm");


		IESMManager esmManager = ESMManager.getESMManager(file.getAbsolutePath());
		try
		{
			PluginRecord pr = esmManager.getWRLD(0);

			Toast.makeText(ElderScrollsActivity.this, "ESMManger loaded up", LENGTH_LONG).show();
		}
		catch (DataFormatException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (PluginException e)
		{
			e.printStackTrace();
		}

	}


}
