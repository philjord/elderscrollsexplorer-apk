package com.ingenieur.andyelderscolls.nifdisplay;

import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.ingenieur.andyelderscolls.utils.SopInterceptor;
import com.jogamp.newt.event.MonitorEvent;
import com.jogamp.newt.event.MonitorModeListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.zip.DataFormatException;

import esmmanager.common.PluginException;
import esmmanager.common.data.plugin.PluginRecord;
import esmmanager.loader.ESMManager;
import esmmanager.loader.IESMManager;
import jogamp.newt.driver.android.NewtBaseActivity;
import nif.BgsmSource;
import nif.NifFile;
import nif.NifFileReader;
import nif.NifJ3dHavokRoot;
import nif.NifJ3dVisRoot;
import nif.NifToJ3d;
import nif.appearance.NiGeometryAppearanceFactoryShader;
import utils.source.file.FileMediaRoots;
import utils.source.file.FileMeshSource;
import utils.source.file.FileTextureSource;

import static android.widget.Toast.LENGTH_LONG;

public class NifDisplayActivity extends NewtBaseActivity
{
	public static NifDisplayTester nifDisplay;
	GLWindow gl_window;

	@Override
	public void onCreate(final Bundle state)
	{
		super.onCreate(state);

		// get system out to log
		PrintStream interceptor = new SopInterceptor(System.out, "sysout");
		System.setOut(interceptor);
		PrintStream interceptor2 = new SopInterceptor(System.err, "syserr");
		System.setErr(interceptor2);

		int hasWriteExternalStorage = checkSelfPermission(permission.WRITE_EXTERNAL_STORAGE);
		if (hasWriteExternalStorage != PackageManager.PERMISSION_GRANTED)
		{
			requestPermissions(new String[]{permission.WRITE_EXTERNAL_STORAGE},
					REQUEST_CODE_ASK_PERMISSIONS);
		}
		else
		{
			setupGLDisplay();
		}
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
					setupGLDisplay();
				}
				else
				{
					// Permission Denied
					Toast.makeText(NifDisplayActivity.this, "WRITE_EXTERNAL_STORAGE Denied", Toast.LENGTH_SHORT)
							.show();
				}
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}


	private void setupGLDisplay()
	{
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
												 nifDisplay = new NifDisplayTester(NifDisplayActivity.this, gl_window);

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


	private void readSingleNifFIle()
	{
		File path = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DCIM);
		File file = new File(path, "/ese/morrowind/meshes/box.nif");

		NifFile nifFile = null;
		if (file.exists())
		{
			String filename = file.getAbsolutePath();
			InputStream inputStream = null;
			try
			{
				inputStream = new BufferedInputStream(new FileInputStream(file));

				nifFile = NifFileReader.readNif(filename, inputStream);
				Toast.makeText(NifDisplayActivity.this, "Dear lord nif file read in all " + nifFile.header, Toast.LENGTH_LONG)
						.show();
			}
			catch (IOException e)
			{
				System.out.println("FileMeshSource:  " + file + " " + e + " " + e.getStackTrace()[0]);
			}
			finally
			{
				try
				{
					if (inputStream != null)
						inputStream.close();
				}
				catch (IOException e)
				{

					e.printStackTrace();
				}
			}
		}
		if (nifFile == null)
		{
			System.out.println("FileMeshSource - Problem with loading niffile: " + file);
		}
	}


	void readExternalStoragePublicDcimESE()
	{
		File path = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DCIM);
		File file = new File(path, "/ese/morrowind/morrowind.esm");


		IESMManager esmManager = ESMManager.getESMManager(file.getAbsolutePath());
		try
		{
			PluginRecord pr = esmManager.getWRLD(0);

			Toast.makeText(NifDisplayActivity.this, "ESMManger loaded up", LENGTH_LONG).show();
		}
		catch (DataFormatException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (PluginException e)
		{
			e.printStackTrace();
		}
/*
		BufferedInputStream buf = null;
        try {

            buf = new BufferedInputStream(new FileInputStream(file));
            byte[] b = new byte[1024];
            int c = 0;
            while (buf.available() > 0) {
               c+= buf.read(b);
            }
            Toast.makeText(ElderScrollsActivity.this, "I just read in " +c+ " bytes of the ol' esm file there", LENGTH_LONG)
                    .show();


        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing " + file, e);
        } finally {
            try {

                if (buf != null) {
                    buf.close();
                }
            } catch (IOException e) {
            }
        }*/
	}

	private void readNifUsingMeshSource()
	{
		File path = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DCIM);

		NifToJ3d.SUPPRESS_EXCEPTIONS = false;
		//ASTC or DDS
		FileTextureSource.compressionType = FileTextureSource.CompressionType.ASTC;
		NiGeometryAppearanceFactoryShader.setAsDefault();


		File meshRoot = new File(path, "/ese/morrowind/");
		FileMediaRoots.setMediaRoots(new String[]{meshRoot.getAbsolutePath()});
		NiGeometryAppearanceFactoryShader.setAsDefault();

		FileMeshSource fileMeshSource = new FileMeshSource();
		BgsmSource.setBgsmSource(fileMeshSource);

		try
		{

			File f = new File("meshes/x/ex_vivec_palace_01.nif");

			NifJ3dVisRoot s2 = NifToJ3d.loadShapes(f.getCanonicalPath(), fileMeshSource, new FileTextureSource());

			NifJ3dHavokRoot h = NifToJ3d.loadHavok(f.getCanonicalPath(), fileMeshSource);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}


}
