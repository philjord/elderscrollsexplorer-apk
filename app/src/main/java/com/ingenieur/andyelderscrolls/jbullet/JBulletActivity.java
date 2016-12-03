package com.ingenieur.andyelderscrolls.jbullet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ingenieur.andyelderscrolls.ElderScrollsActivity;
import com.ingenieur.andyelderscrolls.kfdisplay.KfDisplayTester;
import com.jogamp.newt.event.MonitorEvent;
import com.jogamp.newt.event.MonitorModeListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;

import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;

import java.io.File;

import jogamp.newt.driver.android.NewtBaseActivity;

public class JBulletActivity extends NewtBaseActivity
{
	private JBulletTester jBulletTester;
	private String andyRoot;
	private String gameDir;


	@Override
	public void onCreate(final Bundle state)
	{
		System.setProperty("j3d.cacheAutoComputeBounds", "true");
		System.setProperty("j3d.defaultReadCapability", "false");
		System.setProperty("j3d.defaultNodePickable", "false");
		System.setProperty("j3d.defaultNodeCollidable", "false");

		SimpleShaderAppearance.setVersionES300();

		super.onCreate(state);

		Intent intent = getIntent();
		gameDir = intent.getStringExtra(ElderScrollsActivity.SELECTED_GAME);
		andyRoot = intent.getStringExtra(ElderScrollsActivity.ANDY_ROOT);
		jBulletTester = new JBulletTester(this, new File(andyRoot, gameDir));
	}

	@Override
	public void onPause()
	{

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


		super.onDestroy();
	}
}
