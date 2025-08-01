package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.ingenieur.andyelderscrolls.R;
import com.ingenieur.andyelderscrolls.andyesexplorer.ui.GLWindowOverLay;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.graph.ui.shapes.Label;
import com.jogamp.nativewindow.NativeWindowException;
import com.jogamp.newt.Window;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;

import org.jogamp.java3d.Transform3D;
import org.jogamp.vecmath.AxisAngle4f;
import org.jogamp.vecmath.Quat4f;
import org.jogamp.vecmath.Vector3d;

import java.io.IOException;

import jogamp.newt.WindowImpl;
import jogamp.newt.driver.android.NewtBaseFragment;
import jogamp.newt.driver.android.WindowDriver;
import nifbullet.NavigationProcessorBullet;
import tools3d.mixed3d2d.Canvas3D2D;
import tools3d.navigation.twocircles.NavigationInputNewtMove;
import tools3d.utils.YawPitch;

public class AndyESEVRFragment extends NewtBaseFragment
{
	private GLWindow gl_window;
	private ScrollsExplorer scrollsExplorer;
	private boolean scrollsExplorerInitCalled = false;

	private GLWindowOverLay moveNavigationPanel;
	private GLWindowOverLay lookNavigationPanel;

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

			//when uncommenting look below to onPause and onResume
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
						debuglabel = new Label(0, FontFactory.get(0).getDefault(), pixelSizeFPS, "");
						canvas3D2D.addUIShape(debuglabel);
						debuglabel.setEnabled(true);
						debuglabel.moveTo(-0.9F, 0F, 0f);
						debuglabel.setColor(1f, 1f, 1f, 0.85f);
						debuglabel2 = new Label(0, FontFactory.get(0).getDefault(), pixelSizeFPS, "");
						canvas3D2D.addUIShape(debuglabel2);
						debuglabel2.setEnabled(true);
						debuglabel2.moveTo(-0.9F, 0.2F, 0f);
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
					AndyESExplorerActivity act = (AndyESExplorerActivity) AndyESEVRFragment.this.getActivity();
					Toast.makeText(act, message, Toast.LENGTH_LONG);
					return true;
				}
				// return true to indicate success, false will throw the exception
				public boolean handleRuntimeException(RuntimeException re)
				{
					AndyESExplorerActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "NativeWindowException", null);
					String message = "insufficient3dResourcesMessage";
					String title = "insufficient3dResourcesTitle";
					//JOptionPane.showMessageDialog(getActivity(), message, title, JOptionPane.ERROR_MESSAGE);
					AndyESExplorerActivity act = (AndyESExplorerActivity) AndyESEVRFragment.this.getActivity();
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
					AndyESExplorerActivity activity = (AndyESExplorerActivity) AndyESEVRFragment.this.getActivity();

					scrollsExplorer = new ScrollsExplorer(activity, gl_window, activity.gameName, activity.gameConfigId, null);
					activity.scrollsExplorer = scrollsExplorer;

					moveNavigationPanel = createMoveNavigationPanel(scrollsExplorer.simpleWalkSetup.getNavigationProcessor());
					lookNavigationPanel = createLookNavigationPanel(scrollsExplorer.simpleWalkSetup.getNavigationProcessor());
					// showing the nav panel can only be done on the UI thread
					activity.runOnUiThread(new Runnable() {
						public void run() {
							moveNavigationPanel.showTooltip();
							lookNavigationPanel.showTooltip();
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
			moveNavigationPanel.showTooltip();
			if( moveNavigationPanel != null) {
				if(isVisibleToUser)
					moveNavigationPanel.showTooltip();
				else
					moveNavigationPanel.hideTooltip();
			}
			if( lookNavigationPanel != null) {
				if(isVisibleToUser)
					lookNavigationPanel.showTooltip();
				else
					lookNavigationPanel.hideTooltip();
			}

		}

		super.setUserVisibleHint(isVisibleToUser);
	}




	private GLWindowOverLay createMoveNavigationPanel(NavigationProcessorBullet npb) {
		NavigationInputNewtMove.VERTICAL_RATE = 50f;// allow jumping
		GLWindowOverLay navigationMovePanel = new GLWindowOverLay(getContext(), getView(), R.layout.navigationpanelmovepopup,Gravity.LEFT | Gravity.BOTTOM);

		new MoveNavigationButton(NavigationInputNewtMove.FORWARD_RATE, 0, 0, npb, navigationMovePanel.getButton(R.id.navPanelForwardButton));
		new MoveNavigationButton(-NavigationInputNewtMove.BACKWARD_RATE, 0, 0, npb, navigationMovePanel.getButton(R.id.navPanelBackButton));
		new MoveNavigationButton(0, -NavigationInputNewtMove.STRAFF_RATE, 0,  npb, navigationMovePanel.getButton(R.id.navPanelLeftButton));
		new MoveNavigationButton(0, NavigationInputNewtMove.STRAFF_RATE, 0, npb, navigationMovePanel.getButton(R.id.navPanelRightButton));
		new MoveNavigationButton(0, 0, NavigationInputNewtMove.VERTICAL_RATE, npb, navigationMovePanel.getButton(R.id.navPanelUpButton));
		new MoveNavigationButton(0, 0, -NavigationInputNewtMove.VERTICAL_RATE, npb, navigationMovePanel.getButton(R.id.navPanelDownButton));
		return navigationMovePanel;
	}

	private static class MoveNavigationButton  {

		public MoveNavigationButton(final float zRate,
														final float xRate,
														final float yRate,
														final NavigationProcessorBullet npb,
														View button) {
			button.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						if(zRate!=0)
							npb.setZChange(zRate);
						if(xRate!=0)
							npb.setXChange(xRate);
						if(yRate!=0)
							npb.setYChange(yRate);
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						if(zRate!=0)
							npb.setZChange(0);
						if(xRate!=0)
							npb.setXChange(0);
						if(yRate!=0)
							npb.setYChange(0);
					}
					return true;
				}

			});
		}
	}
	private GLWindowOverLay createLookNavigationPanel(NavigationProcessorBullet npb) {

		GLWindowOverLay navigationLookPanel = new GLWindowOverLay(getContext(), getView(), R.layout.navigationpanellookpopup, Gravity.RIGHT | Gravity.BOTTOM);

		new LookNavigationButton(npb, navigationLookPanel.getButton(R.id.looky));
		return navigationLookPanel;
	}

	private static class LookNavigationButton  {
		private static final float FREE_LOOK_GROSS_ROTATE_FACTOR = -0.005f;
		private float mouseDownLocationx;
		private float mouseDownLocationy;
		public LookNavigationButton(	final NavigationProcessorBullet npb,
														View button) {
			button.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
							mouseDownLocationx = event.getX();
							mouseDownLocationy = event.getY();
					} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
						float ex = event.getX();
						float ey = event.getY();

						float dx = ex - mouseDownLocationx;
						float dy = ey - mouseDownLocationy;

						if (dx != 0 || dy != 0) {
							double scaledDeltaX = (double) dx * FREE_LOOK_GROSS_ROTATE_FACTOR;
							double scaledDeltaY = (double) dy * FREE_LOOK_GROSS_ROTATE_FACTOR;

							if (npb != null) {
								npb.changeRotation(scaledDeltaY, scaledDeltaX);
							}

							mouseDownLocationx = ex;
							mouseDownLocationy = ey;
						}
					}
					return true;
				}

			});
		}
	}
}
