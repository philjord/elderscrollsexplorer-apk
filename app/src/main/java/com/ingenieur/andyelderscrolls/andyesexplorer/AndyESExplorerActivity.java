package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

public class AndyESExplorerActivity extends NewtBaseActivity
{
	private ScrollsExplorer scrollsExplorer;
	private GLWindow gl_window;
	private String andyRoot;
	private String gameDir;

	private TextView angleTextView;
	private TextView powerTextView;
	private TextView directionTextView;


	@Override
	public void onCreate(final Bundle state)
	{
		super.onCreate(state);

		Intent intent = getIntent();
		gameDir = intent.getStringExtra(ElderScrollsActivity.SELECTED_GAME);
		andyRoot = intent.getStringExtra(ElderScrollsActivity.ANDY_ROOT);

		final GLCapabilities caps =
				new GLCapabilities(GLProfile.get(GLProfile.GLES2));

		caps.setDoubleBuffered(true);
		caps.setDepthBits(16);
		caps.setStencilBits(8);
		caps.setHardwareAccelerated(true);
		//caps.setSampleBuffers(true);death! no touch!
		//caps.setNumSamples(2);


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
												 //KfDisplayTester from this init function
												 scrollsExplorer = new ScrollsExplorer(AndyESExplorerActivity.this, gl_window, new File(andyRoot, gameDir));
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
		scrollsExplorer.closingTime();
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
