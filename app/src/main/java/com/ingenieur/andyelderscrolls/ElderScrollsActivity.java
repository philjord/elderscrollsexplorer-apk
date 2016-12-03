package com.ingenieur.andyelderscrolls;

import android.Manifest.permission;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ingenieur.andyelderscrolls.andyesexplorer.AndyESExplorerActivity;
import com.ingenieur.andyelderscrolls.jbullet.JBulletActivity;
import com.ingenieur.andyelderscrolls.kfdisplay.KfDisplayActivity;
import com.ingenieur.andyelderscrolls.nifdisplay.NifDisplayActivity;
import com.ingenieur.andyelderscrolls.utils.FileChooser;
import com.ingenieur.andyelderscrolls.utils.SopInterceptor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

import esmmanager.common.PluginException;
import esmmanager.common.data.plugin.PluginRecord;
import esmmanager.loader.ESMManager;
import esmmanager.loader.IESMManager;

import static android.widget.Toast.LENGTH_LONG;

public class ElderScrollsActivity extends Activity
{

	public static final String PREFS_NAME = "ElderScrollsActivityDefault";

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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
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
		else
		{
			permissionGranted();
		}
	}


	private void permissionGranted()
	{
		setContentView(R.layout.main);




		String extStore = System.getenv("EXTERNAL_STORAGE");

		// Restore preferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String prefRoot = settings.getString("andyRoot", extStore);

		chooserStartFolder = new File(prefRoot);

		showRootFolderChooser();
	}


	private File chooserStartFolder;
	private File folderClicked;
	private FileChooser rootFileChooser;

	private void showRootFolderChooser()
	{
		// in case of dismissal without any clicks
		folderClicked = chooserStartFolder;

		rootFileChooser = new FileChooser(ElderScrollsActivity.this, chooserStartFolder)
		{
			@Override
			public void onDismiss(DialogInterface dialogInterface)
			{
				setRootFolder(folderClicked);
			}
		};
		rootFileChooser.setFileListener(
				new FileChooser.FileSelectedListener()
				{
					@Override
					public void fileSelected(final File file)
					{//ignore
					}

					public void folderSelected(final File file)
					{
						System.out.println("floder= " + file);
						folderClicked = file;
					}
				}
		);

		// show file chooser
		this.runOnUiThread(new Runnable()
						   {
							   public void run()
							   {
								   rootFileChooser.showDialog();
							   }
						   }

		);
	}


	private void setRootFolder(File root)
	{
	/*	String extStore = System.getenv("EXTERNAL_STORAGE");
		File f_exts = new File(extStore);
		String secStore = System.getenv("SECONDARY_STORAGE");
		File f_secs = new File(secStore);
		//extStore = "/storage/emulated/legacy"
		//secStore = "/storage/extSdCarcd"
		if (f_exts != null && andyRoot == null)
		{
			File andyRootTest = new File(f_exts, ROOT_FOLDER_NAME);
			if (andyRootTest.exists())
			{
				andyRoot = andyRootTest;
			}
		}
		if (f_secs != null && andyRoot == null)
		{
			File andyRootTest = new File(f_secs, ROOT_FOLDER_NAME);
			if (andyRootTest.exists())
			{
				andyRoot = andyRootTest;
			}
		}*/

		System.out.println("root= " + root);

		andyRoot = root;

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("andyRoot", andyRoot.getAbsolutePath());
		editor.commit();


		/*Map<String, File> externalLocations = ExternalStorage.getAllStorageLocations();
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
		}*/


		//ok so the externalsd card and the root file name should
		// both gathered and saved by the file selector interface thing I have
		// showing /storage/extSdCard/AndyElderScrolls as location which is perfect!

		final ListView gameSelectorList = (ListView) findViewById(R.id.gameSelectView);

		gameSelectorList.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int which, long id)
			{
				gameDir = (String) gameSelectorList.getItemAtPosition(which);
			}
		});

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
			Intent intent = new Intent(this, KfDisplayActivity.class);
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

	public void showJBulletDisplay(View view)
	{
		if (gameDir != null)
		{
			Intent intent = new Intent(this, JBulletActivity.class);
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

	public void showESExplorer(View view)
	{
		if (gameDir != null)
		{
			Intent intent = new Intent(this, AndyESExplorerActivity.class);
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
