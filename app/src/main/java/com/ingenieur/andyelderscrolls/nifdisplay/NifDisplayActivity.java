package com.ingenieur.andyelderscrolls.nifdisplay;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.ingenieur.andyelderscrolls.ElderScrollsActivity;
import com.jogamp.newt.event.MonitorEvent;
import com.jogamp.newt.event.MonitorModeListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;

import java.io.File;

import jogamp.newt.driver.android.NewtBaseActivity;

public class NifDisplayActivity extends NewtBaseActivity
{
	private NifDisplayTester nifDisplay;
	private GLWindow gl_window;
	private String andyRoot;
	private String gameDir;


	@Override
	public void onCreate(final Bundle state)
	{
		super.onCreate(state);

		Intent intent = getIntent();
		gameDir = intent.getStringExtra(ElderScrollsActivity.SELECTED_GAME);
		andyRoot = intent.getStringExtra(ElderScrollsActivity.ANDY_ROOT);

		final GLCapabilities caps =
				new GLCapabilities(GLProfile.get(GLProfile.GLES2));
		gl_window = GLWindow.create(caps);
		gl_window.setFullscreen(true);

		this.setContentView(this.getWindow(), gl_window);


		gl_window.getScreen().addMonitorModeListener(new MonitorModeListener()
													 {
														 @Override
														 public void monitorModeChangeNotify(MonitorEvent monitorEvent)
														 {
														 }

														 @Override
														 public void monitorModeChanged(MonitorEvent monitorEvent, boolean b)
														 {
															 Log.e("System.err", "monitorModeChanged: " + monitorEvent);
														 }
													 }

		);


		gl_window.setVisible(true);

		gl_window.addGLEventListener(new GLEventListener()
									 {
										 @Override
										 public void init(@SuppressWarnings("unused") final GLAutoDrawable drawable)
										 {
											 try
											 {
												 //NOTE Canvas3D requires a fully initialized glWindow (in the android setup) so we must call
												 //NifDisplayTester from this init function
												 nifDisplay = new NifDisplayTester(NifDisplayActivity.this, gl_window, new File(andyRoot, gameDir));

												 // addNotify will start up the renderer and kick things off
												 nifDisplay.canvas3D2D.addNotify();
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
										 }

										 @Override
										 public void display(final GLAutoDrawable drawable)
										 {
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
		//if (nifDisplay != null)
		//	nifDisplay.canvas3D2D.stopRenderer();
		//gl_window.setVisible(false);
		super.onPause();
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
		if (nifDisplay != null)
		{
			nifDisplay.canvas3D2D.stopRenderer();
			nifDisplay.canvas3D2D.removeNotify();
		}
		gl_window.destroy();

		super.onDestroy();
	}
}
