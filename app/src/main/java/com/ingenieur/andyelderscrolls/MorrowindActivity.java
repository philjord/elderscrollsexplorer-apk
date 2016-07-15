package com.ingenieur.andyelderscrolls;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ingenieur.andyelderscrolls.andyesexplorer.AndyESExplorerActivity;
import com.ingenieur.andyelderscrolls.utils.ExternalStorage;
import com.ingenieur.andyelderscrolls.utils.SopInterceptor;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.util.Map;

/**
 * Created by phil on 7/15/2016.
 */
public class MorrowindActivity extends Activity
{

	public final static String SELECTED_GAME = "SELECTED_GAME";
	public final static String ANDY_ROOT = "ANDY_ROOT";
	public final static String ROOT_FOLDER_NAME = "AndyElderScrolls";

	public static File andyRoot;
	private String gameDir;


	@Override
	public void onCreate(final Bundle state)
	{
		super.onCreate(state);

		// get system out to log
		PrintStream interceptor = new SopInterceptor(System.out, "sysout");
		System.setOut(interceptor);
		PrintStream interceptor2 = new SopInterceptor(System.err, "syserr");
		System.setErr(interceptor2);

		int hasWriteExternalStorage = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (hasWriteExternalStorage != PackageManager.PERMISSION_GRANTED)
		{
			requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
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
			gameDir = "Morrowind";

			Intent intent = new Intent(this, AndyESExplorerActivity.class);
			intent.putExtra(SELECTED_GAME, gameDir);
			intent.putExtra(ANDY_ROOT, andyRoot.getAbsolutePath());
			startActivity(intent);

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
					Toast.makeText(MorrowindActivity.this, "WRITE_EXTERNAL_STORAGE Denied", Toast.LENGTH_SHORT)
							.show();
				}
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}


}
