package com.ingenieur.andyelderscrolls.kfdisplay;


import android.app.Activity;
import android.os.Environment;
import android.widget.Toast;

import com.ingenieur.andyelderscrolls.utils.AndyFPSCounter;
import com.ingenieur.andyelderscrolls.utils.DragMouseAdapter;
import com.ingenieur.andyelderscrolls.utils.FileChooser;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.opengl.GLWindow;

import org.jogamp.java3d.AmbientLight;
import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.Light;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.PointLight;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.compressedtexture.CompressedTextureLoader;
import org.jogamp.java3d.utils.shader.Cube;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Quat4f;
import org.jogamp.vecmath.Vector3f;

import java.io.File;
import java.util.ArrayList;

import bsaio.ArchiveFile;
import bsaio.BSArchiveSet;
import bsa.source.BsaMeshSource;
import bsa.source.BsaTextureSource;
import nif.BgsmSource;
import nif.NifToJ3d;
import nif.appearance.NiGeometryAppearanceFactoryShader;
import nif.character.AttachedParts;
import nif.character.NifCharacter;
import nif.character.NifCharacterTes3;
import nif.character.NifJ3dSkeletonRoot;
import nif.j3d.J3dNiSkinInstance;
import nif.j3d.animation.tes3.J3dNiSequenceStreamHelper;
import scrollsexplorer.GameConfig;
import tools3d.camera.simple.SimpleCameraHandler;
import tools3d.mixed3d2d.Canvas3D2D;
import tools3d.utils.ShaderSourceIO;
import tools3d.utils.scenegraph.SpinTransform;
import utils.source.MediaSources;
import utils.source.MeshSource;
import utils.source.TextureSource;
import utils.source.file.FileMediaRoots;
import utils.source.file.FileMeshSource;
import utils.source.file.FileSoundSource;
import utils.source.file.FileTextureSource;

public class KfDisplayTester implements DragMouseAdapter.Listener
{
	private SimpleCameraHandler simpleCameraHandler;

	private TransformGroup spinTransformGroup = new TransformGroup();

	private TransformGroup rotateTransformGroup = new TransformGroup();

	private BranchGroup modelGroup = new BranchGroup();

	private SpinTransform spinTransform;

	/*private boolean cycle = true;

	private boolean showHavok = true;

	private boolean showVisual = true;

	private boolean animateModel = true;

	private boolean spin = false;*/

	private SimpleUniverse simpleUniverse;

	private Background background = new Background();

	public Canvas3D2D canvas3D2D;

	private AndyFPSCounter fpsCounter;

	private Activity parentActivity;

	private File chooserStartFolder;

	private static String skeletonNifModelFile;

	private static ArrayList<String> skinNifFiles = new ArrayList<String>();

	private DragMouseAdapter dragMouseAdapter = new DragMouseAdapter();

	private MeshSource meshSource = null;
	private TextureSource textureSource = null;

	private GameConfig gameConfigToLoad;

	public KfDisplayTester(Activity parentActivity2, GLWindow gl_window, GameConfig gameConfigToLoad)
	{
		this.gameConfigToLoad = gameConfigToLoad;
		File rootDir = new File(gameConfigToLoad.scrollsFolder);
		chooserStartFolder = new File(rootDir, "Meshes");
		this.parentActivity = parentActivity2;
		NifToJ3d.SUPPRESS_EXCEPTIONS = false;
		ArchiveFile.USE_FILE_MAPS = false;
		ArchiveFile.USE_MINI_CHANNEL_MAPS = true;
		ArchiveFile.USE_NON_NATIVE_ZIP = false;

		FileTextureSource.compressionType = FileTextureSource.CompressionType.KTX;
		NiGeometryAppearanceFactoryShader.setAsDefault();
		ShaderSourceIO.ES_SHADERS = true;

		BsaMeshSource.FALLBACK_TO_FILE_SOURCE = true;
		FileMediaRoots.setFixedRoot(rootDir.getAbsolutePath());

		meshSource = new FileMeshSource();

		String obbRoot = Environment.getExternalStorageDirectory() + "/Android/obb/" + parentActivity.getPackageName();
		String[] BSARoots = new String[]{rootDir.getAbsolutePath(), obbRoot};

		BSArchiveSet bsaFileSet = new BSArchiveSet(BSARoots, true);
		textureSource = new BsaTextureSource(bsaFileSet);

		canvas3D2D = new Canvas3D2D(gl_window);

		simpleUniverse = new SimpleUniverse(canvas3D2D);
		CompressedTextureLoader.setAnisotropicFilterDegree(8);

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
		spinTransform = new SpinTransform(spinTransformGroup, 0.2);
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
		t = new Transform3D(new Quat4f(0, 0, 0, 1), new Vector3f(0, 0, 2), 1);
		tg.setTransform(t);
		BranchGroup bgc = new BranchGroup();
		bgc.addChild(tg);
		bgc.setCapability(BranchGroup.ALLOW_DETACH);
		tg.addChild(new Cube(0.02f));
		spinTransformGroup.addChild(bgc);

		simpleUniverse.addBranchGraph(bg);

		canvas3D2D.getView().setBackClipDistance(5000);
		canvas3D2D.getView().setFrontClipDistance(0.1f);


		canvas3D2D.getGLWindow().addKeyListener(new KeyHandler());


		dragMouseAdapter.setListener(this);
		canvas3D2D.getGLWindow().addMouseListener(dragMouseAdapter);

		parentActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				Toast.makeText(parentActivity, "Please select a skeleton nif file", Toast.LENGTH_SHORT)
						.show();

				new FileChooser(parentActivity, chooserStartFolder).setExtension("nif").setFileListener(new FileChooser.FileSelectedListener()
				{
					@Override
					public void fileSelected(final File file)
					{
						chooserStartFolder = file;
						skeletonNifModelFile = file.getAbsolutePath();
						selectSkins();
					}

					@Override
					public void folderSelected(final File file)
					{

					}
				}).showDialog();
			}
		});
	}

	private void selectSkins()
	{

		Toast.makeText(parentActivity, "Please select skin(s) nif file(s)", Toast.LENGTH_SHORT)
				.show();
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

						/*File[] skinNifModelFiles = skinFc.getSelectedFiles();
						for (File skinNifModelFile : skinNifModelFiles)
						{
							skinNifFiles.add(skinNifModelFile.getCanonicalPath());
						}*/
						skinNifFiles.add(file.getAbsolutePath());

						//TODO: multi selection in file chooser, and directory selection maybe?
						//TODO: somehow allow null skin selection too

						Toast.makeText(parentActivity, "Please select anim to display", Toast.LENGTH_SHORT).show();
						selectKfFile();
					}

					@Override
					public void folderSelected(final File file)
					{

					}

				}).showDialog();
			}
		});

	}

	private void selectKfFile()
	{
		if (!gameConfigToLoad.folderKey.equals("MorrowindFolder"))
		{
			parentActivity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					new FileChooser(parentActivity, chooserStartFolder).setExtension("kf").setFileListener(new FileChooser.FileSelectedListener()
					{
						@Override
						public void fileSelected(final File file)
						{
							chooserStartFolder = file;
							display(skeletonNifModelFile, skinNifFiles, file);
						}

						@Override
						public void folderSelected(final File file)
						{

						}

					}).showDialog();
				}
			});

		}
		else
		{
			//morrowind has a single kf files named after sekeleton
			if (nifCharacterTes3 == null)
				displayTes3(skeletonNifModelFile, skinNifFiles);

			// now display all sequences from the kf file for user to pickage
			final J3dNiSequenceStreamHelper j3dNiSequenceStreamHelper = nifCharacterTes3.getJ3dNiSequenceStreamHelper();

			parentActivity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					new Tes3AnimChooser(parentActivity, "Anims availible", j3dNiSequenceStreamHelper.getAllSequences()).setAnimListener(new Tes3AnimChooser.AnimSelectedListener()
					{
						@Override
						public void animSelected(String anim)
						{
							nifCharacterTes3.startAnimation(anim, false);
						}
					}).showDialog();
				}
			});
		}
	}

	private void prevKfFile()
	{
		//TODO:
	}

	private void nextKfFile()
	{
		//TODO:
	}

	private void display(String skeletonNifFile, ArrayList<String> skinNifFiles2, File kff)
	{
		modelGroup.removeAllChildren();

		BranchGroup bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);

		NifJ3dSkeletonRoot.showBoneMarkers = true;
		J3dNiSkinInstance.showSkinBoneMarkers = false;//TODO: this doesn't show anything?

		BgsmSource.setBgsmSource(meshSource);
		MediaSources mediaSources = new MediaSources(meshSource, textureSource, new FileSoundSource());

		ArrayList<String> idleAnimations = new ArrayList<String>();

		if (kff != null)
		{
			idleAnimations.add(kff.getAbsolutePath());
		}

		// now add the root to the scene so the controller sequence is live
		NifCharacter nifCharacter = new NifCharacter(skeletonNifFile, skinNifFiles2, mediaSources, idleAnimations);
		nifCharacter.setCapability(Node.ALLOW_BOUNDS_READ);
		bg.addChild(nifCharacter);

		modelGroup.addChild(bg);

		simpleCameraHandler.viewBounds(nifCharacter.getBounds());

	}

	/**
	 * Note called once at start, not re called like display above
	 */
	private NifCharacterTes3 nifCharacterTes3;

	private void displayTes3(String skeletonNifFile, ArrayList<String> skinNifFiles2)
	{
		modelGroup.removeAllChildren();

		BranchGroup bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);

		NifJ3dSkeletonRoot.showBoneMarkers = true;
		J3dNiSkinInstance.showSkinBoneMarkers = false;//TODO: this doesn't show anything?

		BgsmSource.setBgsmSource(meshSource);
		MediaSources mediaSources = new MediaSources(meshSource, textureSource, new FileSoundSource());

		AttachedParts attachFileNames = new AttachedParts();
		attachFileNames.addPart(AttachedParts.Part.Root, skinNifFiles2.get(0));

		nifCharacterTes3 = new NifCharacterTes3(skeletonNifFile, attachFileNames, mediaSources);
		bg.addChild(nifCharacterTes3);

		modelGroup.addChild(bg);
		simpleCameraHandler.viewBounds(nifCharacterTes3.getBounds());
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
			selectKfFile();
		}
		else if (dragType == DragMouseAdapter.DRAG_TYPE.LEFT)
		{
			prevKfFile();
		}
		else if (dragType == DragMouseAdapter.DRAG_TYPE.RIGHT)
		{
			nextKfFile();
		}
	}

	private class KeyHandler extends KeyAdapter
	{

		public KeyHandler()
		{
		}

		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_N)
			{
				nextKfFile();
			}
			else if (e.getKeyCode() == KeyEvent.VK_M)
			{
				prevKfFile();
			}
		}

	}

}