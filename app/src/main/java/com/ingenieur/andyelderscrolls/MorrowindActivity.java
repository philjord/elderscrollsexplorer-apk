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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.google.android.vending.expansion.downloader.DownloaderServiceMarshaller;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import com.google.android.vending.expansion.downloader.IDownloaderService;
import com.google.android.vending.expansion.downloader.IStub;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ingenieur.andyelderscrolls.andyesexplorer.AndyESExplorerActivity;
import com.ingenieur.andyelderscrolls.andyesexplorer.OptionsDialog;
import com.ingenieur.andyelderscrolls.utils.FileChooser;
import com.ingenieur.andyelderscrolls.utils.SopInterceptor;
import com.ingenieur.andyelderscrolls.utils.obb.ObbDownloaderService;

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
public class MorrowindActivity extends Activity implements IDownloaderClient
{
	private static final String WELCOME_SCREEN_UNWANTED = "WELCOME_SCREEN_UNWANTED";
	private static final String DEOPTOMIZE = "DEOPTOMIZE";
	private static final String ANTIALIAS = "ANTIALIAS";

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
		boolean deoptomize = settings.getBoolean(DEOPTOMIZE, false);
		menu.findItem(R.id.menu_deoptomize).setChecked(deoptomize);
		setDeoptomize(deoptomize);
		boolean antialias = settings.getBoolean(ANTIALIAS, false);
		menu.findItem(R.id.menu_anti_alias).setChecked(antialias);
		AndyESExplorerActivity.antialias = antialias;


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
			case R.id.menu_options:
				OptionsDialog od = new OptionsDialog(this, null);
				od.display();
				return true;
			case R.id.menu_test_3d:
				test3d();
				return true;
			case R.id.menu_donate:
				donate();
				return true;
			case R.id.menu_deoptomize:
				item.setChecked(!item.isChecked());
				setDeoptomize(item.isChecked());
				return true;
			case R.id.menu_anti_alias:
				item.setChecked(!item.isChecked());
				AndyESExplorerActivity.antialias = item.isChecked();
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean(ANTIALIAS, item.isChecked());
				editor.apply();
				return true;
			case R.id.menu_start_tools:
				Intent intent = new Intent(this, ElderScrollsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
				return true;
			case R.id.menu_help:
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
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |	Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
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



	private ProgressBar mDownloadProgressBar;
	private TextView mProgressPercentTextView;
	private View mDownloadViewGroup;


	private void permissionGranted()
	{
		setContentView(R.layout.morrowind);

		mDownloadProgressBar = (ProgressBar) findViewById(R.id.downloadProgressBar);
		mProgressPercentTextView = (TextView) findViewById(R.id.downloadTextView);
		mDownloadViewGroup = findViewById(R.id.downloadViewGroup);



		// Check if expansion files are available before going any further
		if (!expansionFilesDelivered())
		{
			// Build an Intent to start this activity from the Notification
			Intent notifierIntent = new Intent(this, this.getClass());
			notifierIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
					Intent.FLAG_ACTIVITY_CLEAR_TOP);

			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					notifierIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			// Start the download service (if required)
			try
			{
				int startResult = DownloaderClientMarshaller.startDownloadServiceIfRequired(this,
						pendingIntent, ObbDownloaderService.class);

				// If download has started, initialize this activity to show download progress
				if (startResult != DownloaderClientMarshaller.NO_DOWNLOAD_REQUIRED)
				{
					// Instantiate a member instance of IStub
					mDownloaderClientStub = DownloaderClientMarshaller.CreateStub(this,
							ObbDownloaderService.class);
					// startApp will be called by download state change
					return;
				} // If the download wasn't necessary, fall through to start the app
			}
			catch (PackageManager.NameNotFoundException e)
			{
				e.printStackTrace();
			}
		}
		startApp(); // Expansion files are available, start the app
	}

	private IDownloaderService mRemoteService;
	private IStub mDownloaderClientStub;
	private int mState;

	@Override
	public void onServiceConnected(Messenger m)
	{
		mRemoteService = DownloaderServiceMarshaller.CreateProxy(m);
		mRemoteService.onClientUpdated(mDownloaderClientStub.getMessenger());
	}


	@Override
	public void onDownloadStateChanged(int newState)
	{
		setState(newState);
		boolean showDashboard = true;
		boolean indeterminate;
		String failMessage = null;

		switch (newState)
		{
			case IDownloaderClient.STATE_IDLE:
				// STATE_IDLE means the service is listening, so it's
				// safe to start making calls via mRemoteService.
				indeterminate = true;
				break;
			case IDownloaderClient.STATE_CONNECTING:
			case IDownloaderClient.STATE_FETCHING_URL:
				showDashboard = true;
				indeterminate = true;
				break;
			case IDownloaderClient.STATE_DOWNLOADING:
				showDashboard = true;
				indeterminate = false;
				break;

			case IDownloaderClient.STATE_FAILED_CANCELED:
				failMessage = "STATE_FAILED_CANCELED";
			case IDownloaderClient.STATE_FAILED_UNLICENSED:
				failMessage = "STATE_FAILED_UNLICENSED";
			case IDownloaderClient.STATE_FAILED:
				failMessage = "STATE_FAILED";
			case IDownloaderClient.STATE_FAILED_FETCHING_URL:
				failMessage = "STATE_FAILED_FETCHING_URL";
				showDashboard = false;
				indeterminate = false;
				break;
			case IDownloaderClient.STATE_PAUSED_NEED_CELLULAR_PERMISSION:
			case IDownloaderClient.STATE_PAUSED_WIFI_DISABLED_NEED_CELLULAR_PERMISSION:
				showDashboard = false;
				indeterminate = false;
				break;

			case IDownloaderClient.STATE_PAUSED_BY_REQUEST:
				indeterminate = false;
				break;
			case IDownloaderClient.STATE_PAUSED_ROAMING:
			case IDownloaderClient.STATE_PAUSED_SDCARD_UNAVAILABLE:
				indeterminate = false;
				break;
			case IDownloaderClient.STATE_COMPLETED:
				showDashboard = false;
				indeterminate = false;
				break;
			default:
				indeterminate = true;
				showDashboard = true;
		}
		int newDashboardVisibility = showDashboard ? View.VISIBLE : View.GONE;
		if (mDownloadViewGroup.getVisibility() != newDashboardVisibility)
		{
			mDownloadViewGroup.setVisibility(newDashboardVisibility);
		}
		mDownloadProgressBar.setIndeterminate(indeterminate);

		if(newDashboardVisibility == View.GONE)
		{
			startApp();
		}

		if(failMessage != null)
		{
			Toast.makeText(MorrowindActivity.this, "Download of expansion file failed due to : " + failMessage +
					". It is recomended you uninstall then re-install the app.", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onDownloadProgress(DownloadProgressInfo progress)
	{
		mDownloadProgressBar.setMax((int) (progress.mOverallTotal >> 8));
		mDownloadProgressBar.setProgress((int) (progress.mOverallProgress >> 8));
		mProgressPercentTextView.setText(Long.toString(progress.mOverallProgress * 100 / progress.mOverallTotal) + "%");
	}

	private void setState(int newState)
	{
		if (mState != newState)
		{
			mState = newState;
		}
	}


	@Override
	protected void onResume()
	{
		if (null != mDownloaderClientStub)
		{
			mDownloaderClientStub.connect(this);
		}
		super.onResume();
	}

	@Override
	protected void onStop()
	{
		if (null != mDownloaderClientStub)
		{
			mDownloaderClientStub.disconnect(this);
		}
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

	boolean expansionFilesDelivered()
	{
		for (XAPKFile xf : xAPKS)
		{
			String fileName = Helpers.getExpansionAPKFileName(this, xf.mIsMain,
					xf.mFileVersion);
			if (!Helpers.doesFileExist(this, fileName, xf.mFileSize, false))
				return false;
		}
		return true;
	}

	public void setDeoptomize(boolean deoptomize)
	{
		//DEBUG to fix Nexus 5
		J3dNiTriBasedGeom.JOGLES_OPTIMIZED_GEOMETRY = !deoptomize;
		JoglesPipeline.ATTEMPT_OPTIMIZED_VERTICES = !deoptomize;
		JoglesPipeline.COMPRESS_OPTIMIZED_VERTICES = !deoptomize;
		JoglesPipeline.LATE_RELEASE_CONTEXT = !deoptomize;
		JoglesPipeline.MINIMISE_NATIVE_CALLS_TRANSPARENCY = !deoptomize;
		JoglesPipeline.MINIMISE_NATIVE_CALLS_TEXTURE = !deoptomize;

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(DEOPTOMIZE, deoptomize);
		editor.apply();
	}


	/**
	 * This is a little helper class that demonstrates simple testing of an
	 * Expansion APK file delivered by Market. You may not wish to hard-code
	 * things such as file lengths into your executable... and you may wish to
	 * turn this code off during application development.
	 */

	private static class XAPKFile
	{
		public final boolean mIsMain;
		public final int mFileVersion;
		public final long mFileSize;

		XAPKFile(boolean isMain, int fileVersion, long fileSize)
		{
			mIsMain = isMain;
			mFileVersion = fileVersion;
			mFileSize = fileSize;
		}
	}

	/**
	 * Here is where you place the data that the validator will use to determine
	 * if the file was delivered correctly. This is encoded in the source code
	 * so the application can easily determine whether the file has been
	 * properly delivered without having to talk to the server. If the
	 * application is using LVL for licensing, it may make sense to eliminate
	 * these checks and to just rely on the server.
	 * main.3.com.ingenieur.ese.eseandroid.obb (87.7 MB)
	 * ok, play console will tell me what expansion apk exists for the new apk uploaded
	 * for example the above statement, that number in that name has to match the number
	 * in the details below, it will increment if I upload a new file
	 *
	 * Debugging on my machine is more complicated, I MUST download the latest install version from play
	 * then the versionCode of that apk file will become "licensed" for my machine
	 *
	 * Then I can un-install the apk from the store and install my debug versions and I'll still
	 * be able to download (repeatedly) the obb file.
	 * This means I must develop using the versionCode that is on the play store and only increment it
	 * just before generating the signed apk file for upload.
	 *
	 * In case I forget I will get a little download failed note (one time) on the device when I try to
	 * use a debug with a versionCode other than the play store one
	 * I have to download the current version from store, then replace with debugging version so I'm considered licensed.
	 *
	 */
	private static final XAPKFile[] xAPKS = {
			new XAPKFile(
					true, // true signifies a main file
					3, // the version of the APK that the file was uploaded against
					91969003L // the length of the file in bytes
			),
		/*	new XAPKFile(
					false, // false signifies a patch file
					2, // the version of the APK that the patch file was uploaded against
					0L // the length of the patch file in bytes
			)*/
	};
}
