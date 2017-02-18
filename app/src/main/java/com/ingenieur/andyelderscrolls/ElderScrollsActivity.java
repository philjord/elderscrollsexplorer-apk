package com.ingenieur.andyelderscrolls;

import android.Manifest.permission;
import android.app.Activity;
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

import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.JoglesPipeline;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import nif.j3d.J3dNiTriBasedGeom;
import scrollsexplorer.GameConfig;
import scrollsexplorer.PropertyLoader;

import static android.widget.Toast.LENGTH_LONG;

public class ElderScrollsActivity extends Activity
{

	/**
	 * Ok no more single root folder, all games must be added by selecting there esm file
	 * bsa files MUST be sibling files or possibly an obb file with teh game name in it
	 * esm files will be identified by filename these are fixed
	 * Tools will ahve an add button to add as many as you like, if a different location
	 * is selected for a current folder it is simply overwritten
	 * Morrowind will look for it's folder and ask for it if not found, tools is how you change this if needed
	 * the esm names that are stored come from the gameconfig list key is folderKey esm name is mainESMFile
	 * So note Andy root must disappear totally? yes I think that's right
	 */
	public static final String PREFS_NAME = "ElderScrollsActivityDefault";

	public final static String SELECTED_GAME = "SELECTED_GAME";
	public static final String SELECTED_START_CONFIG = "SELECTED_START_CONFIG";

	public static final String LAST_SELECTED_FILE = "LastSelectedFile";
	public static final String GAME_FOLDER = "GAME_FOLDER";

	private GameConfig gameSelected;

	private SopInterceptor sysoutInterceptor;
	private SopInterceptor syserrInterceptor;
	private boolean setLogFile = false;


	@Override
	public void onCreate(final Bundle state)
	{
		super.onCreate(state);

		//DEBUG to fix Nexus 5
		J3dNiTriBasedGeom.JOGLES_OPTIMIZED_GEOMETRY = false;
		JoglesPipeline.ATTEMPT_OPTIMIZED_VERTICES = false;
		JoglesPipeline.COMPRESS_OPTIMIZED_VERTICES = false;

		// get system out to log
		sysoutInterceptor = new SopInterceptor(System.out, "sysout");
		System.setOut(sysoutInterceptor);
		syserrInterceptor = new SopInterceptor(System.err, "syserr");
		System.setErr(syserrInterceptor);
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
		try
		{
			PropertyLoader.load(getFilesDir().getAbsolutePath());
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}

		// for testing this code will blank out the prefs
		//SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		//SharedPreferences.Editor editor = settings.edit();
		//for (GameConfig gameConfig : GameConfig.allGameConfigs)
		//		editor.remove(GAME_FOLDER + gameConfig.folderKey);
		//editor.apply();

		fillGameList();
	}

	public void toggleWriteLog(View view)
	{
		setLogFile = !setLogFile;
	}

	public void setGameESMFile(View view)
	{
		String extStore = System.getenv("EXTERNAL_STORAGE");
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String prefRoot = settings.getString(LAST_SELECTED_FILE, extStore);

		File chooserStartFolder = new File(prefRoot);
		if (!chooserStartFolder.isDirectory())
			chooserStartFolder = chooserStartFolder.getParentFile();

		final FileChooser esmFileChooser = new FileChooser(ElderScrollsActivity.this, chooserStartFolder);
		esmFileChooser.setExtension("esm");
		esmFileChooser.setFileListener(
				new FileChooser.FileSelectedListener()
				{
					@Override
					public void fileSelected(final File file)
					{
						setGameESMFileSelect(file);

						SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
						SharedPreferences.Editor editor = settings.edit();
						editor.putString(LAST_SELECTED_FILE, file.getAbsolutePath());
						editor.apply();

						// note the dialog will be dismissed now by itself
					}

					public void folderSelected(final File file)
					{
						//ignore
					}
				}
		);

		// show file chooser
		this.runOnUiThread(new Runnable()
						   {
							   public void run()
							   {
								   esmFileChooser.showDialog();
							   }
						   }
		);
	}

	private void setGameESMFileSelect(File file)
	{
		// let's see if this guy is one of our game configs
		String fileName = file.getName();
		boolean validESM = false;
		for (GameConfig gameConfig : GameConfig.allGameConfigs)
		{
			System.out.println("checking against " + gameConfig.gameName);
			if (gameConfig.mainESMFile.equals(fileName))
			{
				System.out.println("Matched esm file name! " + gameConfig.gameName);

				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString(GAME_FOLDER + gameConfig.folderKey, file.getParentFile().getAbsolutePath());
				editor.apply();

				validESM = true;

				break;
			}
		}

		if (!validESM)
		{
			Toast.makeText(ElderScrollsActivity.this, "Selected file not a valid game esm", LENGTH_LONG).show();
		}

		// update the game list
		fillGameList();
	}


	private void fillGameList()
	{
		final ListView gameSelectorList = (ListView) findViewById(R.id.gameSelectView);

		final ArrayList<GameConfig> gamesWithFoldersSet = new ArrayList<GameConfig>();
		//TODO: by list of game configs see if prefs has a folder value and is it valid file location and has the esm in it
		// if so add it to teh game list thingy
		for (GameConfig gameConfig : GameConfig.allGameConfigs)
		{
			System.out.println("looking for game folder of " + gameConfig.folderKey);

			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			String gameFolder = settings.getString(GAME_FOLDER + gameConfig.folderKey, "");
			if (gameFolder.length() > 0)
			{
				System.out.println("has game folder " + gameFolder);

				gameConfig.scrollsFolder = gameFolder;
				gamesWithFoldersSet.add(gameConfig);
			}

		}


		String[] gameNames = new String[gamesWithFoldersSet.size()];
		int i = 0;
		for (GameConfig gameConfig : gamesWithFoldersSet)
		{
			gameNames[i++] = gameConfig.gameName;
		}
		gameSelectorList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, gameNames)
		{
			@Override
			public View getView(int pos, View view, ViewGroup parent)
			{
				view = super.getView(pos, view, parent);
				((TextView) view).setSingleLine(true);
				return view;
			}
		});


		gameSelectorList.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int which, long id)
			{
				String selection = (String) gameSelectorList.getItemAtPosition(which);
				for (GameConfig gameConfig : gamesWithFoldersSet)
				{
					if (selection.equals(gameConfig.gameName))
					{
						gameSelected = gameConfig;
						break;
					}
				}
			}
		});

		// now await clicky clicky
	}

	public void showNifDisplay(View view)
	{
		if (gameSelected != null)
		{
			setUpLogFile(gameSelected);
			Intent intent = new Intent(this, NifDisplayActivity.class);
			intent.putExtra(SELECTED_GAME, gameSelected.gameName);
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
		if (gameSelected != null)
		{
			setUpLogFile(gameSelected);
			Intent intent = new Intent(this, KfDisplayActivity.class);
			intent.putExtra(SELECTED_GAME, gameSelected.gameName);
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
		if (gameSelected != null)
		{
			setUpLogFile(gameSelected);
			Intent intent = new Intent(this, JBulletActivity.class);
			intent.putExtra(SELECTED_GAME, gameSelected.gameName);
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
		if (gameSelected != null)
		{
			setUpLogFile(gameSelected);
			Intent intent = new Intent(this, AndyESExplorerActivity.class);
			intent.putExtra(SELECTED_GAME, gameSelected.gameName);
			startActivity(intent);
		}
		else
		{
			Toast.makeText(ElderScrollsActivity.this, "Please select a game root folder", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void setUpLogFile(GameConfig gameSelected)
	{
		if (setLogFile)
		{
			// just go for downloads, always there
			File logFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "CallOfMorrowindLog.log");
			sysoutInterceptor.setLogFile(logFile);
			syserrInterceptor.setLogFile(logFile);
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

	public static GameConfig getGameConfig(String gameName)
	{
		for (GameConfig gameConfig : GameConfig.allGameConfigs)
		{
			System.out.println("checking " + gameName + " against " + gameConfig.gameName);
			if (gameConfig.gameName.equals(gameName))
			{
				System.out.println("Found game to load! " + gameConfig.gameName);

				return gameConfig;
			}
		}

		System.out.println("No game found for! " + gameName);
		return null;
	}

}
