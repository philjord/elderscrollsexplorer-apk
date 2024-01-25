package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.ingenieur.andyelderscrolls.BuildConfig;
import com.ingenieur.andyelderscrolls.ElderScrollsActivity;
import com.ingenieur.andyelderscrolls.R;
import com.ingenieur.andyelderscrolls.utils.SopInterceptor;

import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;

import java.io.PrintStream;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;


public class AndyESExplorerActivity extends FragmentActivity
{
	private static FirebaseAnalytics mFirebaseAnalytics;

	public static boolean antialias = false;
	public static boolean gyroscope = false;
	public ScrollsExplorer scrollsExplorer;
	public int gameConfigId = 1;
	public String gameName;


	public AndyESExplorerPagerAdapter mPagerAdapter;
	public ViewPager mViewPager; // public to allow fragment to move around by button

	/**
	 * For generally doing day to day things
	 *
	 * @param id    short id (method name?)
	 * @param value optional value
	 */
	public static void logFireBaseContent(String id, String value)
	{
		logFireBase(FirebaseAnalytics.Event.SELECT_CONTENT, id, value);
	}

	/**
	 * For generally doing day to day things
	 *
	 * @param id short id (method name?)
	 */
	public static void logFireBaseContent(String id)
	{
		logFireBase(FirebaseAnalytics.Event.SELECT_CONTENT, id, null);
	}

	/**
	 * For more unusual event that represent exploring/exploiting the system
	 *
	 * @param id    short id (method name?)
	 * @param value optional value
	 */
	public static void logFireBaseLevelUp(String id, String value)
	{
		logFireBase(FirebaseAnalytics.Event.LEVEL_UP, id, value);
	}

	/**
	 * For more unusual event that represent exploring/exploiting the system
	 *
	 * @param id short id (method name?)
	 */
	public static void logFireBaseLevelUp(String id)
	{
		logFireBase(FirebaseAnalytics.Event.LEVEL_UP, id, null);
	}

	/**
	 * @param event Event MUST be FirebaseAnalytics.Event.*
	 * @param id    short id (method name?)
	 * @param value optional value
	 */
	public static void logFireBase(String event, String id, String value)
	{
		System.out.println("logFireBase : " + id + "[" + value + "]");
		if (mFirebaseAnalytics != null && !BuildConfig.DEBUG)
		{
			Bundle bundle = new Bundle();
			bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
			if (value != null && value.length() > 0)
				bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, value);
			mFirebaseAnalytics.logEvent(event, bundle);
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		System.setProperty("j3d.cacheAutoComputeBounds", "true");
		System.setProperty("j3d.defaultReadCapability", "false");
		System.setProperty("j3d.defaultNodePickable", "false");
		System.setProperty("j3d.defaultNodeCollidable", "false");
		//System.setProperty("j3d.usePbuffer", "true"); // some phones have no fbo under offscreen
		System.setProperty("j3d.noDestroyContext", "true");// don't clean up as the preserve/restore will handle it

		SimpleShaderAppearance.setVersionES300();

		// get system out to log
		PrintStream interceptor = new SopInterceptor(System.out, "sysout");
		System.setOut(interceptor);
		PrintStream interceptor2 = new SopInterceptor(System.err, "syserr");
		System.setErr(interceptor2);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.esexplorer);

		if (!BuildConfig.DEBUG)
		{
			if (mFirebaseAnalytics == null)
			{
				// Obtain the FirebaseAnalytics instance.
				mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
			}
		}


		Intent intent = getIntent();
		gameName = intent.getStringExtra(ElderScrollsActivity.SELECTED_GAME);
		gameConfigId = intent.getIntExtra(ElderScrollsActivity.SELECTED_START_CONFIG, -1);

		logFireBaseContent("SELECTED_GAME", gameName);

		ActionBar actionBar = getActionBar();
		//actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);

		// if we have any fragments in the manager then we are doing a restore with bundle style,
		// so all frags will get onCreate() but not the init() and fail, let's chuck em away now
		if (getSupportFragmentManager().getFragments() != null)
		{
			Fragment[] frags = getSupportFragmentManager().getFragments().toArray(new Fragment[0]);
			for (int i = 0; i < frags.length; i++)
			{
				Fragment fragment = frags[i];
				if (fragment != null)
					getSupportFragmentManager().beginTransaction().remove(fragment).commit();
			}
		}

		mPagerAdapter = new AndyESExplorerPagerAdapter(getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setCurrentItem(1);
		mViewPager.setOffscreenPageLimit(4);

		invalidateOptionsMenu();
	}


	@Override
	public void onPause()
	{
		if (scrollsExplorer != null)
		{
			scrollsExplorer.closingTime();
			// note stop renderer also calls removenotify
			scrollsExplorer.stopRenderer();
		}

		super.onPause();
	}


	@Override
	public void onResume()
	{
		super.onResume();
	}

	@Override
	public void onDestroy()
	{
		scrollsExplorer.closingTime();
		scrollsExplorer.destroy();
		scrollsExplorer = null;
		System.out.println("onDestroy called");
		super.onDestroy();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		//PJ work out why I had these in here exactly, just for immersive interface?
		View decorView = getWindow().getDecorView();
		if (hasFocus)
		{
			/*decorView.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);*/
			decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.es_main_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId())
		{
			case R.id.es_menu_options:
				if (scrollsExplorer != null && scrollsExplorer.simpleWalkSetup != null)
				{
					OptionsDialog od = new OptionsDialog(this, scrollsExplorer.simpleWalkSetup);
					od.display();
				}
				else
				{
					OptionsDialog od = new OptionsDialog(this, null);
					od.display();
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
