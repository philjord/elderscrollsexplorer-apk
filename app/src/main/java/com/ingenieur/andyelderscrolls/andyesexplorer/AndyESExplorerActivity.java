package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ingenieur.andyelderscrolls.ElderScrollsActivity;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;

import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;

import jogamp.newt.driver.android.NewtBaseActivity;


public class AndyESExplorerActivity extends NewtBaseActivity
{
	private ScrollsExplorer scrollsExplorer;
	private GLWindow gl_window;
	private String gameName;
	private int gameConfigId = 1;
	private boolean scrollsExplorerInitCalled = false;


	@Override
	public void onCreate(final Bundle state)
	{
		System.setProperty("j3d.cacheAutoComputeBounds", "true");
		System.setProperty("j3d.defaultReadCapability", "false");
		System.setProperty("j3d.defaultNodePickable", "false");
		System.setProperty("j3d.defaultNodeCollidable", "false");
		System.setProperty("j3d.noDestroyContext", "true");// don't clean up as the preserve/restore will handle it

		SimpleShaderAppearance.setVersionES300();

		super.onCreate(state);

		Intent intent = getIntent();
		gameName = intent.getStringExtra(ElderScrollsActivity.SELECTED_GAME);
		gameConfigId = intent.getIntExtra(ElderScrollsActivity.SELECTED_START_CONFIG, 1);

		createGLWindow();
	}

	private void createGLWindow()
	{
		final GLCapabilities caps =
				new GLCapabilities(GLProfile.get(GLProfile.GLES2));

		caps.setDoubleBuffered(true);
		caps.setDepthBits(16);
		caps.setStencilBits(8);
		caps.setHardwareAccelerated(true);
		//caps.setSampleBuffers(true);death! no touch!
		//caps.setNumSamples(2);


		gl_window = GLWindow.create(caps);
		//TODO: why the hell was this here? just to try to imprve performance
		//gl_window.setSurfaceScale(new float[]{0.5f, 0.5f});
		gl_window.setFullscreen(true);

		this.setContentView(this.getWindow(), gl_window);


		gl_window.addGLEventListener(new GLEventListener()
									 {
										 @Override
										 public void init(@SuppressWarnings("unused") final GLAutoDrawable drawable)
										 {
										 }

										 @Override
										 public void reshape(final GLAutoDrawable drawable, final int x, final int y,
															 final int w, final int h)
										 {
										 }

										 @Override
										 public void display(final GLAutoDrawable drawable)
										 {
											 try
											 {
												 // this is called on a resume as well, so only init once
												 if (!scrollsExplorerInitCalled)
												 {
													 scrollsExplorerInitCalled = true;
													 AndyESExplorerActivity.this.runOnUiThread(new Runnable()
													 {
														 @Override
														 public void run()
														 {
															 scrollsExplorer = new ScrollsExplorer(AndyESExplorerActivity.this, gl_window, gameName, gameConfigId);
														 }
													 });
												 }
												 else
												 {
													 // possibly hasn't been created yet
													 if (scrollsExplorer != null)
													 {
														 // this is from a resume (start renderer calls addNotify)
														 scrollsExplorer.startRenderer(gl_window);
													 }
												 }
											 }
											 catch (Exception e)
											 {
												 e.printStackTrace();
											 }
										 }

										 @Override
										 public void dispose(final GLAutoDrawable drawable)
										 {
										 }
									 }

		);


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
		super.onDestroy();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		View decorView = getWindow().getDecorView();
		if (hasFocus)
		{
			decorView.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}

}
