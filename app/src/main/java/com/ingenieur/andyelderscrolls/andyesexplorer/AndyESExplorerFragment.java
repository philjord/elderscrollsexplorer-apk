package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.nativewindow.NativeWindowException;
import com.jogamp.newt.Window;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.hudbasics.graph.demos.ui.Label;

import org.jogamp.java3d.Transform3D;
import org.jogamp.vecmath.AxisAngle4f;
import org.jogamp.vecmath.Quat4f;
import org.jogamp.vecmath.Vector3d;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import jogamp.newt.WindowImpl;
import jogamp.newt.driver.android.NewtBaseFragment;
import jogamp.newt.driver.android.WindowDriver;
import tools3d.mixed3d2d.Canvas3D2D;
import tools3d.utils.YawPitch;


public class AndyESExplorerFragment extends NewtBaseFragment
{
	private GLWindow gl_window;
	private ScrollsExplorer scrollsExplorer;
	private boolean scrollsExplorerInitCalled = false;

	private NavigationPanel navigationPanel;

	@Override
	public void onCreate(final Bundle state)
	{
		super.onCreate(state);


		createGLWindow();


		//PHIL DO THIS http://mesai0.blogspot.co.nz/2013/03/outdoorgeo-augmented-reality-camera.html
		// Configure sensors and touch in the Activity constructor.
		if (AndyESExplorerActivity.gyroscope)
		{
			sensorManager = (SensorManager) this.getActivity().getSystemService(Context.SENSOR_SERVICE);
			// TYPE_ROTATION_VECTOR is the easiest sensor since it handles all the hard math for fusion.
			orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
			//TODO: why doesn't			Sensor.TYPE_GAME_ROTATION_VECTOR work at all
			phoneOrientationListener = new PhoneOrientationListener();



			//when uncommenting tlook below to onPause and onResume
		}
	}

	private SensorManager sensorManager;
	private Sensor orientationSensor;
	private PhoneOrientationListener phoneOrientationListener;
	private float[] phoneInWorldSpaceMatrix = new float[16];
	private float[] ori = new float[3];

	// Detect sensor events and save them as a matrix.
	private class PhoneOrientationListener implements SensorEventListener
	{
		private Quat4f q = new Quat4f();
		YawPitch yp = new YawPitch();
		AxisAngle4f aa = new AxisAngle4f();
		Transform3D t = new Transform3D();
		Vector3d v = new Vector3d();

		@Override
		public void onSensorChanged(SensorEvent event)
		{
			//https://stackoverflow.com/questions/2881128/how-to-use-onsensorchanged-sensor-data-in-combination-with-opengl
			if (scrollsExplorer != null && scrollsExplorer.simpleWalkSetup != null)
			{
				// Android coordinate system assumes Y points North and Z points to the sky. OpenGL has
				// Y pointing up and Z pointing toward the user.
				SensorManager.getRotationMatrixFromVector(phoneInWorldSpaceMatrix, event.values);
				// Rotate from Android coordinates to OpenGL coordinates.
				//Matrix.rotateM(phoneInWorldSpaceMatrix, 0, 90, 1, 0, 0);


				SensorManager.remapCoordinateSystem(phoneInWorldSpaceMatrix, SensorManager.AXIS_X,
						SensorManager.AXIS_Z, phoneInWorldSpaceMatrix);

				final float[] anglesInRadians = new float[3];
				SensorManager.getOrientation(phoneInWorldSpaceMatrix, anglesInRadians);



				yp.setYaw(-anglesInRadians[0]);
				yp.setPitch(-anglesInRadians[1]);
				yp.get(q);
				System.out.println("yp = " + yp);
				System.out.println("a[0] " + anglesInRadians[0] + " anglesInRadians[1] " + anglesInRadians[1] + " anglesInRadians[2] " + anglesInRadians[2] + " " );

				System.out.println("x "+event.values[0]+ " y "+ event.values[1]+ " z "+ event.values[2]);
				scrollsExplorer.simpleWalkSetup.getAvatarLocation().setRotation(q);

				if(debuglabel==null)
				{
					Canvas3D2D canvas3D2D = scrollsExplorer.simpleWalkSetup.getCanvas2D3D();
					float pixelSizeFPS = 0.00006F * (float) canvas3D2D.getGLWindow().getSurfaceHeight();
					try
					{
						debuglabel = new Label(canvas3D2D.getVertexFactory(), 0, FontFactory.get(0).getDefault(), pixelSizeFPS, "");
						canvas3D2D.addUIShape(debuglabel);
						debuglabel.setEnabled(true);
						debuglabel.translate(-0.9F, 0F, 0f);
						debuglabel.setColor(1f, 1f, 1f, 0.85f);
						debuglabel2 = new Label(canvas3D2D.getVertexFactory(), 0, FontFactory.get(0).getDefault(), pixelSizeFPS, "");
						canvas3D2D.addUIShape(debuglabel2);
						debuglabel2.setEnabled(true);
						debuglabel2.translate(-0.9F, 0.2F, 0f);
						debuglabel2.setColor(1f, 1f, 1f, 0.85f);

					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}

				debuglabel.setText(" a[0] " + String.format("%.4g%n", anglesInRadians[0]) + " a[1] " + String.format("%.4g%n", anglesInRadians[1]) + " a[2] " + String.format("%.4g%n", anglesInRadians[2]));
				debuglabel2.setText(" y = " + String.format("%.4g%n", yp.getYaw()) + " p = " + String.format("%.4g%n", yp.getPitch()));
			}
		}

		private Label debuglabel;
		private Label debuglabel2;

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{

		}
	}

	private void createGLWindow()
	{
		final GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GLES2));
		caps.setDoubleBuffered(true);
		caps.setDepthBits(16);
		caps.setStencilBits(8);
		caps.setHardwareAccelerated(true);
		caps.setBackgroundOpaque(true);
		if (AndyESExplorerActivity.antialias)
		{
			caps.setSampleBuffers(true);//TODO: I wrote death! no touch! but it seems fine?
			caps.setNumSamples(2);
		}

		gl_window = GLWindow.create(caps);
		//gl_window.setFullscreen(true);

		AndyESExplorerActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "GLWindow.create(caps)", null);

		final Window delegateWindow = gl_window.getDelegatedWindow();
		if (delegateWindow instanceof WindowDriver)
		{
			WindowDriver wd = (WindowDriver) delegateWindow;
			wd.setNativeWindowExceptionListener(new WindowImpl.NativeWindowExceptionListener()
			{
				// return true to indicate success, false will throw the exception
				public boolean handleException(NativeWindowException nwp)
				{
					AndyESExplorerActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "NativeWindowException", null);
					String message = "insufficient3dResourcesMessage";
					String title = "insufficient3dResourcesTitle";
					//JOptionPane.showMessageDialog(getActivity(), message, title, JOptionPane.ERROR_MESSAGE);
					AndyESExplorerActivity act = (AndyESExplorerActivity) AndyESExplorerFragment.this.getActivity();
					Toast.makeText(act, message, Toast.LENGTH_LONG);
					return true;
				}
			});
		}

		gl_window.addGLEventListener(glWindowInitListener);

	}


	GLEventListener glWindowInitListener = new GLEventListener()
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
					AndyESExplorerActivity act = (AndyESExplorerActivity) AndyESExplorerFragment.this.getActivity();

					scrollsExplorer = new ScrollsExplorer(act, gl_window, act.gameName, act.gameConfigId);
					act.scrollsExplorer = scrollsExplorer;
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
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		this.setHasOptionsMenu(true);

		View rootView = getContentView(this.getWindow(), gl_window);
		getActivity().getActionBar().hide();
		return rootView;
	}

	@Override
	public void onStart()
	{
		super.onStart();
		AndyESExplorerActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onStart", null);
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
		if (AndyESExplorerActivity.gyroscope)
		{
			sensorManager.unregisterListener(phoneOrientationListener);
		}
		super.onPause();
	}


	@Override
	public void onResume()
	{
		// Use the fastest sensor readings.
		if (AndyESExplorerActivity.gyroscope)
		{
			sensorManager.registerListener(phoneOrientationListener, orientationSensor, SensorManager.SENSOR_DELAY_GAME);
		}
		super.onResume();
	}

	@Override
	public void onDestroy()
	{
		gl_window.destroy();
		gl_window = null;
		super.onDestroy();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{

		if (scrollsExplorer != null)
		{
			if (isVisibleToUser)
			{
				getActivity().getActionBar().hide();
				// this stops the 3d view when changin to other pages, but it doesn't look cool
				//	scrollsExplorer.startRenderer(gl_window);
			}
			else
			{
				getActivity().getActionBar().show();
				//	scrollsExplorer.stopRenderer();
			}

			if( navigationPanel != null) {
				if(isVisibleToUser)
					navigationPanel.showTooltip();
				else
					navigationPanel.hideTooltip();
			}
		}

		super.setUserVisibleHint(isVisibleToUser);
	}


	/**

	private NavigationPanel createNavigationPanel(Home home,
																								UserPreferences preferences,
																								HomeController3D controller) {

		NavigationPanel navigationPanel = new NavigationPanel(getContext(), getView());
		new NavigationButton(0, -(float) Math.PI / 36, 0, "TURN_LEFT", preferences, controller, navigationPanel.getLeftButton());
		new NavigationButton(12.5f, 0, 0, "GO_FORWARD", preferences, controller, navigationPanel.getForwardButton());
		new NavigationButton(0, (float) Math.PI / 36, 0, "TURN_RIGHT", preferences, controller, navigationPanel.getRightButton());
		new NavigationButton(-12.5f, 0, 0, "GO_BACKWARD", preferences, controller, navigationPanel.getBackButton());
		new NavigationButton(0, 0, -(float) Math.PI / 100, "TURN_UP", preferences, controller, navigationPanel.getUpButton());
		new NavigationButton(0, 0, (float) Math.PI / 100, "TURN_DOWN", preferences, controller, navigationPanel.getDownButton());
		return navigationPanel;
	}


	private static class NavigationButton  {
		private boolean shiftDown;

		public NavigationButton(final float moveDelta,
														final float yawDelta,
														final float pitchDelta,
														String actionName,
														UserPreferences preferences,
														final HomeController3D controller,
														android.view.View button) {


			button.setOnKeyListener(new android.view.View.OnKeyListener() {
				@Override
				public boolean onKey(android.view.View v, int keyCode, KeyEvent event) {
					if(event.getKeyCode() == KeyEvent.KEYCODE_SHIFT_LEFT || event.getKeyCode() == KeyEvent.KEYCODE_SHIFT_RIGHT) {
						shiftDown = event.getAction() == KeyEvent.ACTION_DOWN;
					}
					return false;
				}
			});
			// Update camera when button is armed
			button.setOnTouchListener(new android.view.View.OnTouchListener() {
				// Create a timer that will update camera angles and location
				Timer timer;
				@Override
				public boolean onTouch(android.view.View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						if(timer != null){
							timer.cancel();
						}
						timer = new Timer("",true);
						timer.schedule(new TimerTask() {
							@Override
							public void run() {
								controller.moveCamera(shiftDown ? moveDelta : moveDelta / 5);
								controller.rotateCameraYaw(shiftDown ? yawDelta : yawDelta / 5);
								controller.rotateCameraPitch(pitchDelta);
							}
						},50, 50 );
					} else if (event.getAction() == MotionEvent.ACTION_UP && timer != null) {
						timer.cancel();
					}
					return true;
				}

			});
		}
	}*/
}
