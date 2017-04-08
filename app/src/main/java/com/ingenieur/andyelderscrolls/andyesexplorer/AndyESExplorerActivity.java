package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.ingenieur.andyelderscrolls.BuildConfig;
import com.ingenieur.andyelderscrolls.ElderScrollsActivity;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;

import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.vecmath.AxisAngle4f;
import org.jogamp.vecmath.Quat4f;
import org.jogamp.vecmath.Vector3d;

import jogamp.newt.driver.android.NewtBaseActivity;
import jogamp.newt.driver.android.WindowDriver;
import tools3d.utils.YawPitch;


public class AndyESExplorerActivity extends NewtBaseActivity
{

	private static FirebaseAnalytics mFirebaseAnalytics;

	public static boolean antialias = false;
	private ScrollsExplorer scrollsExplorer;
	private GLWindow gl_window;
	private String gameName;
	private int gameConfigId = 1;
	private boolean scrollsExplorerInitCalled = false;

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
	 * @param id    short id (method name?)
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
	 * @param id    short id (method name?)
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
		if ( mFirebaseAnalytics != null && !BuildConfig.DEBUG )
		{
			Bundle bundle = new Bundle();
			bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
			if (value != null && value.length() > 0)
				bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, value);
			mFirebaseAnalytics.logEvent(event, bundle);
		}
	}

	@Override
	public void onCreate(final Bundle state)
	{
		System.setProperty("j3d.cacheAutoComputeBounds", "true");
		System.setProperty("j3d.defaultReadCapability", "false");
		System.setProperty("j3d.defaultNodePickable", "false");
		System.setProperty("j3d.defaultNodeCollidable", "false");
		System.setProperty("j3d.noDestroyContext", "true");// don't clean up as the preserve/restore will handle it

		SimpleShaderAppearance.setVersionES300();

		if (!BuildConfig.DEBUG)
		{
			if(mFirebaseAnalytics == null)
			{
				// Obtain the FirebaseAnalytics instance.
				mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
			}
		}

		super.onCreate(state);

		Intent intent = getIntent();
		gameName = intent.getStringExtra(ElderScrollsActivity.SELECTED_GAME);
		gameConfigId = intent.getIntExtra(ElderScrollsActivity.SELECTED_START_CONFIG, 1);

		logFireBaseContent("SELECTED_GAME", gameName);


		createGLWindow();


		//PHIL DO THIS http://mesai0.blogspot.co.nz/2013/03/outdoorgeo-augmented-reality-camera.html




		// Configure sensors and touch in the Activity constructor.
//		sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		// TYPE_ROTATION_VECTOR is the easiest sensor since it handles all the hard math for fusion.
//		orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
//		phoneOrientationListener = new PhoneOrientationListener();

		//when uncommenting tlook below to onPause and onResume
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
			if(scrollsExplorer!=null && scrollsExplorer.simpleWalkSetup!=null)
			{
			// Android coordinate system assumes Y points North and Z points to the sky. OpenGL has
			// Y pointing up and Z pointing toward the user.
			SensorManager.getRotationMatrixFromVector(phoneInWorldSpaceMatrix, event.values);
			// Rotate from Android coordinates to OpenGL coordinates.
			Matrix.rotateM(phoneInWorldSpaceMatrix, 0, 90, 1, 0, 0);

				//PJ into landscape?
			//Matrix.rotateM(phoneInWorldSpaceMatrix, 0, 90, 0, 0, 1);
			//SensorManager.remapCoordinateSystem(phoneInWorldSpaceMatrix, SensorManager.AXIS_Y,
			//		SensorManager.AXIS_Z, phoneInWorldSpaceMatrix);

				ori = SensorManager.getOrientation(phoneInWorldSpaceMatrix, event.values);

				//double pitch = Math.asin(-phoneInWorldSpaceMatrix[7]);
				//v.set(event.values[0],event.values[1],event.values[2]);
				//t.setEuler(v);
				//t.get(q);
				//q.set(phoneInWorldSpaceMatrix);
				yp.setYaw(ori[0]);
				 yp.setPitch(ori[1]);
				yp.get(q);
				System.out.println("yp = " + yp);
				scrollsExplorer.simpleWalkSetup.getAvatarLocation().setRotation(q);
			}
		}

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
		if (antialias)
		{
			caps.setSampleBuffers(true);//TODO: I wrote death! no touch! but it seems fine?
			caps.setNumSamples(2);
		}

		gl_window = GLWindow.create(caps);
		gl_window.setFullscreen(true);

		this.setContentView(this.getWindow(), gl_window);

		gl_window.addGLEventListener(GLEventListener2);

	}
	GLEventListener GLEventListener2 = new GLEventListener()
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
			 };

	@Override
	public void onPause()
	{
		if (scrollsExplorer != null)
		{
			scrollsExplorer.closingTime();
			// note stop renderer also calls removenotify
			scrollsExplorer.stopRenderer();
		}
//		sensorManager.unregisterListener(phoneOrientationListener);
		super.onPause();
	}



	@Override
	public void onResume() {
		super.onResume();
		// Use the fastest sensor readings.
//		sensorManager.registerListener(	phoneOrientationListener, orientationSensor, SensorManager.SENSOR_DELAY_GAME);

		super.onResume();
	}

	@Override
	public void onDestroy()
	{

		//shitty shityy onDestory doesn't mean destroy at all! god damn it
		// try to remvoe teh keyboard listener, this is the reverse of setKeyboardVisibleImpl in WindowDriver
	/*	WindowDriver wd = ((WindowDriver)gl_window.getDelegatedWindow()) ;
		InputMethodManager var2 = (InputMethodManager)wd.getAndroidView().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		IBinder var3 = wd.getAndroidView().getWindowToken();
		var2.hideSoftInputFromWindow(var3, 0, new ResultReceiver((Handler)null));



		// this driver has a keyboard listener that is not destroyed
		// so the gl_window is not destroyed and the gl_window has a reference to this activity in it
		((WindowDriver)gl_window.getDelegatedWindow()).registerActivity(null);
		gl_window.removeGLEventListener(GLEventListener2);
		GLEventListener2 = null;*/
		scrollsExplorer.closingTime();
		scrollsExplorer.destroy();
		scrollsExplorer = null;
		gl_window.destroy();
		gl_window = null;
		System.out.println("onDestroy called");
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
