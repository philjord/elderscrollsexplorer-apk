package com.ingenieur.andyelderscrolls.nifdisplay;


import android.app.Activity;
import android.widget.Toast;

import com.ingenieur.andyelderscrolls.utils.AndyFPSCounter;
import com.ingenieur.andyelderscrolls.utils.DragMouseAdapter;
import com.ingenieur.andyelderscrolls.utils.FileChooser;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.sun.j3d.utils.universe.SimpleUniverse;

import java.io.File;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Light;
import javax.media.j3d.PointLight;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import archive.ArchiveFile;
import archive.BSArchiveSet;
import bsa.source.BsaTextureSource;
import nif.BgsmSource;
import nif.NifJ3dVisPhysRoot;
import nif.NifToJ3d;
import nif.appearance.NiGeometryAppearanceFactoryShader;
import nif.j3d.J3dNiAVObject;
import nif.shaders.NiGeometryAppearanceShader;
import tools.compressedtexture.dds.DDSTextureLoader;
import tools3d.camera.simple.SimpleCameraHandler;
import tools3d.mixed3d2d.Canvas3D2D;
import tools3d.utils.ShaderSourceIO;
import tools3d.utils.Utils3D;
import tools3d.utils.leafnode.Cube;
import utils.source.MeshSource;
import utils.source.TextureSource;
import utils.source.file.FileMediaRoots;
import utils.source.file.FileMeshSource;
import utils.source.file.FileTextureSource;

public class NifDisplayTester implements DragMouseAdapter.Listener
{
	private SimpleCameraHandler simpleCameraHandler;

	private TransformGroup spinTransformGroup = new TransformGroup();

	private TransformGroup rotateTransformGroup = new TransformGroup();

	private BranchGroup modelGroup = new BranchGroup();

	private SpinTransform spinTransform;

	private boolean cycle = true;

	private boolean showHavok = true;

	private boolean showVisual = true;

	private boolean animateModel = true;

	private boolean spin = false;

	private SimpleUniverse simpleUniverse;

	private Background background = new Background();

	public Canvas3D2D canvas3D2D;

	private AndyFPSCounter fpsCounter;

	private Activity parentActivity;

	private File chooserStartFolder;
	private File currentFileDisplayed;

	private DragMouseAdapter dragMouseAdapter = new DragMouseAdapter();

	private MeshSource meshSource = null;
	private TextureSource textureSource = null;

	public NifDisplayTester(Activity parentActivity, GLWindow gl_window, File rootDir)
	{
		chooserStartFolder = new File(rootDir, "Meshes");
		this.parentActivity = parentActivity;
		NifToJ3d.SUPPRESS_EXCEPTIONS = false;
		NiGeometryAppearanceShader.OUTPUT_BINDINGS = true;
		ArchiveFile.USE_FILE_MAPS = false;

		FileTextureSource.compressionType = FileTextureSource.CompressionType.KTX;
		NiGeometryAppearanceFactoryShader.setAsDefault();
		ShaderSourceIO.SWAP_VER120_TO_VER100 = true;

		FileMediaRoots.setFixedRoot(rootDir.getAbsolutePath());

		meshSource = new FileMeshSource();
		//textureSource = new FileTextureSource();
		BSArchiveSet bsaFileSet = new BSArchiveSet(new String[]{rootDir.getAbsolutePath()}, true, false);
		textureSource = new BsaTextureSource(bsaFileSet);

		canvas3D2D = new Canvas3D2D(gl_window);

		simpleUniverse = new SimpleUniverse(canvas3D2D);
		DDSTextureLoader.setAnisotropicFilterDegree(8);

		fpsCounter = new AndyFPSCounter();


		spinTransformGroup.addChild(rotateTransformGroup);
		rotateTransformGroup.addChild(modelGroup);
		simpleCameraHandler = new SimpleCameraHandler(simpleUniverse.getViewingPlatform(), simpleUniverse.getCanvas(), modelGroup,
				rotateTransformGroup, false);

		spinTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		spinTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		modelGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		modelGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);

		// Create ambient light	and add it
		Color3f alColor = new Color3f(0.5f, 0.5f, 0.5f);
		//Color3f alColor = new Color3f(0.5f, 0.5f, 0.5f);
		AmbientLight ambLight = new AmbientLight(true, alColor);
		ambLight.setCapability(Light.ALLOW_INFLUENCING_BOUNDS_WRITE);
		ambLight.setInfluencingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));

		//Color3f dlColor = new Color3f(0.1f, 0.1f, 0.6f);
		//DirectionalLight dirLight = new DirectionalLight(true, dlColor, new Vector3f(0f, -1f, 0f));
		//dirLight.setCapability(Light.ALLOW_INFLUENCING_BOUNDS_WRITE);
		//dirLight.setInfluencingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));

		Color3f plColor = new Color3f(1.0f, 0.95f, 0.95f);
		//Color3f plColor = new Color3f(0.4f, 0.4f, 0.7f);
		PointLight pLight = new PointLight(true, plColor, new Point3f(0f, 0f, 0f), new Point3f(1f, 1f, 0f));
		pLight.setCapability(Light.ALLOW_INFLUENCING_BOUNDS_WRITE);
		pLight.setInfluencingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));

		BranchGroup bg = new BranchGroup();

		bg.addChild(fpsCounter.getBehaviorBranchGroup());
		fpsCounter.addToCanvas(canvas3D2D);

		bg.addChild(ambLight);
		//bg.addChild(dirLight);

		TransformGroup tg = new TransformGroup();
		// light is above like nifskope
		Transform3D t = new Transform3D(new Quat4f(0, 0, 0, 1), new Vector3f(0, 10, 0), 1);
		tg.setTransform(t);
		//tg.addChild(new Cube(0.1f));
		tg.addChild(pLight);
		bg.addChild(tg);

		bg.addChild(simpleCameraHandler);

		bg.addChild(spinTransformGroup);
		spinTransform = new SpinTransform(spinTransformGroup);
		spinTransform.setEnable(false);
		bg.addChild(spinTransform);

		background.setColor(0.8f, 0.8f, 0.8f);
		background.setApplicationBounds(null);
		background.setCapability(Background.ALLOW_APPLICATION_BOUNDS_WRITE);
		background.setCapability(Background.ALLOW_APPLICATION_BOUNDS_READ);
		bg.addChild(background);


		tg = new TransformGroup();
		t = new Transform3D();
		t.rotY(Math.PI / 8);
		t.setTranslation(new Vector3f(0, 0, -5));
		tg.setTransform(t);
		tg.addChild(new Cube(0.1f));
		bg.addChild(tg);


		tg = new TransformGroup();
		t = new Transform3D(new Quat4f(0, 0, 0, 1), new Vector3f(0, 0, 0.15f), 1);
		tg.setTransform(t);
		BranchGroup bgc = new BranchGroup();
		bgc.addChild(tg);
		bgc.setCapability(BranchGroup.ALLOW_DETACH);
		tg.addChild(new Cube(0.02f));
		spinTransformGroup.addChild(bgc);
		toggleSpin();

		simpleUniverse.addBranchGraph(bg);

		canvas3D2D.getView().setBackClipDistance(5000);
		canvas3D2D.getView().setFrontClipDistance(0.1f);
		canvas3D2D.getGLWindow().addKeyListener(new KeyHandler());

		dragMouseAdapter.setListener(this);
		canvas3D2D.getGLWindow().addMouseListener(dragMouseAdapter);

		showNifFIleChooser();
	}


	private void setModel(File newFileToDisplay)
	{
		currentFileDisplayed = newFileToDisplay;
		displayNif(newFileToDisplay);
	}

	private void nextModel()
	{
		if (currentFileDisplayed != null)
		{
			File parentDir = currentFileDisplayed.getParentFile();
			File[] files = parentDir.listFiles(new NifKfFileFilter());
			if (files != null && files.length > 0)
			{
				// find current as index in parent
				int idx = -1;
				for (int i = 0; i < files.length; i++)
				{
					if (files[i].equals(currentFileDisplayed))
					{
						idx = i;
						break;
					}
				}

				if (idx != -1 && idx < files.length - 1)
				{
					currentFileDisplayed = files[idx + 1];
					displayNif(currentFileDisplayed);
				}
			}
		}
	}

	private void prevModel()
	{
		if (currentFileDisplayed != null)
		{
			File parentDir = currentFileDisplayed.getParentFile();
			File[] files = parentDir.listFiles(new NifKfFileFilter());
			if (files != null && files.length > 0)
			{
				// find current as index in parent
				int idx = -1;
				for (int i = 0; i < files.length; i++)
				{
					if (files[i].equals(currentFileDisplayed))
					{
						idx = i;
						break;
					}
				}

				if (idx != -1 && idx > 0)
				{
					currentFileDisplayed = files[idx - 1];
					displayNif(currentFileDisplayed);
				}
			}
		}
	}

	private void toggleSpin()
	{
		spin = !spin;
		if (spinTransform != null)
		{
			spinTransform.setEnable(spin);
		}
	}

	private void toggleAnimateModel()
	{
		animateModel = !animateModel;
		update();
	}

	private void toggleHavok()
	{
		showHavok = !showHavok;
		update();
	}

	private void toggleVisual()
	{
		showVisual = !showVisual;
		update();
	}

	private void toggleBackground()
	{
		if (background.getApplicationBounds() == null)
		{
			background.setApplicationBounds(Utils3D.defaultBounds);
		}
		else
		{
			background.setApplicationBounds(null);
		}

	}

	private void toggleCycling()
	{
		cycle = !cycle;
	}

	public void displayNif(File f)
	{
		System.out.println("Selected file: " + f);

		if (f.isDirectory())
		{
			//spinTransform.setEnable(true);
			//processDir(f);
			System.out.println("Bad news dir sent into display nif");
		}
		else if (f.isFile())
		{
			showNif(f.getAbsolutePath(), meshSource, textureSource);
		}

		System.out.println("done");

	}

	public void showNif(final String filename, MeshSource meshSource, TextureSource textureSource)
	{
		parentActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				Toast.makeText(parentActivity, "Displaying " + filename, Toast.LENGTH_SHORT)
						.show();
			}
		});
		BgsmSource.setBgsmSource(meshSource);
		display(NifToJ3d.loadNif(filename, meshSource, textureSource));
	}

	private BranchGroup hbg;

	private BranchGroup vbg;

	private void update()
	{
		modelGroup.removeAllChildren();
		if (showHavok)
		{
			modelGroup.addChild(hbg);
		}
		if (showVisual)
		{
			modelGroup.addChild(vbg);
		}
	}

	private void display(NifJ3dVisPhysRoot nif)
	{
		if (nif != null)
		{

			J3dNiAVObject havok = nif.getHavokRoot();
			if (nif.getVisualRoot().getJ3dNiControllerManager() != null && animateModel)
			{
				//note self cleaning uping
				ControllerInvokerThread controllerInvokerThread = new ControllerInvokerThread(nif.getVisualRoot().getName(),
						nif.getVisualRoot().getJ3dNiControllerManager(), havok.getJ3dNiControllerManager());
				controllerInvokerThread.start();
			}

			modelGroup.removeAllChildren();

			hbg = new BranchGroup();
			hbg.setCapability(BranchGroup.ALLOW_DETACH);

			if (showHavok && havok != null)
			{
				hbg.addChild(havok);
				modelGroup.addChild(hbg);
			}

			vbg = new BranchGroup();
			vbg.setCapability(BranchGroup.ALLOW_DETACH);

			if (showVisual)
			{
				vbg.addChild(nif.getVisualRoot());
				modelGroup.addChild(vbg);
			}

			simpleCameraHandler.viewBounds(nif.getVisualRoot().getBounds());
			System.out.println("attempt to set bounds to " + nif.getVisualRoot().getBounds());

			//TODO: the bounds was 0.21 (seems good) and eye was set to 0.8 but this this seems too close?
			if (nif.getVisualRoot().getBounds() instanceof BoundingSphere)
			{
				if (((BoundingSphere) nif.getVisualRoot().getBounds()).getRadius() < 1f)
					simpleCameraHandler.setView(new Point3d(0, 0, 2), new Point3d());
			}

			spinTransform.setEnable(spin);
		}
		else
		{
			System.out.println("why you give display a null eh?");
		}

	}

	private void showNifFIleChooser()
	{
		// show file chooser
		parentActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				new FileChooser(parentActivity, chooserStartFolder).setExtension("nif").setFileListener(new FileChooser.FileSelectedListener()
				{
					@Override
					public void fileSelected(final File file)
					{
						chooserStartFolder = file;
						setModel(file);
					}
				}).showDialog();
			}
		});
	}

	@Override
	public void dragComplete(final MouseEvent e, DragMouseAdapter.DRAG_TYPE dragType)
	{
		if (dragType == DragMouseAdapter.DRAG_TYPE.UP)
		{
			// show Keyboard
			boolean kbVis = ((com.jogamp.newt.Window) e.getSource()).isKeyboardVisible();
			((com.jogamp.newt.Window) e.getSource()).setKeyboardVisible(!kbVis);
		}
		else if (dragType == DragMouseAdapter.DRAG_TYPE.DOWN)
		{
			showNifFIleChooser();
		}
		else if (dragType == DragMouseAdapter.DRAG_TYPE.LEFT)
		{
			prevModel();
		}
		else if (dragType == DragMouseAdapter.DRAG_TYPE.RIGHT)
		{
			nextModel();
		}
	}

	private class KeyHandler extends KeyAdapter
	{
		public KeyHandler()
		{
			/*System.out.println("H toggle havok display");
			System.out.println("L toggle visual display");
			System.out.println("J toggle spin");
			System.out.println("K toggle animate model");
			System.out.println("P toggle background color");
			System.out.println("Space toggle cycle through files");*/
		}


		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_SPACE)
			{
				toggleCycling();
			}
			else if (e.getKeyCode() == KeyEvent.VK_H)
			{
				toggleHavok();
			}
			else if (e.getKeyCode() == KeyEvent.VK_J)
			{
				toggleSpin();
			}
			else if (e.getKeyCode() == KeyEvent.VK_K)
			{
				toggleAnimateModel();
			}
			else if (e.getKeyCode() == KeyEvent.VK_L)
			{
				toggleVisual();
			}
			else if (e.getKeyCode() == KeyEvent.VK_P)
			{
				toggleBackground();
			}
			else if (e.getKeyCode() == KeyEvent.VK_N)
			{
				nextModel();
			}
			else if (e.getKeyCode() == KeyEvent.VK_M)
			{
				prevModel();
			}
		}
	}
}