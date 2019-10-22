package com.ingenieur.andyelderscrolls;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Messenger;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import org.jogamp.java3d.JoglesPipeline;


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import nif.j3d.J3dNiTriBasedGeom;
import nif.shaders.ShaderPrograms;
import scrollsexplorer.GameConfig;
import scrollsexplorer.PropertyLoader;
import simpleandroid.JoglStatusActivity;

import static android.widget.Toast.LENGTH_LONG;
import static com.ingenieur.andyelderscrolls.ElderScrollsActivity.GAME_FOLDER;
import static com.ingenieur.andyelderscrolls.ElderScrollsActivity.LAST_SELECTED_FILE;
import static com.ingenieur.andyelderscrolls.ElderScrollsActivity.PREFS_NAME;
import static com.ingenieur.andyelderscrolls.ElderScrollsActivity.SELECTED_GAME;
import static com.ingenieur.andyelderscrolls.ElderScrollsActivity.SELECTED_START_CONFIG;
import static com.ingenieur.andyelderscrolls.andyesexplorer.ScrollsExplorer.configNames;


/**
 * Created by phil on 7/15/2016.
 */
public class MorrowindActivity extends Activity
{
	private static final String WELCOME_SCREEN_UNWANTED = "WELCOME_SCREEN_UNWANTED";
	private static final String OPTOMIZE = "OPTOMIZE";
	private static final String ANTIALIAS = "ANTIALIAS";
	private static final String GYROSCOPE = "GYROSCOPE";

	public static final String GOOGLE_PUBKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqoEA2+dtSDgAZZhwOIhf67H2xR8rvrLENhrI5zNl8W7+GfGsRxfMmGiwisuOASY8fBh+t5IZumP7WGJ418oML6rUBpUCNihDuZcS/OrNQky7RyFkoY16n1G3v+jm4UwLoEsNQJnEpBWvPy0hptT6qRpRhNI7SVYilzPBc7FQPG2NWKh6kNoqSVoPI3K5hRzIYtqRtkHhFtMvpZhxcQuzKptLDu0ceCyEQLeWJmtiO1yCd57zkG0R+sIWd+69uuORIJGmg8vJWljyBTdhrKB8+sg3SZh4S/6lj0GZpy+M7cpzoJC4aBRVN/YMDxax1c56l7T8AY63pcCou8Ai20ER8QIDAQAB";
	public static final String[] GOOGLE_CATALOG = new String[]{"corm.donation.1",
			"corm.donation.2", "corm.donation.5", "corm.donation.10", "corm.donation.20"};

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
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		boolean optomize = settings.getBoolean(OPTOMIZE, false);
		menu.findItem(R.id.menu_optomize).setChecked(optomize);
		setOptomize(optomize);
		boolean antialias = settings.getBoolean(ANTIALIAS, false);
		menu.findItem(R.id.menu_anti_alias).setChecked(antialias);
		AndyESExplorerActivity.antialias = antialias;
		boolean gyroscope = settings.getBoolean(GYROSCOPE, false);
		menu.findItem(R.id.menu_gyroscope).setChecked(antialias);
		AndyESExplorerActivity.gyroscope = gyroscope;


		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		return super.onPrepareOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_test_3d:
				test3d();
				return true;

			case R.id.menu_optomize:
				item.setChecked(!item.isChecked());
				setOptomize(item.isChecked());
				return true;
			case R.id.menu_anti_alias:
				item.setChecked(!item.isChecked());
				AndyESExplorerActivity.antialias = item.isChecked();
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean(ANTIALIAS, item.isChecked());
				editor.apply();
				return true;
			case R.id.menu_gyroscope:
				item.setChecked(!item.isChecked());
				AndyESExplorerActivity.gyroscope = item.isChecked();
				SharedPreferences settings2 = getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor editor2 = settings2.edit();
				editor2.putBoolean(GYROSCOPE, item.isChecked());
				editor2.apply();
				return true;
			case R.id.menu_start_tools:
				Intent intent = new Intent(this, ElderScrollsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
				return true;
			case R.id.menu_help_screen:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				String welcomeMessage = this.getString(R.string.welcometext);
				TextView textView = new TextView(this);
				textView.setPadding(10,10,10,10);
				textView.setText(Html.fromHtml(welcomeMessage));
				textView.setMovementMethod(LinkMovementMethod.getInstance());
				builder.setView(textView);
				AlertDialog dialog = builder.create();
				dialog.show();
				return true;
			case R.id.menu_donate:
				donate();
				return true;
			case R.id.menu_privacy:
				String urlStr = "https://sites.google.com/view/corm-privacy/home";
				Uri webpage = Uri.parse(urlStr);
				Intent intent2 = new Intent(Intent.ACTION_VIEW, webpage);
				if (intent2.resolveActivity(getPackageManager()) != null) {
					startActivity(intent2);
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void donate()
	{
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		DonationsFragment donationsFragment;

		donationsFragment = DonationsFragment.newInstance( false, true,//BuildConfig.DEBUG, true,
				GOOGLE_PUBKEY, GOOGLE_CATALOG,
				getResources().getStringArray(R.array.donation_google_catalog_values), false, null, null,
				null, false, null, null, false, null);

		ft.replace(R.id.donations_activity_container, donationsFragment, "donationsFragment");
		ft.commit();
	}

	/**
	 * Needed for Google Play In-app Billing. It uses startIntentSenderForResult(). The result is not propagated to
	 * the Fragment like in startActivityForResult(). Thus we need to propagate manually to our Fragment.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		FragmentManager fragmentManager = getFragmentManager();
		Fragment fragment = fragmentManager.findFragmentByTag("donationsFragment");
		if (fragment != null)
		{
			fragment.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void test3d()
	{
		Intent myIntent = new Intent(this, JoglStatusActivity.class);
		this.startActivity(myIntent);
	}

	private void startApp()
	{
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

			String welcomeMessage = this.getString(R.string.welcometext);
			TextView textView = new TextView(this);
			textView.setPadding(10,10,10,10);
			textView.setText(Html.fromHtml(welcomeMessage));
			textView.setMovementMethod(LinkMovementMethod.getInstance());

			builder.setView(textView);

			// Create the AlertDialog
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	private void setGameESMFileSelect(File file)
	{
		// let's see if this guy is one of our game configs
		String fileName = file.getName();
		boolean validESM = false;
		for (GameConfig gameConfig : GameConfig.allGameConfigs)
		{
			if (gameConfig.mainESMFile.equals(fileName))
			{
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

	private void attemptLaunchMorrowind()
	{
		for (GameConfig gameConfig : GameConfig.allGameConfigs)
		{
			//System.out.println("looking for game folder " + gameConfig.folderKey);

			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			String gameFolder = settings.getString(GAME_FOLDER + gameConfig.folderKey, "");
			if (gameFolder.length() > 0)
			{
				// id this key the morrowind one?
				if ("MorrowindFolder".equals(gameConfig.folderKey))
				{
					// does it in fact contain the esm file (perhaps the data has been deleted for example)
					File checkEsm = new File(gameFolder, gameConfig.mainESMFile);

					if (!checkEsm.exists())
					{
						// if no esm clear this settings so we don't waste time with it
						SharedPreferences.Editor editor = settings.edit();
						editor.remove(GAME_FOLDER + gameConfig.folderKey);
						editor.apply();
					}
					else
					{
						gameConfig.scrollsFolder = gameFolder;
						gameSelected = gameConfig;

						// debug shaders like this to externalize from jars
						ShaderPrograms.fileSystemFolder = new File(gameFolder, "shaders");

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



	private void showStartConfigPicker()
	{
		final ListView gameConfigSelectorList = (ListView) findViewById(R.id.gameConfigSelectView);


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


	private void permissionGranted()
	{
		setContentView(R.layout.morrowind);

		startApp(); // Expansion files are available, start the app
	}






	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
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
					Toast.makeText(MorrowindActivity.this, "WRITE_EXTERNAL_STORAGE Denied", Toast.LENGTH_LONG)
							.show();
				}
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}


	public void setOptomize(boolean optomize)
	{
		J3dNiTriBasedGeom.JOGLES_OPTIMIZED_GEOMETRY = optomize;
		JoglesPipeline.ATTEMPT_OPTIMIZED_VERTICES = optomize;
		JoglesPipeline.COMPRESS_OPTIMIZED_VERTICES = optomize;

		// these don't seem to cause trouble often, but the above three do constantly
		//JoglesPipeline.LATE_RELEASE_CONTEXT = optomize;
		//JoglesPipeline.MINIMISE_NATIVE_CALLS_TRANSPARENCY = optomize;
		//JoglesPipeline.MINIMISE_NATIVE_CALLS_TEXTURE = optomize;

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(OPTOMIZE, optomize);
		editor.apply();
	}

}
