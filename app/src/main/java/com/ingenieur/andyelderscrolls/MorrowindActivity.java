package com.ingenieur.andyelderscrolls;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Messenger;
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
import com.ingenieur.andyelderscrolls.andyesexplorer.AndyESExplorerActivity;
import com.ingenieur.andyelderscrolls.utils.FileChooser;
import com.ingenieur.andyelderscrolls.utils.SopInterceptor;
import com.ingenieur.andyelderscrolls.utils.obb.ObbDownloaderService;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

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
public class MorrowindActivity extends Activity implements IDownloaderClient
{

	private static final String WELCOME_SCREEN_UNWANTED = "WELCOME_SCREEN_UNWANTED";
	private static final String OBB_FILE_NAMES = "OBB_FILE_NAMES";
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

	private ProgressBar mDownloadProgressBar;
	private TextView mProgressPercentTextView;
	private View mDownloadViewGroup;

	private View mainLayout;

	private void permissionGranted()
	{

		setContentView(R.layout.morrowind);
		mainLayout = findViewById(R.id.gameConfigSelectView);;

		mDownloadProgressBar = (ProgressBar) findViewById(R.id.downloadProgressBar);
		mProgressPercentTextView = (TextView) findViewById(R.id.downloadTextView);
		mDownloadViewGroup = findViewById(R.id.downloadViewGroup);


		//see here for smaple that has a download progressbar
		//C:\Users\phil\AppData\Local\Android\sdk\extras\google\market_apk_expansion\downloader_sample\src\com\example\expansion\downloader

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

				// If download has started, initialize this activity to show
				// download progress
				if (startResult != DownloaderClientMarshaller.NO_DOWNLOAD_REQUIRED)
				{
					// Instantiate a member instance of IStub
					mDownloaderClientStub = DownloaderClientMarshaller.CreateStub(this,
							ObbDownloaderService.class);

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
			case IDownloaderClient.STATE_FAILED:
			case IDownloaderClient.STATE_FAILED_FETCHING_URL:
			case IDownloaderClient.STATE_FAILED_UNLICENSED:
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

	// just publicly static can't ever vary really
	public static String[] obbFileNames = null;

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
	 * DEAR GOD! that number there HAS to match the manifest versionCode! has to! so
	 * wait I just get unlicensed from my dev machine when versionCode goes forward one step, so perhaps it's still ok on the live system???
	 *
	 * If this works then I can debug and get te obb file if I keep my versionCode equals to the current on play store
	 * and just add one when new apk is ready for upload, meaning my versionCode gets the ok from license server whilst I debug and develop
	 * because on upload I leave teh apk expansion file alone and it points at the number below's worths
	 *
	 * On top of all this, I just got a download failed note because of license failure
	 * possibly I ahve to download the current version from store, then replace with debugging version so I'm considered licnesed?
	 *
	 * on a new versionCode for upload I have to upload a new apk expansion file, no options no fall back system
	 * because an uninstall removes the file but doesn't find the older file.
	 * I presume people who installed the prior ersion will keep the file so a pure apk update wouldn't need this
	 * but then it doesn't work for new installs
	 * so people will always have to take a new apk expansion until I can get teh "older apkversion" to work with updated apks
	 * when they are mismatched I don't get teh problem in the AEKExpansionPolicy code
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
