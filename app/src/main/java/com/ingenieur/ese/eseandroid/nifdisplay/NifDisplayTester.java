package com.ingenieur.ese.eseandroid.nifdisplay;


import android.os.Environment;

import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.sun.j3d.utils.universe.SimpleUniverse;

import java.io.File;
import java.util.Enumeration;
import java.util.prefs.Preferences;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Background;
import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Group;
import javax.media.j3d.Light;
import javax.media.j3d.PointLight;
import javax.media.j3d.Screen3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import jogamp.newt.driver.android.NewtBaseActivity;
import nif.BgsmSource;
import nif.NifJ3dVisPhysRoot;
import nif.NifToJ3d;
import nif.appearance.NiGeometryAppearanceFactoryShader;
import nif.j3d.J3dNiAVObject;
import tools.compressedtexture.dds.DDSTextureLoader;
import tools3d.camera.simple.SimpleCameraHandler;
import tools3d.utils.ShaderSourceIO;
import tools3d.utils.Utils3D;
import tools3d.utils.leafnode.Cube;
import utils.source.MeshSource;
import utils.source.TextureSource;
import utils.source.file.FileMeshSource;
import utils.source.file.FileTextureSource;

public class NifDisplayTester
{
	private SimpleCameraHandler simpleCameraHandler;

	private TransformGroup spinTransformGroup = new TransformGroup();

	private TransformGroup rotateTransformGroup = new TransformGroup();

	private BranchGroup modelGroup = new BranchGroup();

	private SpinTransform spinTransform;

	private FileManageBehavior fileManageBehavior = new FileManageBehavior();

	private boolean cycle = true;

	private boolean showHavok = true;

	private boolean showVisual = true;

	private boolean animateModel = true;

	private boolean spin = false;

	private long currentFileLoadTime = 0;

	private File currentFileTreeRoot;

	private File nextFileTreeRoot;

	private File currentFileDisplayed;

	private File nextFileToDisplay;

	private SimpleUniverse simpleUniverse;

	private Background background = new Background();

	public Canvas3D canvas3D;

	//private JFrame win = new JFrame("Nif model");

	public NifDisplayTester(GLWindow gl_window)
	{
		NifToJ3d.SUPPRESS_EXCEPTIONS = false;
		//ASTC or DDS
		FileTextureSource.compressionType = FileTextureSource.CompressionType.ASTC;
		NiGeometryAppearanceFactoryShader.setAsDefault();
		ShaderSourceIO.SWAP_VER120_TO_VER100 = true;

		//win.setVisible(true);
		//win.setLocation(400, 0);
		//win.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		canvas3D = new Canvas3D(gl_window);

		simpleUniverse = new SimpleUniverse(canvas3D);
		/*		GraphicsSettings gs = ScreenResolution.organiseResolution(Preferences.userNodeForPackage(NifDisplayTester.class), win, false, true,
						true);
		
				canvas3D.getView().setSceneAntialiasingEnable(gs.isAaRequired());
				DDSTextureLoader.setAnisotropicFilterDegree(gs.getAnisotropicFilterDegree());
			*/
		//TODO: these must come form a new one of those ^
		//canvas3D.getGLWindow().setFullscreen(true);
		DDSTextureLoader.setAnisotropicFilterDegree(8);


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

		bg.addChild(fileManageBehavior);

		bg.addChild(spinTransformGroup);
		spinTransform = new SpinTransform(spinTransformGroup);
		spinTransform.setEnable(false);
		bg.addChild(spinTransform);

		background.setColor(0.8f, 0.8f, 0.8f);
		background.setApplicationBounds(null);
		background.setCapability(Background.ALLOW_APPLICATION_BOUNDS_WRITE);
		background.setCapability(Background.ALLOW_APPLICATION_BOUNDS_READ);
		bg.addChild(background);

		//bg.addChild(new Cube(0.01f));




		tg = new TransformGroup();
	 	t = new Transform3D(new Quat4f(0, 0, 0, 1), new Vector3f(10, 0, 0), 1);
		tg.setTransform(t);
		//tg.addChild(new Cube(0.1f));
		bg.addChild(tg);

		tg = new TransformGroup();
		t = new Transform3D(new Quat4f(0, 0, 0, 1), new Vector3f(-10, 0, 0), 1);
		tg.setTransform(t);
		//tg.addChild(new Cube(0.1f));
		bg.addChild(tg);

		tg = new TransformGroup();
		t = new Transform3D(new Quat4f(0, 0, 0, 1), new Vector3f(0, 0, 10), 1);
		tg.setTransform(t);
		//tg.addChild(new Cube(0.1f));
		bg.addChild(tg);

		tg = new TransformGroup();
		t = new Transform3D();
		t.rotY(Math.PI / 8);
		t.setTranslation(new Vector3f(0, 0, -5));
		tg.setTransform(t);
		tg.addChild(new Cube(0.1f));
		bg.addChild(tg);


		tg = new TransformGroup();
		t = new Transform3D(new Quat4f(0, 0, 0, 1), new Vector3f(0, 0, 10), 1);
		tg.setTransform(t);
		BranchGroup bgc = new BranchGroup();
		bgc.addChild(tg);
		bgc.setCapability(BranchGroup.ALLOW_DETACH);
		tg.addChild(new Cube(0.2f));
		spinTransformGroup.addChild(bgc);
		toggleSpin();

		simpleUniverse.addBranchGraph(bg);

		simpleUniverse.getViewer().getView().setBackClipDistance(5000);

		simpleUniverse.getCanvas().getGLWindow().addKeyListener(new KeyHandler());



	}

	public void setNextFileTreeRoot(File nextFileTreeRoot)
	{
		this.nextFileToDisplay = null;
		this.nextFileTreeRoot = nextFileTreeRoot;
	}

	public void setNextFileToDisplay(File nextFileToDisplay)
	{
		this.nextFileTreeRoot = null;
		this.nextFileToDisplay = nextFileToDisplay;
	}

	private void manage()
	{
		if (nextFileTreeRoot != null)
		{
			if (!nextFileTreeRoot.equals(currentFileTreeRoot))
			{
				currentFileTreeRoot = nextFileTreeRoot;
				currentFileDisplayed = null;
				currentFileLoadTime = Long.MAX_VALUE;
			}
		}

		if (currentFileTreeRoot != null)
		{
			if (cycle)
			{
				File[] files = currentFileTreeRoot.listFiles(new NifKfFileFilter());
				if (files!=null && files.length > 0)
				{
					if (currentFileDisplayed == null)
					{
						currentFileDisplayed = files[0];
					//	displayNif(currentFileDisplayed);
					}
					else if (System.currentTimeMillis() - currentFileLoadTime > 10000)
					{

					}
				}
			}
		}
		else if (nextFileToDisplay != null)
		{
			if (!nextFileToDisplay.equals(currentFileDisplayed))
			{
				currentFileDisplayed = nextFileToDisplay;
			//	displayNif(currentFileDisplayed);
				nextFileToDisplay = null;
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
		/*if (cycle)
		{
			// awake the directory processing thread
			synchronized (waitMonitor)
			{
				waitMonitor.notifyAll();
			}
		}*/
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
			showNif(f.getAbsolutePath(), new FileMeshSource(), new FileTextureSource());
		}

		System.out.println("done");

	}

	public void showNif(String filename, MeshSource meshSource, TextureSource textureSource)
	{
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

			if (showVisual && nif != null)
			{
				vbg.addChild(nif.getVisualRoot());
				modelGroup.addChild(vbg);
			}

			simpleCameraHandler.viewBounds(nif.getVisualRoot().getBounds());

			spinTransform.setEnable(spin);
		}
		else
		{
			System.out.println("why you give display a null eh?");
		}

	}







	private class FileManageBehavior extends Behavior
	{

		private WakeupCondition FPSCriterion = new WakeupOnElapsedFrames(0, false);

		public FileManageBehavior()
		{

			setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
			setEnable(true);
		}

		public void initialize()
		{
			wakeupOn(FPSCriterion);
		}

		@SuppressWarnings("rawtypes")
		public void processStimulus(Enumeration criteria)
		{
			process();
			wakeupOn(FPSCriterion);
		}

		private void process()
		{
			manage();
		}

	}

	private class KeyHandler extends KeyAdapter
	{

		public KeyHandler()
		{
			System.out.println("H toggle havok display");
			System.out.println("L toggle visual display");
			System.out.println("J toggle spin");
			System.out.println("K toggle animate model");
			System.out.println("P toggle background color");
			System.out.println("Space toggle cycle through files");
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
		}

	}

}