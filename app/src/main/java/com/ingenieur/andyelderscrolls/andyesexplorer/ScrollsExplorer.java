package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.ingenieur.andyelderscrolls.ElderScrollsActivity;
import com.ingenieur.andyelderscrolls.utils.DragMouseAdapter;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;

import org.jogamp.vecmath.Quat4f;
import org.jogamp.vecmath.Vector3f;

import java.io.File;

import archive.ArchiveFile;
import archive.BSArchiveSet;
import bsa.source.BsaMeshSource;
import bsa.source.BsaSoundSource;
import bsa.source.BsaTextureSource;
import esmj3d.j3d.BethRenderSettings;
import esmj3d.j3d.j3drecords.inst.J3dLAND;
import esmj3dtes3.ai.Tes3AICREA;
import esmj3dtes3.j3d.cell.J3dCELL;
import esmmanager.loader.ESMManager;
import esmmanager.loader.IESMManager;
import nif.BgsmSource;
import nif.appearance.NiGeometryAppearanceFactoryShader;
import nif.character.NifCharacter;
import nif.j3d.J3dNiTriBasedGeom;
import nif.j3d.particles.tes3.J3dNiParticles;
import scrollsexplorer.DashboardNewt;
import scrollsexplorer.GameConfig;
import scrollsexplorer.IDashboard;
import scrollsexplorer.PropertyLoader;
import scrollsexplorer.simpleclient.BethWorldVisualBranch;
import scrollsexplorer.simpleclient.SimpleBethCellManager;
import scrollsexplorer.simpleclient.mouseover.MouseOverHandler;
import scrollsexplorer.simpleclient.physics.DynamicsEngine;
import scrollsexplorer.simpleclient.physics.PhysicsSystem;
import scrollsexplorer.simpleclient.tes3.Tes3Extensions;
import tools.compressedtexture.CompressedTextureLoader;
import tools3d.audio.SimpleSounds;
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

	private GameConfig selectedGameConfig = null;

	private DragMouseAdapter dragMouseAdapter = new DragMouseAdapter();

	private Activity parentActivity;

	private GameConfig gameConfigToLoad;

	private Tes3Extensions tes3Extensions;

	private MediaPlayer musicMediaPlayer;


	public ScrollsExplorer(Activity parentActivity2, GLWindow gl_window, String gameName, int gameConfigId)
	{

		this.parentActivity = parentActivity2;

		Camera.FRONT_CLIP = 0.2f;
		Camera.BACK_CLIP = 300f;
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
		BethRenderSettings.setEnablePlacedLights(false);
		BethWorldVisualBranch.LOAD_PHYS_FROM_VIS = true;
		DynamicsEngine.MAX_SUB_STEPS = 3;
		PhysicsSystem.MIN_TIME_BETWEEN_STEPS_MS = 40;
		NiGeometryAppearanceFactoryShader.setAsDefault();
		CompressedTextureLoader.setAnisotropicFilterDegree(4);
		ShaderSourceIO.ES_SHADERS = true;
		J3dNiTriBasedGeom.USE_FIXED_BOUNDS = true;
		NifCharacter.BULK_BUFFER_UPDATES = true;

		MouseOverHandler.MIN_TIME_BETWEEN_STEPS_MS = 500;
		MouseOverHandler.MAX_MOUSE_RAY_DIST = 20;

		BethWorldVisualBranch.FOG_START = 75;
		BethWorldVisualBranch.FOG_END = 150;

		gameConfigToLoad = ElderScrollsActivity.getGameConfig(gameName);

		if (gameConfigToLoad.folderKey.equals("MorrowindFolder"))
		{
			J3dLAND.setTes3();
			BethRenderSettings.setTes3(true);

			// for syda neen performance
			//J3dCELL.DO_DUMP = true;
			BethRenderSettings.setFarLoadGridCount(4);

			//long distance view
			//BethRenderSettings.setFarLoadGridCount(16);
			//BethRenderSettings.setFogEnabled(false);
		}
		else
		{
			BethRenderSettings.setFarLoadGridCount(0);
			BethRenderSettings.setNearLoadGridCount(2);
			BethRenderSettings.setLOD_LOAD_DIST_MAX(0);
		}
		FileMediaRoots.setFixedRoot(gameConfigToLoad.scrollsFolder);

		int startConfig = gameConfigId;
		// 0 inside boat
		// 1 outside boat
		// 2 combat
		// 3 vivec, third
		// 4 ald rhun,
		// 5 tel mora, cast spell in third
		// 6 inside cavern with azura, lots of people
		// 7 ghost gate, look at gate, turn walk along gully, transition to next
		// 8 nice green land walk along a road, transition to next
		// 9 dwarf ruins outside  along a bridge walk up behind crea


		if (startConfig == 0)
		{
			//scene  Imperial prison ship id 22668
			GameConfig.allGameConfigs.get(0).startCellId = 22668;
			GameConfig.allGameConfigs.get(0).startLocation = new Vector3f(1, -0.3f, 2);
			GameConfig.allGameConfigs.get(0).startYP = new YawPitch(Math.PI / 4, 0);
			musicToPlay = 1;//explore
			Tes3Extensions.HANDS = Tes3Extensions.hands.NONE;
		}
		else if (startConfig == 1)
		{
			// deck of start ship
			GameConfig.allGameConfigs.get(0).startCellId = 0;
			GameConfig.allGameConfigs.get(0).startLocation = new Vector3f(-108, 3, 936);
			GameConfig.allGameConfigs.get(0).startYP = new YawPitch(0, 0);//TODO:
			musicToPlay = 1;//explore
			Tes3Extensions.HANDS = Tes3Extensions.hands.NONE;

		}
		else if (startConfig == 2)
		{
			//dwarwen ruin for combat but odd sound issue
			// need to have axe and spell casting hands out and ready
			GameConfig.allGameConfigs.get(0).startCellId = 23903;//23042;
			GameConfig.allGameConfigs.get(0).startLocation = new Vector3f(2, -1, 18);//(57, 0, -17);
			GameConfig.allGameConfigs.get(0).startYP = new YawPitch(Math.PI / 8, 0);//TODO:
			musicToPlay = 2;//battle
			Tes3Extensions.HANDS = Tes3Extensions.hands.AXE;
			Tes3AICREA.combatDemo = true;
		}
		else if (startConfig == 3)
		{
			//vivec for third person view
			GameConfig.allGameConfigs.get(0).startCellId = 0;
			GameConfig.allGameConfigs.get(0).startLocation = new Vector3f(423, 8, 1079);
			GameConfig.allGameConfigs.get(0).startYP = new YawPitch(0, 0);//TODO:
			musicToPlay = 1;//explore
			Tes3Extensions.HANDS = Tes3Extensions.hands.NONE;
			AndySimpleWalkSetup.TRAILER_CAM = true;
		}
		else if (startConfig == 4)
		{
			// ald rhun
			GameConfig.allGameConfigs.get(0).startCellId = 0;
			GameConfig.allGameConfigs.get(0).startLocation = new Vector3f(-152, 31, -682);
			GameConfig.allGameConfigs.get(0).startYP = new YawPitch(0, 0);//TODO:
			musicToPlay = 1;//explore
			Tes3Extensions.HANDS = Tes3Extensions.hands.AXE;
		}
		else if (startConfig == 5)
		{
			//tel mora  , cast spell in third
			GameConfig.allGameConfigs.get(0).startCellId = 0;
			GameConfig.allGameConfigs.get(0).startLocation = new Vector3f(1387, 18, -1438);
			GameConfig.allGameConfigs.get(0).startYP = new YawPitch(Math.PI / 8, 0);//TODO:
			musicToPlay = 1;//explore
			Tes3Extensions.HANDS = Tes3Extensions.hands.SPELL;
			AndySimpleWalkSetup.TRAILER_CAM = true;
		}
		else if (startConfig == 6)
		{
			//inside cavern with azura
			GameConfig.allGameConfigs.get(0).startCellId = 22087;
			GameConfig.allGameConfigs.get(0).startLocation = new Vector3f(0, 0, 16);
			GameConfig.allGameConfigs.get(0).startYP = new YawPitch(0, 0);//TODO:
			Tes3Extensions.HANDS = Tes3Extensions.hands.NONE;
			musicToPlay = 1;//explore
		}
		else if (startConfig == 7)
		{
			//  ghost gate, look the walk down gully
			GameConfig.allGameConfigs.get(0).startCellId = 0;
			GameConfig.allGameConfigs.get(0).startLocation = new Vector3f(256, 11, -460);
			GameConfig.allGameConfigs.get(0).startYP = new YawPitch(0, 0);//TODO:
			Tes3Extensions.HANDS = Tes3Extensions.hands.SPELL;
			musicToPlay = 1;//explore
		}
		else if (startConfig == 8)
		{
			//nice green land walk along a road, transition to next
			GameConfig.allGameConfigs.get(0).startCellId = 0;
			GameConfig.allGameConfigs.get(0).startLocation = new Vector3f(896, 12, -1472);
			GameConfig.allGameConfigs.get(0).startYP = new YawPitch(0, 0);//TODO:
			Tes3Extensions.HANDS = Tes3Extensions.hands.NONE;
			musicToPlay = 1;//explore
		}
		else if (startConfig == 9)
		{
			//   dwarf ruins outside along a bridge walk up behind crea
			GameConfig.allGameConfigs.get(0).startCellId = 0;
			GameConfig.allGameConfigs.get(0).startLocation = new Vector3f(-183, 49, -1059);
			GameConfig.allGameConfigs.get(0).startYP = new YawPitch(0, 0);//TODO:
			Tes3Extensions.HANDS = Tes3Extensions.hands.AXE;
			musicToPlay = 2;//battle
		}


		//Android TESIV: Oblivion = 143176?, (425,43,-912)
		GameConfig.allGameConfigs.get(1).startCellId = 180488;
		GameConfig.allGameConfigs.get(1).startLocation = new Vector3f(425, 43, -912);

		//Android FO3: Fallout 3 = 2676, (-37, 165, 281)
		GameConfig.allGameConfigs.get(2).startCellId = 2676;
		GameConfig.allGameConfigs.get(2).startLocation = new Vector3f(-37, 165, 281);

		//Android FONV: Fallout New Vegas = 1064441, (23, 94, -24)
		GameConfig.allGameConfigs.get(3).startCellId = 1064441;
		GameConfig.allGameConfigs.get(3).startLocation = new Vector3f(23, 94, -24);

		//Android TESV: Skyrim = 107119, (251, -44, 94)
		GameConfig.allGameConfigs.get(4).startCellId = 107119;
		GameConfig.allGameConfigs.get(4).startLocation = new Vector3f(251, -44, 94);

		//Android FO4: Fallout 4 = 7768, (19, 1, 5)
		GameConfig.allGameConfigs.get(5).startCellId = 7768;
		GameConfig.allGameConfigs.get(5).startLocation = new Vector3f(19, 1, 5);


		simpleWalkSetup = new AndySimpleWalkSetup("SimpleBethCellManager", gl_window);
		simpleWalkSetup.setAzerty(false);

		simpleBethCellManager = new SimpleBethCellManager(simpleWalkSetup);

		BethRenderSettings.addUpdateListener(this);

		simpleWalkSetup.getAvatarLocation().addAvatarLocationListener(this);

		dragMouseAdapter.setListener(this);

		// start teh actual game up!
		if (hasESMAndBSAFiles(gameConfigToLoad))
		{
			setSelectedGameConfig(gameConfigToLoad);
		}
		else
		{
			Toast.makeText(parentActivity2, "But it's not setup correctly! where are teh bsa and esm files?", Toast.LENGTH_LONG)
					.show();
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
						//YawPitch yp = YawPitch
						//		.parse(PropertyLoader.properties.getProperty("YawPitch" + esmManager.getName(), selectedGameConfig.startYP.toString()));
						//Vector3f trans = PropertyCodec.vector3fOut(PropertyLoader.properties.getProperty("Trans" + esmManager.getName(),
						//		selectedGameConfig.startLocation.toString()));
						YawPitch yp = selectedGameConfig.startYP;
						Vector3f trans = selectedGameConfig.startLocation;

						int prevCellformid = Integer.parseInt(PropertyLoader.properties.getProperty("CellId" + esmManager.getName(), "-1"));
						simpleWalkSetup.getAvatarLocation().set(yp.get(new Quat4f()), trans);

						//FIXME: in order to allow me to jump wherever I want, I ignore the properties gear
						//if (prevCellformid == -1)
						{
							prevCellformid = selectedGameConfig.startCellId;

						}

						new EsmSoundKeyToName(esmManager);
						MeshSource meshSource;
						TextureSource textureSource;
						SoundSource soundSource;

						if (bsaFileSet == null)
						{

							//The specific location for your expansion files is:
							//<shared-storage>/Android/obb/<package-name>/
							//<shared-storage> is the path to the shared storage space, available from getExternalStorageDirectory().
							//	<package-name> is your application's Java-style package name, available from getPackageName().
							String obbRoot = Environment.getExternalStorageDirectory() + "/Android/obb/" + parentActivity.getPackageName();
							String[] BSARoots = new String[]{selectedGameConfig.scrollsFolder, obbRoot};


							bsaFileSet = new BSArchiveSet(BSARoots, true);
						}

						if (bsaFileSet.size() == 0)
						{
							System.err.println("bsa files size is 0 :(");
							IDashboard.dashboard.setEsmLoading(-1);
							return;
						}

						meshSource = new BsaMeshSource(bsaFileSet);
						textureSource = new BsaTextureSource(bsaFileSet);
						soundSource = new BsaSoundSource(bsaFileSet, null);//new EsmSoundKeyToName(esmManager));

						//Just for the crazy new fallout 4 system
						BgsmSource.setBgsmSource(meshSource);

						mediaSources = new MediaSources(meshSource, textureSource, soundSource);

						simpleWalkSetup.configure(meshSource, simpleBethCellManager);
						simpleWalkSetup.setEnabled(false);

						simpleWalkSetup.getWindow().addKeyListener(new KeyHandler());
						simpleWalkSetup.getWindow().addMouseListener(dragMouseAdapter);
						simpleWalkSetup.getWindow().addWindowListener(new WindowAdapter()
						{
							@Override
							public void windowResized(final WindowEvent e)
							{
								J3dNiParticles.setScreenWidth(simpleWalkSetup.getWindow().getWidth());
							}

						});
						J3dNiParticles.setScreenWidth(simpleWalkSetup.getWindow().getWidth());
						simpleWalkSetup.setMouseLock(true);// auto press teh tab key

						// I could use the j3dcellfactory now? with the cached cell records?
						simpleBethCellManager.setSources(selectedGameConfig, esmManager, mediaSources);

						if (selectedGameConfig == GameConfig.allGameConfigs.get(0))
						{
							System.out.println("Adding Tes3 extensions");
							tes3Extensions = new Tes3Extensions(selectedGameConfig, esmManager, mediaSources, simpleWalkSetup,
									simpleBethCellManager);
						}

						display(prevCellformid);

						if (gameConfigToLoad.folderKey.equals("MorrowindFolder"))
						{
							// this is how you play a mp3  file. the setDataSource
							// version doesn't seem to work, possibly the activity is the key
							if (musicToPlay > 0)
							{
								if (musicToPlay == 1)
								{
									//1-7
									int piece = (int) (Math.random() * 7) + 1;
									musicMediaPlayer = MediaPlayer.create(parentActivity, Uri.fromFile(new File(gameConfigToLoad.scrollsFolder + "/Music/Explore/mx_explore_" + piece + ".mp3")));
								}
								else if (musicToPlay == 2)
								{
									// notice extra spaces in some battle mp3 names
									musicMediaPlayer = MediaPlayer.create(parentActivity, Uri.fromFile(new File(gameConfigToLoad.scrollsFolder + "/Music/Battle/MW battle1.mp3")));
								}
								musicMediaPlayer.setVolume(0.15f, 0.15f);
								musicMediaPlayer.start();
							}

							SimpleSounds.mp3SystemMediaPlayer = new
									SimpleSounds.Mp3SystemMediaPlayer()
									{
										MediaPlayer musicMediaPlayer2;

										@Override
										public void playAnMp3(String s, float v)
										{

											s = s.replace("\\", "/");
											if (!s.startsWith("/"))
												s = "/" + s;

											File f = new File(gameConfigToLoad.scrollsFolder + s);
											System.out.println("does mp3 exist? " + f.exists() + " " + f);


											musicMediaPlayer2 = MediaPlayer.create(parentActivity, Uri.fromFile(new File(gameConfigToLoad.scrollsFolder + s)));

											musicMediaPlayer2.setVolume(v, v);
											musicMediaPlayer2.setLooping(false);
											musicMediaPlayer2.start();
										}
									};

						}


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


	private int musicToPlay = 0;//0=none,1=explore,2=battle

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

