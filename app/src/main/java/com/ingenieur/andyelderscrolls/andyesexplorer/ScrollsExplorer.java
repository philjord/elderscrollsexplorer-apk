package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.app.Activity;

import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;

import java.io.File;
import java.io.IOException;
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
import nif.NifToJ3d;
import nif.appearance.NiGeometryAppearanceFactoryShader;
import scrollsexplorer.DashboardNewt;
import scrollsexplorer.GameConfig;
import scrollsexplorer.IDashboard;
import scrollsexplorer.PropertyLoader;
import scrollsexplorer.simpleclient.SimpleBethCellManager;
import scrollsexplorer.simpleclient.SimpleWalkSetup;
import tools.io.ConfigLoader;
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
import utils.source.file.FileTextureSource;

/**
 * Created by phil on 3/10/2016.
 */

public class ScrollsExplorer implements BethRenderSettings.UpdateListener, LocationUpdateListener
{
	//I think this auto installs itself
	public DashboardNewt dashboardNewt = new DashboardNewt();

	private SimpleBethCellManager simpleBethCellManager;

	private SimpleWalkSetup simpleWalkSetup;

	private MediaSources mediaSources;

	public IESMManager esmManager;

	public BSArchiveSet bsaFileSet;

	private GameConfig selectedGameConfig = null;

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

		ArchiveFile.USE_FILE_MAPS = false;

		FileTextureSource.compressionType = FileTextureSource.CompressionType.KTX;
		NiGeometryAppearanceFactoryShader.setAsDefault();
		ShaderSourceIO.SWAP_VER120_TO_VER100 = true;

		FileMediaRoots.setFixedRoot(rootDir.getAbsolutePath());

		try
		{
			PropertyLoader.load(rootDir.getAbsolutePath());

			simpleWalkSetup = new SimpleWalkSetup("SimpleBethCellManager", gl_window);
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
							bsaFileSet = new BSArchiveSet(new String[]{selectedGameConfig.scrollsFolder}, true, false);
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

	public SimpleWalkSetup getSimpleWalkSetup()
	{
		return simpleWalkSetup;
	}

	private static void setDebug(boolean b)
	{
		if (b)
		{
			System.out.println("DEBUG ON");
			// leave settings alone for optional debug parts
		}
		else
		{

		}
	}
}

