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

import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;

import java.io.File;

import jogamp.newt.driver.android.NewtBaseActivity;


public class AndyESExplorerActivity extends NewtBaseActivity
{
	private ScrollsExplorer scrollsExplorer;
	private GLWindow gl_window;
	private String gameName;


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
		gameName = intent.getStringExtra(ElderScrollsActivity.SELECTED_GAME);
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
		gl_window.setSurfaceScale(new float[]{0.5f, 0.5f});

		//Alternatively, control scaling through the Android API:
		// For applications written in Java, configure the fixed-size property of the GLSurfaceView instance (available since API level 1).
		//	Set the property using the setFixedSize function, which takes two arguments defining the resolution of the final render target.
		//gl_window.setSize(1280,720);

		gl_window.setFullscreen(true);

		this.setContentView(this.getWindow(), gl_window);


		gl_window.setVisible(true);

		gl_window.addGLEventListener(new GLEventListener()
									 {
										 @Override
										 public void init(@SuppressWarnings("unused") final GLAutoDrawable drawable)
										 {
											 //System.err.println("GLEventListenerinit");

											 try
											 {
												 float[] fs = new float[2];
												 gl_window.getCurrentSurfaceScale(fs);
												 //System.out.println("getCurrentSurfaceScale " + fs[0] + " " + fs[1]);


												 //NOTE Canvas3D requires a fully initialized glWindow (in the android setup) so we must call
												 //KfDisplayTester from this init function

												 // this is called ona  resume as well, so only inti once
												 if (scrollsExplorer == null)
												 {
													 scrollsExplorer = new ScrollsExplorer(AndyESExplorerActivity.this, gl_window, gameName);
												 }
												 else
												 {

													 //System.err.println("Init with a non null explorer");

													 if (pauseRestartRequired)
													 {

														 //System.err.println("pauseRestartRequired is true!");

														 // this is from a resume
														 scrollsExplorer.startRenderer(gl_window);
														 pauseRestartRequired = false;
													 }
												 }
											 }
											 catch (Exception e)
											 {
												 e.printStackTrace();
											 }
										 }

										 @Override
										 public void reshape(final GLAutoDrawable drawable, final int x, final int y,
															 final int w, final int h)
										 {
											 //System.err.println("GLEventListenerreshape");

										 }

										 @Override
										 public void display(final GLAutoDrawable drawable)
										 {
											 //System.err.println("GLEventListenerdisplay");
										 }

										 @Override
										 public void dispose(final GLAutoDrawable drawable)
										 {
											 //System.err.println("GLEventListenerdispose");

										 }
									 }

		);


	}

	private boolean pauseRestartRequired = false;

	@Override
	public void onPause()
	{
		//System.err.println("onPause onPause onPause onPause onPause");
		if (scrollsExplorer != null)
		{
			scrollsExplorer.closingTime();
			scrollsExplorer.stopRenderer();
		}
		//gl_window.setVisible(false);
		super.onPause();
		pauseRestartRequired = true;
	}

	@Override
	public void onResume()
	{


		//gl_window.setVisible(true);
		//if (nifDisplay != null)
		//	nifDisplay.canvas3D2D.startRenderer();
		super.onResume();


	}

	@Override
	public void onDestroy()
	{
		if (scrollsExplorer != null)
			scrollsExplorer.closingTime();
		if (gl_window != null)
			gl_window.destroy();

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
