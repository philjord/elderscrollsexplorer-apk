package com.ingenieur.andyelderscrolls;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ingenieur.andyelderscrolls.andyesexplorer.AndyESExplorerActivity;
import com.ingenieur.andyelderscrolls.utils.FileChooser;
import com.ingenieur.andyelderscrolls.utils.SopInterceptor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import scrollsexplorer.GameConfig;
import scrollsexplorer.PropertyLoader;

import static android.widget.Toast.LENGTH_LONG;
import static com.ingenieur.andyelderscrolls.ElderScrollsActivity.GAME_FOLDER;
import static com.ingenieur.andyelderscrolls.ElderScrollsActivity.LAST_SELECTED_FILE;
import static com.ingenieur.andyelderscrolls.ElderScrollsActivity.PREFS_NAME;
import static com.ingenieur.andyelderscrolls.ElderScrollsActivity.SELECTED_GAME;
import static com.ingenieur.andyelderscrolls.ElderScrollsActivity.SELECTED_START_CONFIG;


/**
 * Created by phil on 7/15/2016.
 */
public class MorrowindActivity extends Activity
{

	private static final String WELCOME_SCREEN_UNWANTED = "WELCOME_SCREEN_UNWANTED";
	private GameConfig gameSelected;


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
		else
		{
			permissionGranted();
		}
	}

	private void permissionGranted()
	{
		setContentView(R.layout.morrowind);

		try
		{
			PropertyLoader.load(getFilesDir().getAbsolutePath());
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		possiblyShowWelcomeScreen();
	}

	private void attemptLaunchMorrowind()
	{
		for (GameConfig gameConfig : GameConfig.allGameConfigs)
		{
			System.out.println("looking for game folder " + gameConfig.folderKey);

			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			String gameFolder = settings.getString(GAME_FOLDER + gameConfig.folderKey, "");
			if (gameFolder.length() > 0)
			{
				gameConfig.scrollsFolder = gameFolder;

				// id this key the morrowind one?
				if ("MorrowindFolder".equals(gameConfig.folderKey))
				{
					// does it in fact contain the esm file (perhaps the data has been deleted for example)
					File checkEsm = new File(gameConfig.scrollsFolder, gameConfig.mainESMFile);

					if (!checkEsm.exists())
					{
						// if no esm clear this settings so we don't waste time with it
						SharedPreferences.Editor editor = settings.edit();
						editor.remove(GAME_FOLDER + gameConfig.folderKey);
						editor.apply();
					}
					else
					{
						gameSelected = gameConfig;
						break;
					}
				}
			}

		}


		if (gameSelected != null)
		{
			showStartConfigPicker();
		}
		else
		{
			Toast.makeText(this, "Please select the morrowind.esm file", Toast.LENGTH_LONG)
					.show();
			setGameESMFile();
		}

	}

	private void possiblyShowWelcomeScreen()
	{
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		boolean welcomeScreenUnwanted = settings.getBoolean(WELCOME_SCREEN_UNWANTED, false);

		if (welcomeScreenUnwanted)
		{
			attemptLaunchMorrowind();
		}
		else
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			// Add the buttons
			builder.setPositiveButton(R.string.welcometextyes, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					// do remind again so no prefs
					attemptLaunchMorrowind();
				}
			});
			builder.setNegativeButton(R.string.welcometextno, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					// don't remind again
					SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
					SharedPreferences.Editor editor = settings.edit();
					editor.putBoolean(WELCOME_SCREEN_UNWANTED, true);
					editor.apply();
					attemptLaunchMorrowind();
				}
			});
			builder.setMessage(R.string.welcometext);

			// Create the AlertDialog
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}


	private void showStartConfigPicker()
	{
		final ListView gameConfigSelectorList = (ListView) findViewById(R.id.gameConfigSelectView);


		String[] configNames = new String[]
				{
						"inside boat",
						"outside boat",
						"combat",
						"vivec",
						"ald rhun",
						"tel mora",
						"inside cavern with azura",
						"ghost gate",
						"nice green land",
						"dwarf ruins"
				};

		gameConfigSelectorList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, configNames)
		{
			@Override
			public View getView(int pos, View view, ViewGroup parent)
			{
				view = super.getView(pos, view, parent);
				((TextView) view).setSingleLine(true);
				return view;
			}
		});


		gameConfigSelectorList.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int which, long id)
			{
				// send the which through and hope they match up

				Intent intent = new Intent(MorrowindActivity.this, AndyESExplorerActivity.class);
				intent.putExtra(SELECTED_GAME, gameSelected.gameName);
				intent.putExtra(SELECTED_START_CONFIG, which);
				startActivity(intent);
			}
		});
	}

	public void setGameESMFile()
	{
		String extStore = System.getenv("EXTERNAL_STORAGE");
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String prefRoot = settings.getString(LAST_SELECTED_FILE, extStore);

		File chooserStartFolder = new File(prefRoot);
		if (!chooserStartFolder.isDirectory())
			chooserStartFolder = chooserStartFolder.getParentFile();

		final FileChooser esmFileChooser = new FileChooser(this, chooserStartFolder);
		esmFileChooser.setExtension("morrowind.esm");// note extension is whole file name
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
			Toast.makeText(this, "Selected file not a valid game esm", LENGTH_LONG).show();
		}


		attemptLaunchMorrowind();
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
