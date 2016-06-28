package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.app.Activity;

import com.ingenieur.andyelderscrolls.utils.AndyFPSCounter;
import com.ingenieur.andyelderscrolls.utils.DragMouseAdapter;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.opengl.GLWindow;

import java.io.File;
import java.util.HashMap;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import archive.ArchiveFile;
import archive.BSArchiveSet;
import bsa.source.BsaMeshSource;
import bsa.source.BsaSoundSource;
import bsa.source.BsaTextureSource;
import esmj3d.j3d.BethRenderSettings;
import esmmanager.loader.ESMManager;
import esmmanager.loader.IESMManager;
import nif.BgsmSource;
import nif.appearance.NiGeometryAppearanceFactoryShader;
import nif.character.NifCharacter;
import nif.j3d.J3dNiGeometry;
import nif.j3d.J3dNiTriBasedGeom;
import scrollsexplorer.DashboardNewt;
import scrollsexplorer.GameConfig;
import scrollsexplorer.IDashboard;
import scrollsexplorer.PropertyLoader;
import scrollsexplorer.simpleclient.BethWorldVisualBranch;
import scrollsexplorer.simpleclient.SimpleBethCellManager;
import scrollsexplorer.simpleclient.mouseover.MouseOverHandler;
import scrollsexplorer.simpleclient.physics.DynamicsEngine;
import scrollsexplorer.simpleclient.physics.PhysicsSystem;
import tools.compressedtexture.CompressedTextureLoader;
import tools3d.camera.Camera;
import tools3d.utils.ShaderSourceIO;
import tools3d.utils.YawPitch;
import tools3d.utils.loader.PropertyCodec;
import tools3d.utils.scenegraph.LocationUpdateListener;
import utils.source.EsmSoundKeyToName;
import utils.source.MediaSources;
import utils.source.MeshSource;
import utils.source.SoundSource;
import utils.source.TextureSource;
import utils.source.file.FileMediaRoots;

/**
 * Created by phil on 3/10/2016.
 */

public class ScrollsExplorer implements BethRenderSettings.UpdateListener, LocationUpdateListener, DragMouseAdapter.Listener
{
	//I think this auto installs itself
	public DashboardNewt dashboardNewt = new DashboardNewt();

	private SimpleBethCellManager simpleBethCellManager;

	public AndySimpleWalkSetup simpleWalkSetup;

	private MediaSources mediaSources;

	public IESMManager esmManager;

	public BSArchiveSet bsaFileSet;

	private AndyFPSCounter fpsCounter;

	private GameConfig selectedGameConfig = null;

	private DragMouseAdapter dragMouseAdapter = new DragMouseAdapter();

	private Activity parentActivity;

	private File rootDir;

	private static HashMap<String, String> rootToGameName = new HashMap<String, String>();

	static
	{
		rootToGameName.put("Morrowind", "TESIII: Morrowind");
		rootToGameName.put("Oblivion", "TESIV: Oblivion");
		rootToGameName.put("Fallout3", "FO3: Fallout 3");
		rootToGameName.put("FalloutNV", "FONV: Fallout New Vegas");
		rootToGameName.put("Skyrim", "TESV: Skyrim");
		rootToGameName.put("Fallout4", "FO4: Fallout 4");
	}

	public ScrollsExplorer(Activity parentActivity2, GLWindow gl_window, File rootDir)
	{
		this.rootDir = rootDir;
		this.parentActivity = parentActivity2;

		Camera.FRONT_CLIP = 0.2f;
		Camera.BACK_CLIP = 1000f;
		Camera.MIN_FRAME_CYCLE_TIME = 15;

		ESMManager.USE_FILE_MAPS = false;
		ESMManager.USE_MINI_CHANNEL_MAPS = true;
		ESMManager.USE_NON_NATIVE_ZIP = false;

		ArchiveFile.USE_FILE_MAPS = false;
		ArchiveFile.USE_MINI_CHANNEL_MAPS = true;
		ArchiveFile.USE_NON_NATIVE_ZIP = false;

		BethRenderSettings.setFarLoadGridCount(4);
		BethRenderSettings.setNearLoadGridCount(2);
		BethRenderSettings.setLOD_LOAD_DIST_MAX(32);
		BethRenderSettings.setObjectFade(100);
		BethRenderSettings.setItemFade(60);
		BethRenderSettings.setActorFade(35);
		BethRenderSettings.setOutlineFocused(false);
		BethWorldVisualBranch.LOAD_PHYS_FROM_VIS = true;
		DynamicsEngine.MAX_SUB_STEPS = 3;
		PhysicsSystem.MIN_TIME_BETWEEN_STEPS_MS = 40;
		NiGeometryAppearanceFactoryShader.setAsDefault();
		CompressedTextureLoader.setAnisotropicFilterDegree(4);
		ShaderSourceIO.ES_SHADERS = true;
		J3dNiTriBasedGeom.USE_FIXED_BOUNDS = true;
		// this definately doesn't help on desktop, but lots of methods calls so maybe?
		NifCharacter.BULK_BUFFER_UPDATES = true;

		MouseOverHandler.MIN_TIME_BETWEEN_STEPS_MS = 500;
		MouseOverHandler.MAX_MOUSE_RAY_DIST = 20;

		BethWorldVisualBranch.FOG_START = 75;
		BethWorldVisualBranch.FOG_END = 150;


		//fallout dies from memory
		if (rootDir.getName().equals("Fallout3"))
		{
			BethRenderSettings.setFarLoadGridCount(0);
			BethRenderSettings.setNearLoadGridCount(1);
			BethRenderSettings.setLOD_LOAD_DIST_MAX(0);
		}

		//for big games go low spec
		if (!rootDir.getName().equals("Morrowind"))
		{
			BethRenderSettings.setFarLoadGridCount(0);
			BethRenderSettings.setNearLoadGridCount(2);
			BethRenderSettings.setLOD_LOAD_DIST_MAX(0);
		}
		FileMediaRoots.setFixedRoot(rootDir.getAbsolutePath());
		try
		{
			PropertyLoader.load(parentActivity.getFilesDir().getAbsolutePath());

			simpleWalkSetup = new AndySimpleWalkSetup("SimpleBethCellManager", gl_window);
			simpleWalkSetup.setAzerty(false);

			simpleBethCellManager = new SimpleBethCellManager(simpleWalkSetup);

			BethRenderSettings.addUpdateListener(this);


			simpleWalkSetup.getAvatarLocation().addAvatarLocationListener(this);


			String gameToLoad = rootToGameName.get(rootDir.getName());

			for (GameConfig gameConfig : GameConfig.allGameConfigs)
			{
				System.out.println("checking against " + gameConfig.gameName);
				if (gameConfig.gameName.equals(gameToLoad))
				{
					System.out.println("Found game to load! " + gameConfig.gameName);
					gameConfig.scrollsFolder = rootDir.getAbsolutePath();
					if (hasESMAndBSAFiles(gameConfig))
					{
						setSelectedGameConfig(gameConfig);
					}
					else
					{
						System.out.println("But it's not setup correctly!");
					}
					break;
				}
			}


			dragMouseAdapter.setListener(this);


		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}


	public void closingTime()
	{
		if (esmManager != null)
		{
			PropertyLoader.properties.setProperty("YawPitch" + esmManager.getName(),
					new YawPitch(simpleWalkSetup.getAvatarLocation().getTransform()).toString());
			PropertyLoader.properties.setProperty("Trans" + esmManager.getName(),
					"" + PropertyCodec.vector3fIn(simpleWalkSetup.getAvatarLocation().get(new Vector3f())));
			PropertyLoader.properties.setProperty("CellId" + esmManager.getName(), "" + simpleBethCellManager.getCurrentCellFormId());
		}
		PropertyLoader.save();

	}

	private static boolean hasESMAndBSAFiles(GameConfig gameConfig)
	{
		// check to ensure the esm file and at least one bsa file are in the folder
		File checkEsm = new File(gameConfig.scrollsFolder, gameConfig.mainESMFile);
		if (!checkEsm.exists())
		{
			return false;
		}

		int countOfBsa = 0;
		File checkBsa = new File(gameConfig.scrollsFolder);
		for (File f : checkBsa.listFiles())
		{
			countOfBsa += f.getName().toLowerCase().endsWith(".bsa") ? 1 : 0;
			countOfBsa += f.getName().toLowerCase().endsWith(".ba2") ? 1 : 0;
		}

		if (countOfBsa == 0)
		{
			return false;
		}

		return true;
	}

	@Override
	public void renderSettingsUpdated()
	{
		simpleBethCellManager.updateBranches();
	}

	/**

	 */
	private void setSelectedGameConfig(GameConfig newGameConfig)
	{
		selectedGameConfig = newGameConfig;
		simpleWalkSetup.getAvatarCollisionInfo().setAvatarYHeight(selectedGameConfig.avatarYHeight);

		Thread t = new Thread()
		{
			public void run()
			{
				synchronized (selectedGameConfig)
				{
					IDashboard.dashboard.setEsmLoading(1);

					esmManager = ESMManager.getESMManager(selectedGameConfig.getESMPath());
					bsaFileSet = null;
					if (esmManager != null)
					{
						YawPitch yp = YawPitch
								.parse(PropertyLoader.properties.getProperty("YawPitch" + esmManager.getName(), new YawPitch().toString()));
						Vector3f trans = PropertyCodec.vector3fOut(PropertyLoader.properties.getProperty("Trans" + esmManager.getName(),
								selectedGameConfig.startLocation.toString()));
						int prevCellformid = Integer.parseInt(PropertyLoader.properties.getProperty("CellId" + esmManager.getName(), "-1"));
						simpleWalkSetup.getAvatarLocation().set(yp.get(new Quat4f()), trans);

						if (prevCellformid == -1)
						{
							prevCellformid = selectedGameConfig.startCellId;
						}

						new EsmSoundKeyToName(esmManager);
						MeshSource meshSource;
						TextureSource textureSource;
						SoundSource soundSource;

						if (bsaFileSet == null)
						{
							bsaFileSet = new BSArchiveSet(new String[]{selectedGameConfig.scrollsFolder}, true);
						}

						if (bsaFileSet.size() == 0)
						{
							System.err.println("bsa files size is 0 :(");
							IDashboard.dashboard.setEsmLoading(-1);
							return;
						}

						meshSource = new BsaMeshSource(bsaFileSet);
						textureSource = new BsaTextureSource(bsaFileSet);
						soundSource = new BsaSoundSource(bsaFileSet, new EsmSoundKeyToName(esmManager));

						//Just for the crazy new fallout 4 system
						BgsmSource.setBgsmSource(meshSource);

						mediaSources = new MediaSources(meshSource, textureSource, soundSource);

						simpleWalkSetup.configure(meshSource, simpleBethCellManager);
						simpleWalkSetup.setEnabled(false);

						simpleWalkSetup.getWindow().addKeyListener(new KeyHandler());
						simpleWalkSetup.getWindow().addMouseListener(dragMouseAdapter);
						simpleWalkSetup.setMouseLock(true);// auto press teh tab key

						// I could use the j3dcellfactory now? with the cached cell records?
						simpleBethCellManager.setSources(selectedGameConfig, esmManager, mediaSources);

						display(prevCellformid);

					}
					else
					{
						System.out.println("esm manger is null, I just don't know why...");
					}

					IDashboard.dashboard.setEsmLoading(-1);
				}

			}
		};
		t.start();
	}

	@Override
	public void locationUpdated(Quat4f rot, Vector3f trans)
	{

	}

	private void display(final int cellformid)
	{
		Vector3f t = simpleWalkSetup.getAvatarLocation().get(new Vector3f());
		Quat4f r = simpleWalkSetup.getAvatarLocation().get(new Quat4f());
		simpleBethCellManager.setCurrentCellFormId(cellformid, t, r);
	}

	public SimpleBethCellManager getSimpleBethCellManager()
	{
		return simpleBethCellManager;
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

		}
		else if (dragType == DragMouseAdapter.DRAG_TYPE.LEFT)
		{

		}
		else if (dragType == DragMouseAdapter.DRAG_TYPE.RIGHT)
		{
		}
	}

	public void stopRenderer()
	{
		simpleWalkSetup.stopRenderer();
	}

	public void startRenderer(GLWindow gl_window)
	{
		simpleWalkSetup.startRenderer(gl_window);
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
			if (e.getKeyCode() == KeyEvent.VK_1)
			{
				BethRenderSettings.setOutlineFocused(!BethRenderSettings.isOutlineFocused());
			}
			else if (e.getKeyCode() == KeyEvent.VK_2)
			{
				BethRenderSettings.setOutlineChars(!BethRenderSettings.isOutlineChars());
			}
			else if (e.getKeyCode() == KeyEvent.VK_3)
			{
				BethRenderSettings.setOutlineConts(!BethRenderSettings.isOutlineConts());
			}
			else if (e.getKeyCode() == KeyEvent.VK_4)
			{
				BethRenderSettings.setOutlineDoors(!BethRenderSettings.isOutlineDoors());
			}
			else if (e.getKeyCode() == KeyEvent.VK_5)
			{
				BethRenderSettings.setOutlineParts(!BethRenderSettings.isOutlineParts());
			}

		}
	}
}

