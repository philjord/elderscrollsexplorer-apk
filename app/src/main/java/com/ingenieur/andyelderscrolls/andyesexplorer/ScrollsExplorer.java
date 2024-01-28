package com.ingenieur.andyelderscrolls.andyesexplorer;

import static scrollsexplorer.GameConfig.allGameConfigs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.FragmentActivity;

import com.amrdeveloper.treeview.TreeNode;
import com.ingenieur.andyelderscrolls.ElderScrollsActivity;
import com.ingenieur.andyelderscrolls.MorrowindActivity;
import com.ingenieur.andyelderscrolls.R;
import com.ingenieur.andyelderscrolls.andyesexplorer.games.fallout3.Fallout3MapImage;
import com.ingenieur.andyelderscrolls.andyesexplorer.games.fallout4.Fallout4MapImage;
import com.ingenieur.andyelderscrolls.andyesexplorer.games.falloutnv.FalloutNVMapImage;
import com.ingenieur.andyelderscrolls.andyesexplorer.games.morrowind.MorrowindMapImage;
import com.ingenieur.andyelderscrolls.andyesexplorer.games.oblivion.OblivionMapImage;
import com.ingenieur.andyelderscrolls.andyesexplorer.games.skyrim.SkyrimMapImage;
import com.ingenieur.andyelderscrolls.andyesexplorer.games.starfield.StarfieldMapImage;
import com.ingenieur.andyelderscrolls.utils.DragMouseAdapter;
import com.ingenieur.andyelderscrolls.utils.ESMCellChooser;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;

import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.compressedtexture.CompressedTextureLoader;
import org.jogamp.vecmath.Quat4f;
import org.jogamp.vecmath.Vector3f;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import bsa.source.BsaMeshSource;
import bsa.source.BsaSoundSource;
import bsa.source.BsaTextureSource;
import bsa.source.DDSToKTXBsaConverter;
import bsaio.ArchiveFile;
import bsaio.BSArchiveSet;
import bsaio.BSArchiveSetUri;
import bsaio.BsaUtils;
import bsaio.DBException;
import esfilemanager.common.data.plugin.PluginGroup;
import esfilemanager.common.data.record.Record;
import esfilemanager.loader.ESMManager;
import esfilemanager.loader.ESMManagerUri;
import esfilemanager.loader.IESMManager;
import esfilemanager.utils.ESMUtils;
import esfilemanager.utils.source.EsmSoundKeyToName;
import esmj3d.data.shared.records.CommonREFR;
import esmj3d.data.shared.subrecords.XTEL;
import esmj3d.j3d.BethRenderSettings;
import esmj3d.j3d.cell.J3dICellFactory;
import esmj3d.j3d.j3drecords.inst.J3dLAND;
import javaawt.VMEventQueue;
import javaawt.image.VMBufferedImage;
import javaawt.imageio.VMImageIO;
import nif.BgsmSource;
import nif.appearance.NiGeometryAppearanceFactoryShader;
import nif.character.NifCharacter;
import nif.j3d.J3dNiTriBasedGeom;
import nif.j3d.particles.tes3.J3dNiParticles;
import nif.shader.ShaderSourceIO;
import scrollsexplorer.DashboardNewt;
import scrollsexplorer.GameConfig;
import scrollsexplorer.IDashboard;
import scrollsexplorer.PropertyLoader;
import scrollsexplorer.simpleclient.BethWorldVisualBranch;
import scrollsexplorer.simpleclient.SimpleBethCellManager;
import scrollsexplorer.simpleclient.mouseover.ActionableMouseOverHandler;
import scrollsexplorer.simpleclient.mouseover.MouseOverHandler;
import scrollsexplorer.simpleclient.physics.DynamicsEngine;
import scrollsexplorer.simpleclient.physics.PhysicsSystem;
import scrollsexplorer.simpleclient.tes3.Tes3Extensions;
import tools3d.audio.SimpleSounds;
import tools3d.camera.Camera;
import tools3d.utils.YawPitch;
import tools3d.utils.loader.PropertyCodec;
import tools3d.utils.scenegraph.LocationUpdateListener;
import utils.source.MediaSources;
import utils.source.MeshSource;
import utils.source.SoundSource;

/**
 * Created by phil on 3/10/2016.
 */

public class ScrollsExplorer
        //TODO: extends ScrollsExplorerNewt
        implements BethRenderSettings.UpdateListener, LocationUpdateListener, DragMouseAdapter.Listener {
    //I think this auto installs itself
    public DashboardNewt dashboardNewt = new DashboardNewt();

    private SimpleBethCellManager simpleBethCellManager;

    private ESMCellChooser esmCellChooser;

    public AndySimpleWalkSetup simpleWalkSetup;

    private MediaSources mediaSources;

    public IESMManager esmManager;

    public BSArchiveSet bsaFileSet;

    private GameConfig selectedGameConfig = null;

    private DragMouseAdapter dragMouseAdapter = new DragMouseAdapter();

    private FragmentActivity parentActivity;

    private AndyESExplorerFragment parentFragment;

    private GameConfig gameConfigToLoad;

    private Tes3Extensions tes3Extensions;

    private boolean stayAlive = true;

    private MediaPlayer musicMediaPlayer;

    private boolean saveLoadConfig = false;
    private ProgressBar progressBar;
    private MapFragment.MapImageInterface map;

    // to chase memory leaks
    protected void finalize() throws Throwable {
        try {
            System.out.println("ONE ScrollsExplorer CLEANED UP");
        } finally {
            super.finalize();
        }
    }

    public ScrollsExplorer(FragmentActivity parentActivity2, GLWindow gl_window, String gameName, int gameConfigId, AndyESExplorerFragment parentFragment) {

        System.out.println("ONE ScrollsExplorer CREATED");

        this.parentActivity = parentActivity2;
        this.parentFragment = parentFragment;

        progressBar = (ProgressBar) parentActivity.findViewById(R.id.progressBar);

        Camera.FRONT_CLIP = 0.2f;
        Camera.BACK_CLIP = 300f;
        Camera.MIN_FRAME_CYCLE_TIME = 15;

        ESMManager.USE_FILE_MAPS = false;
        ESMManager.USE_MINI_CHANNEL_MAPS = true;
        ESMManager.USE_NON_NATIVE_ZIP = false;

        ArchiveFile.USE_FILE_MAPS = false;
        ArchiveFile.USE_MINI_CHANNEL_MAPS = true;
        ArchiveFile.USE_NON_NATIVE_ZIP = false;

        // only KTX, if DDS is found it will convert ot KTX on the fly
        BsaTextureSource.allowedTextureFormats = BsaTextureSource.AllowedTextureFormats.KTX;
        BsaTextureSource.CompressedTextureLoaderETCPackDDS.CONVERT_DDS_TO_ETC2 = true;// always convert if we don't find ktx

        javaawt.image.BufferedImage.installBufferedImageDelegate(VMBufferedImage.class);
        javaawt.imageio.ImageIO.installBufferedImageImpl(VMImageIO.class);
        javaawt.EventQueue.installEventQueueImpl(VMEventQueue.class);

        BethRenderSettings.setOutlineFocused(true);
        BethRenderSettings.setEnablePlacedLights(true);
        BethWorldVisualBranch.LOAD_PHYS_FROM_VIS = true;//make false to see the red lines!
        DynamicsEngine.MAX_SUB_STEPS = 3;
        PhysicsSystem.MIN_TIME_BETWEEN_STEPS_MS = 40;
        NiGeometryAppearanceFactoryShader.setAsDefault();
        CompressedTextureLoader.setAnisotropicFilterDegree(4);
        ShaderSourceIO.ES_SHADERS = true;
        J3dNiTriBasedGeom.USE_FIXED_BOUNDS = true;
        NifCharacter.BULK_BUFFER_UPDATES = true;

        MouseOverHandler.MIN_TIME_BETWEEN_STEPS_MS = 500;
        MouseOverHandler.MAX_MOUSE_RAY_DIST = 20;

        gameConfigToLoad = ElderScrollsActivity.getGameConfig(gameName);
        //record the cell request
        if (gameConfigId == -1)
            gameConfigToLoad.startCellId = -1;

        BsaMeshSource.FALLBACK_TO_FILE_SOURCE = false;

        if (gameConfigToLoad.folderKey.equals("MorrowindFolder")) {

            gameConfigToLoad.musicToPlayId = 0;

            MorrowindActivity.organiseMorrowindPreselectedConfigs(gameConfigId);

            J3dLAND.setTes3();
            BethRenderSettings.setTes3(true);
        }

        // default to none
        Tes3Extensions.HANDS = Tes3Extensions.hands.NONE;

        simpleWalkSetup = new AndySimpleWalkSetup("SimpleBethCellManager", gl_window);
        simpleWalkSetup.setAzerty(false);

        simpleBethCellManager = new SimpleBethCellManager(simpleWalkSetup);

        BethRenderSettings.addUpdateListener(this);

        simpleWalkSetup.getAvatarLocation().addAvatarLocationListener(this);

        dragMouseAdapter.setListener(this);

       // start the actual game up!
        if (hasESMAndBSAFiles(gameConfigToLoad)) {
            setSelectedGameConfig(gameConfigToLoad);
        } else {
            Looper.prepare();
            Toast.makeText(parentActivity2, "Failed to load " + gameConfigToLoad.gameName + " can't find the bsa and esm files!", Toast.LENGTH_LONG)
                    .show();
        }

        //TODO: do I still need this guy?

        // so there is a lack of non daemon threads see jogamp.newt.driver.awt.AWTEDTUtil for example
        // so with a pure Newt world I have to keep the app alive with my own non daemon useless keep alive thread!
        // closing time has to kill it
        // the real solution is to find out why jogl doesn't provide a non daemon EDT thread for GLWindow seems strange

        Thread newtKeepAliveThread = new Thread() {
            @Override
            public void run() {
                while (stayAlive) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        newtKeepAliveThread.setDaemon(false);// in case a daemon parent
        newtKeepAliveThread.setName("Newt Keep Alive Thread");
        newtKeepAliveThread.start();
    }


    public void closingTime() {
        if (esmManager != null) {
            // don't write out location unless we started the save load version up
            if (saveLoadConfig) {
                PropertyLoader.properties.setProperty("YawPitch" + esmManager.getName(),
                        new YawPitch(simpleWalkSetup.getAvatarLocation().getTransform()).toString());
                PropertyLoader.properties.setProperty("Trans" + esmManager.getName(),
                        "" + PropertyCodec.vector3fIn(simpleWalkSetup.getAvatarLocation().get(new Vector3f())));
                PropertyLoader.properties.setProperty("CellId" + esmManager.getName(), "" + simpleBethCellManager.getCurrentCellFormId());
            }
        }
        // save it in case anything else has written to it
        PropertyLoader.save();

        // now to allow the app to exit
        stayAlive = false;
    }

    public void destroy() {

        //FIXME: if the esm and bsa files aren't loaded up, I might well be in the middle of teh
        //setSelectedGameConfig(GameConfig newGameConfig) { t Thread work, so this guy needs to be a little careful about what it does
        // setting things to null will cause many NPE
        // also I want this destroy to stop the work being done by the loady up thread to avoid OOMs

        if(simpleWalkSetup != null)
            simpleWalkSetup.destroy();
        // our anonymous class holds a reference to this instance
        SimpleSounds.mp3SystemMediaPlayer = null;

        //simpleBethCellManager.destroy();
    }

    private boolean hasESMAndBSAFiles(GameConfig gameConfig) {
        // check to ensure the esm file and at least one bsa file are in the folder
        DocumentFile checkEsm = DocumentFile.fromTreeUri(this.parentActivity, Uri.parse(gameConfig.scrollsFolder)).findFile(gameConfig.mainESMFile);
        if (checkEsm == null || !checkEsm.exists()) {
            return false;
        }

        int countOfBsa = 0;
        DocumentFile checkBsa = DocumentFile.fromTreeUri(this.parentActivity, Uri.parse(gameConfig.scrollsFolder));
        for (DocumentFile f : checkBsa.listFiles()) {
            countOfBsa += f.getName().toLowerCase().endsWith(".bsa") ? 1 : 0;
            countOfBsa += f.getName().toLowerCase().endsWith(".ba2") ? 1 : 0;
        }

        if (countOfBsa == 0) {
            return false;
        }

        return true;
    }

    @Override
    public void renderSettingsUpdated() {
        simpleBethCellManager.updateBranches();
    }

    private void setSelectedGameConfig(GameConfig newGameConfig) {
        selectedGameConfig = newGameConfig;
        simpleWalkSetup.getAvatarCollisionInfo().setAvatarYHeight(selectedGameConfig.avatarYHeight);

        Thread t = new Thread() {
            public void run() {
                synchronized (selectedGameConfig) {
                    //FIXME: this is how you get more debug info out from this non close called issue
                    // Inflater in ArchiveInputStream seems to be at fault, but idk
                    //   System                  com.ingenieur.ese.eseandroid         W  A resource failed to call end.
                    /*    try {
                        Class.forName("dalvik.system.CloseGuard")
                                .getMethod("setEnabled", boolean.class)
                                .invoke(null, true);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }*/

                    simpleWalkSetup.setEnabled(false);

                    // load up the esm file
                    IDashboard.dashboard.setEsmLoading(1);

                    DocumentFile rootFolder = DocumentFile.fromTreeUri(parentActivity, Uri.parse(selectedGameConfig.scrollsFolder));
                    DocumentFile esmDF = rootFolder.findFile(selectedGameConfig.mainESMFile);
                    esmManager = ESMManagerUri.getESMManager(parentActivity, esmDF.getUri());
                    IDashboard.dashboard.setEsmLoading(-1);

                    if (esmManager != null) {
                        new EsmSoundKeyToName(esmManager);
                        MeshSource meshSource;
                        BsaTextureSource textureSource;
                        SoundSource soundSource;

                        if (bsaFileSet == null) {
                            bsaFileSet = new BSArchiveSetUri(parentActivity, selectedGameConfig.scrollsFolder, false);
                            organiseDDSKTXBSA(parentActivity, rootFolder, bsaFileSet, progressBar);
                        }

                        // did the load above get anything organised?
                        if (bsaFileSet.size() == 0) {
                            System.err.println("bsa size is 0 :( " + selectedGameConfig.scrollsFolder);
                            return;
                        }

                        meshSource = new BsaMeshSource(bsaFileSet);
                        textureSource = new BsaTextureSource(bsaFileSet);

                        //TODO: Morrowind appears to have sound and music as a separate gosh darned file system system! not in a bsa
                        soundSource = new BsaSoundSource(bsaFileSet, null);//new EsmSoundKeyToName(esmManager));

                        //Just for the crazy new fallout 4 system
                        BgsmSource.setBgsmSource(meshSource);

                        mediaSources = new MediaSources(meshSource, textureSource, soundSource);

                        String mapTex = null;
                        String invTex = null;
                        String charTex = null;





                        if (gameConfigToLoad.folderKey.equals("MorrowindFolder")) {
                            BethRenderSettings.setFarLoadGridCount(8);
                            BethRenderSettings.setNearLoadGridCount(2);
                            BethRenderSettings.setLOD_LOAD_DIST_MAX(32);
                            BethRenderSettings.setObjectFade(150);
                            BethRenderSettings.setItemFade(120);
                            BethRenderSettings.setActorFade(50);
                            BethRenderSettings.setFogEnabled(false);
                            //BethWorldVisualBranch.FOG_START = 100;
                            //BethWorldVisualBranch.FOG_END = 250;

                            mapTex = "textures/scroll.ktx";
                            invTex = "levelup/agent.ktx";
                            charTex = "textures/tex_menutest.ktx";

                            parentActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    map = new MorrowindMapImage(parentActivity, ScrollsExplorer.this, textureSource);
                                    ((AndyESExplorerActivity)parentActivity).mPagerAdapter.getMapFragment().setUpMap(map);
                                }
                            });

                        } else if (gameConfigToLoad.folderKey.equals("OblivionFolder")) {
                            BethRenderSettings.setFarLoadGridCount(4);
                            BethRenderSettings.setNearLoadGridCount(2);
                            BethRenderSettings.setLOD_LOAD_DIST_MAX(24);
                            BethRenderSettings.setObjectFade(100);
                            BethRenderSettings.setItemFade(80);
                            BethRenderSettings.setActorFade(40);
                            BethRenderSettings.setFogEnabled(false);//lod make this redundant

                            mapTex = "textures/menus/map/map_icon_tab_world_map.ktx";
                            invTex = "textures/menus/inventory/inv_icon_tab_all.ktx";
                            charTex = "textures/menus/stats/stat_icon_tab_char.ktx";

                            parentActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    map = new OblivionMapImage(parentActivity,ScrollsExplorer.this, textureSource);
                                    ((AndyESExplorerActivity)parentActivity).mPagerAdapter.getMapFragment().setUpMap(map);
                                }
                            });

                        } else if (gameConfigToLoad.folderKey.startsWith("FallOut3")) {
                            BethRenderSettings.setFarLoadGridCount(3);
                            BethRenderSettings.setNearLoadGridCount(1);
                            BethRenderSettings.setLOD_LOAD_DIST_MAX(24);
                            BethRenderSettings.setObjectFade(80);
                            BethRenderSettings.setItemFade(70);
                            BethRenderSettings.setActorFade(35);
                            BethRenderSettings.setFogEnabled(false);//lod make this redundant


                            mapTex = "textures/interface/icons/message icons/glow_message_map.ktx";
                            invTex = "textures/interface/icons/message icons/glow_message_giftbox.ktx";
                            charTex = "textures/interface/icons/message icons/glow_message_vaultboy_neutral.ktx";

                            parentActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    map = new Fallout3MapImage(parentActivity, ScrollsExplorer.this, textureSource);
                                    ((AndyESExplorerActivity)parentActivity).mPagerAdapter.getMapFragment().setUpMap(map);
                                }
                            });

                        } else if (gameConfigToLoad.folderKey.startsWith("FalloutNV")) {
                            BethRenderSettings.setFarLoadGridCount(3);
                            BethRenderSettings.setNearLoadGridCount(1);
                            BethRenderSettings.setLOD_LOAD_DIST_MAX(24);
                            BethRenderSettings.setObjectFade(80);
                            BethRenderSettings.setItemFade(70);
                            BethRenderSettings.setActorFade(35);
                            BethRenderSettings.setFogEnabled(false);//lod make this redundant

                            mapTex = "textures/interface/icons/message icons/glow_message_map.ktx";
                            invTex = "textures/interface/icons/message icons/glow_message_giftbox.ktx";
                            charTex = "textures/interface/icons/message icons/glow_message_vaultboy_brotherhood.ktx";

                            parentActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    map = new FalloutNVMapImage(parentActivity, ScrollsExplorer.this, textureSource);
                                    ((AndyESExplorerActivity)parentActivity).mPagerAdapter.getMapFragment().setUpMap(map);
                                }
                            });

                        } else if (gameConfigToLoad.folderKey.startsWith("Skyrim")) {
                            BethRenderSettings.setFarLoadGridCount(2);
                            BethRenderSettings.setNearLoadGridCount(1);
                            BethRenderSettings.setLOD_LOAD_DIST_MAX(12);
                            BethRenderSettings.setObjectFade(50);
                            BethRenderSettings.setItemFade(50);
                            BethRenderSettings.setActorFade(35);
                            BethRenderSettings.setFogEnabled(false);//lod make this redundant

                            mapTex = "interface/exported/m.png.ktx";
                            invTex = "interface/exported/i.png.ktx";
                            charTex = "interface/exported/c.png.ktx";

                            parentActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    map = new SkyrimMapImage(parentActivity, ScrollsExplorer.this, textureSource);
                                    ((AndyESExplorerActivity)parentActivity).mPagerAdapter.getMapFragment().setUpMap(map);
                                }
                            });

                        } else if (gameConfigToLoad.folderKey.startsWith("FallOut4")) {
                            BethRenderSettings.setFarLoadGridCount(2);
                            BethRenderSettings.setNearLoadGridCount(1);
                            BethRenderSettings.setLOD_LOAD_DIST_MAX(12);
                            BethRenderSettings.setObjectFade(50);
                            BethRenderSettings.setItemFade(50);
                            BethRenderSettings.setActorFade(35);
                            BethRenderSettings.setFogEnabled(false);//lod make this redundant

                            mapTex = "textures/interface/pip-boy/worldmap_d.ktx";
                            invTex = "textures/interface/note/parchment_d.ktx";
                            charTex = "textures/interface/pip-boy/pipscreen01_d.ktx";

                            parentActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    map = new Fallout4MapImage(parentActivity, ScrollsExplorer.this, textureSource);
                                    ((AndyESExplorerActivity)parentActivity).mPagerAdapter.getMapFragment().setUpMap(map);
                                }
                            });
                        }  else if (gameConfigToLoad.folderKey.startsWith("Starfield")) {
                            BethRenderSettings.setFarLoadGridCount(2);
                            BethRenderSettings.setNearLoadGridCount(1);
                            BethRenderSettings.setLOD_LOAD_DIST_MAX(12);
                            BethRenderSettings.setObjectFade(50);
                            BethRenderSettings.setItemFade(50);
                            BethRenderSettings.setActorFade(35);
                            BethRenderSettings.setFogEnabled(false);//lod make this redundant

                            mapTex = "textures/interface/pip-boy/worldmap_d.ktx";
                            invTex = "textures/interface/note/parchment_d.ktx";
                            charTex = "textures/interface/pip-boy/pipscreen01_d.ktx";

                            parentActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    map = new StarfieldMapImage(parentActivity, ScrollsExplorer.this, textureSource);
                                    ((AndyESExplorerActivity)parentActivity).mPagerAdapter.getMapFragment().setUpMap(map);
                                }
                            });
                        }



                        Bitmap mapBitmap = BsaUtils.getBitmapFromTextureSource(mapTex, textureSource);
                        Bitmap invBitmap = BsaUtils.getBitmapFromTextureSource(invTex, textureSource);
                        Bitmap charBitmap = BsaUtils.getBitmapFromTextureSource(charTex, textureSource);

                        parentActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mapBitmap != null)
                                    parentFragment.getMapOverlay().setBitMap(mapBitmap);
                                if (invBitmap != null)
                                    parentFragment.getInventoryOverlay().setBitMap(invBitmap);
                                if (charBitmap != null)
                                    parentFragment.getCharacterSheetOverlay().setBitMap(charBitmap);
                            }
                        });


                        simpleWalkSetup.configure(meshSource, simpleBethCellManager);

                        simpleWalkSetup.getWindow().addKeyListener(new KeyHandler());
                        simpleWalkSetup.getWindow().addMouseListener(dragMouseAdapter);
                        simpleWalkSetup.getWindow().addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowResized(final WindowEvent e) {
                                J3dNiParticles.setScreenWidth(simpleWalkSetup.getWindow().getWidth());
                            }

                        });
                        J3dNiParticles.setScreenWidth(simpleWalkSetup.getWindow().getWidth());
                        simpleWalkSetup.setMouseLock(true);// auto press teh tab key

                        // I could use the j3dcellfactory now? with the cached cell records?
                        simpleBethCellManager.setSources(selectedGameConfig, esmManager, mediaSources);

                        if (selectedGameConfig == allGameConfigs.get(0)) {
                            System.out.println("Adding Tes3 extensions");
                            tes3Extensions = new Tes3Extensions(selectedGameConfig, esmManager, mediaSources, simpleWalkSetup,
                                    simpleBethCellManager);
                        }

                        // only does something if the game is morrowind
                        organizeMorrowindSounds();

                        // startCellId == -1 means show the cell picker
                        if (gameConfigToLoad.startCellId != -1) {
                            YawPitch yp = selectedGameConfig.startYP;
                            Vector3f trans = selectedGameConfig.startLocation;
                            int prevCellformid = selectedGameConfig.startCellId;
                            //Freeform so load the recorded values
                            if (saveLoadConfig) {
                                yp = YawPitch.parse(PropertyLoader.properties.getProperty("YawPitch" + esmManager.getName(), selectedGameConfig.startYP.toString()));
                                trans = PropertyCodec.vector3fOut(PropertyLoader.properties.getProperty("Trans" + esmManager.getName(),
                                        selectedGameConfig.startLocation.toString()));

                                prevCellformid = Integer.parseInt(PropertyLoader.properties.getProperty("CellId" + esmManager.getName(), "-1"));
                            }

                            AndyESExplorerActivity.logFireBaseContent("selectedGameConfig", "startCellId " + prevCellformid + " trans " + trans);

                            simpleWalkSetup.getAvatarLocation().set(yp.get(new Quat4f()), trans);
                            display(prevCellformid);
                        } else {
                            showCellPicker();
                        }

                    } else {
                        System.out.println("esm manger is null, I just don't know why..." + esmDF.getUri());
                    }
                }
            }
        };
        t.start();
    }

    public void showCellPicker() {
        // display the cell picker and create a start location from the selected cell
        parentActivity.runOnUiThread(new Runnable() {
            public void run() {
                esmCellChooser = new ESMCellChooser(parentActivity, esmManager).setFileListener(new ESMCellChooser.EsmFileSelectedListener() {
                    @Override
                    public boolean onTreeNodeLongClick(TreeNode treeNode, View view) {
                        return true;
                    }

                    @Override
                    public void onTreeNodeClick(TreeNode treeNode, View view) {
                        if (treeNode.getValue() instanceof Record) {


                            // defaults
                            YawPitch yp = selectedGameConfig.startYP;
                            Vector3f trans = selectedGameConfig.startLocation;

                            int cellFormId = ((Record) treeNode.getValue()).getFormID();

                            //-1 is load previous
                            if( cellFormId == -1 ) {

                                yp = YawPitch.parse(PropertyLoader.properties.getProperty("YawPitch" + esmManager.getName(), selectedGameConfig.startYP.toString()));
                                trans = PropertyCodec.vector3fOut(PropertyLoader.properties.getProperty("Trans" + esmManager.getName(),
                                        selectedGameConfig.startLocation.toString()));

                                cellFormId = Integer.parseInt(PropertyLoader.properties.getProperty("CellId" + esmManager.getName(), "-1"));

                                // no cell means ignore the click
                                if(cellFormId == -1)
                                    return;

                            } else {
                                findADoor(cellFormId, yp, trans);
                            }
                            esmCellChooser.dismiss();
                            simpleWalkSetup.getAvatarLocation().set(yp.get(new Quat4f()), trans);
                            display(cellFormId);
                        }
                    }
                }).load();

                esmCellChooser.showDialog();
            }
        });
    }


    private void organizeMorrowindSounds() {
        //TODO: music to play is using Files and is Morrowinw Specific
        if (gameConfigToLoad.folderKey.equals("MorrowindFolder")) {
            // this is how you play a mp3  file. the setDataSource
            // version doesn't seem to work, possibly the activity is the key
            if (gameConfigToLoad.musicToPlayId > 0) {
                File musicFileToPlay = new File("");
                if (gameConfigToLoad.musicToPlayId == 1) {
                    //1-7
                    int piece = (int) (Math.random() * 7) + 1;
                    musicFileToPlay = new File(gameConfigToLoad.scrollsFolder + "/Music/Explore/mx_explore_" + piece + ".mp3");
                } else if (gameConfigToLoad.musicToPlayId == 2) {
                    // notice extra spaces in some battle mp3 names
                    musicFileToPlay = new File(gameConfigToLoad.scrollsFolder + "/Music/Battle/MW battle1.mp3");
                }

                if (musicFileToPlay.exists() && musicFileToPlay.isFile()) {
                    musicMediaPlayer = MediaPlayer.create(parentActivity, Uri.fromFile(musicFileToPlay));
                    if (musicMediaPlayer != null) {
                        musicMediaPlayer.setVolume(0.15f, 0.15f);
                        musicMediaPlayer.start();
                    }
                }

            }

            //TODO: simple sounds is File based as well
            //https://stackoverflow.com/questions/1972027/android-playing-mp3-from-byte
            if (SimpleSounds.mp3SystemMediaPlayer == null) {
                SimpleSounds.mp3SystemMediaPlayer = new
                        SimpleSounds.Mp3SystemMediaPlayer() {
                            MediaPlayer musicMediaPlayer2;

                            @Override
                            public void playAnMp3(String s, float v) {
                                s = s.replace("\\", "/");
                                if (!s.startsWith("/"))
                                    s = "/" + s;

                                File mp3File = new File(gameConfigToLoad.scrollsFolder + s);
                                if (mp3File.exists()) {
                                    musicMediaPlayer2 = MediaPlayer.create(parentActivity, Uri.fromFile(mp3File));
                                    if (musicMediaPlayer != null) {
                                        musicMediaPlayer2.setVolume(v, v);
                                        musicMediaPlayer2.setLooping(false);
                                        musicMediaPlayer2.start();
                                    }
                                }
                            }
                        };
            }
        }
    }

    private void findADoor(int formToLoad, YawPitch yp, Vector3f trans) {
        J3dICellFactory j3dCellFactory = selectedGameConfig.j3dCellFactory;
        if (j3dCellFactory != null) {

            ArrayList<CommonREFR> doors = new ArrayList<>();
            if (selectedGameConfig.gameName != "TESIII: Morrowind") {
                // if SimpleBethCellManager.setSources has been called the persistent children will have been loaded
                PluginGroup cellChildGroups = j3dCellFactory.getPersistentChildrenOfCell(formToLoad);
                if(cellChildGroups != null) {
                    for (Record record : cellChildGroups.getRecordList()) {
                        // is this a door way?
                        if (record.getRecordType().equals("REFR")) {
                            // don't go game specific just the common data needed (which include XTEL!)
                            CommonREFR commonREFR = new CommonREFR(record, true);
                            XTEL xtel = commonREFR.XTEL;
                            //if we are a door outward we have a door inward
                            if (xtel != null) {
                                Record otherDoor;
                                if (xtel.doorFormId != 0) {
                                    otherDoor = j3dCellFactory.getRecord(xtel.doorFormId);
                                    if (otherDoor != null) {
                                        CommonREFR otherDoorCommonREFR = new CommonREFR(otherDoor, true);
                                        doors.add(otherDoorCommonREFR);
                                    }
                                }
                            }

                        }
                    }
                }
            } else {
                if (formToLoad == 0) {
                    int attempts = 0;
                    while (doors.size() == 0 && attempts < 20) {
                        attempts++;
                        // morrowind itself we'll have to pick a random cell from -25 to +25 until we find a door
                        ///	looks like x = 23 to -18 y is 27 to -17
                        int x = (int) ((Math.random() * 41) - 18);
                        int y = (int) ((Math.random() * 44) - 17);
                        try {
                            PluginGroup cellChildGroups = esmManager.getWRLDExtBlockCELLChildren(0, x, y);
                            if (cellChildGroups != null) {
                                for (Record record : ESMUtils.getChildren(cellChildGroups, PluginGroup.CELL_TEMPORARY)) {
                                    // is this a door way?
                                    if (record.getRecordType().equals("REFR")) {
                                        //TODO: Fro Morrowind I need to find all doors elsewhere and see where they wouldput you in this cell
                                        // which is obviously too much work!

                                        // morrowind has a half pie system using DNAM
                                        // morrowind has no match inwards door so we'll have to make up t and yp
                                        esmj3dtes3.data.records.REFR commonREFR = new esmj3dtes3.data.records.REFR(record);
                                        XTEL xtel = commonREFR.XTEL;// xtel describe the target position and DNAM states the cell, but a NULL DNAM is Morrowind (id=0)
                                        if (xtel != null)
                                            doors.add(commonREFR);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    }

                } else {
                    // no persistent system, just load them all!
                    PluginGroup cellChildGroups = j3dCellFactory.getPersistentChildrenOfCell(formToLoad);
                    if (cellChildGroups != null) {
                        for (Record record : ESMUtils.getChildren(cellChildGroups, PluginGroup.CELL_TEMPORARY)) {
                            // is this a door way?
                            if (record.getRecordType().equals("REFR")) {
                                //TODO: For Morrowind I need to find all doors elsewhere and see where they would put you in this cell
                                // which is obviously too much work!

                                // morrowind has a half pie system using DNAM
                                // morrowind has no match inwards door so we'll have to make up t and yp
                                esmj3dtes3.data.records.REFR commonREFR = new esmj3dtes3.data.records.REFR(record);
                                XTEL xtel = commonREFR.XTEL;// xtel describe the target position and DNAM states the cell, but a NULL DNAM is Morrowind (id=0)
                                if (xtel != null)
                                    doors.add(commonREFR);
                            }
                        }
                    }
                }

            }

            if (doors.size() > 0) {
                int idx = (int) (Math.random() * (doors.size() - 1));
                if (selectedGameConfig.gameName != "TESIII: Morrowind") {
                    XTEL xtel = doors.get(idx).XTEL; // note this is the other door so the exit is right but it's cell is not our cell
                    Vector3f t = ActionableMouseOverHandler.getTrans(xtel.x, xtel.y, xtel.z);
                    t.y += 1; // TODO: cos it's the floor I reckon, nay something off in all direction a bit here
                    Quat4f r = ActionableMouseOverHandler.getRot(xtel.rx, xtel.ry, xtel.rz);

                    trans.set(t);
                    yp.set(r);
                } else {
                    esmj3dtes3.data.records.REFR refr = (esmj3dtes3.data.records.REFR) doors.get(idx);
                    Vector3f loc = refr.getTrans();
                    Vector3f rot = refr.getEulerRot();
                    //TODO:  location needs to be pushed forward in facing as this is just the door itself
                    Vector3f t = ActionableMouseOverHandler.getTrans(loc.x, loc.y, loc.z);
                    //t.y += 1; // TODO: cos it's the floor I reckon, nay something off in all direction a bit here
                    Quat4f r = ActionableMouseOverHandler.getRot(rot.x, rot.y, rot.z);

                    // now push forward by 1 meter to see if we are in front of the door
                    Transform3D t3d = new Transform3D(r, new Vector3f(0, 0, 0), 1f);
                    Vector3f f = new Vector3f(0, 0, 1);
                    t3d.transform(f);
                    t.add(f);

                    trans.set(t);
                    yp.set(r);
                }
            }
        }

    }


    @Override
    public void locationUpdated(Quat4f rot, Vector3f trans) {

    }

    private void display(final int cellformid) {
        Vector3f t = simpleWalkSetup.getAvatarLocation().get(new Vector3f());
        Quat4f r = simpleWalkSetup.getAvatarLocation().get(new Quat4f());
        simpleBethCellManager.setCurrentCellFormId(cellformid, t, r);
    }

    public SimpleBethCellManager getSimpleBethCellManager() {
        return simpleBethCellManager;
    }


    @Override
    public void dragComplete(final MouseEvent e, DragMouseAdapter.DRAG_TYPE dragType) {
        if (dragType == DragMouseAdapter.DRAG_TYPE.UP) {
            // show Keyboard
            boolean kbVis = ((com.jogamp.newt.Window) e.getSource()).isKeyboardVisible();
            ((com.jogamp.newt.Window) e.getSource()).setKeyboardVisible(!kbVis);
        } else if (dragType == DragMouseAdapter.DRAG_TYPE.DOWN) {
            showCellPicker();
        } else if (dragType == DragMouseAdapter.DRAG_TYPE.LEFT) {

        } else if (dragType == DragMouseAdapter.DRAG_TYPE.RIGHT) {
        }
    }

    public void stopRenderer() {
        simpleWalkSetup.stopRenderer();
    }

    public void startRenderer(GLWindow gl_window) {
        simpleWalkSetup.startRenderer(gl_window);
    }


    private static void organiseDDSKTXBSA(Activity parentActivity, DocumentFile rootFolder, BSArchiveSet bsaFileSet, ProgressBar progressBar) {
        //OK time to check that each bsa file that holds dds has a ktx equivalent and drop the dds version
        // or if not to convert the dds to ktx then drop the dds version

        //a list of dds archives that might have a ktx equivalent
        ArrayList<ArchiveFile> ddsBsas = new ArrayList<ArchiveFile>();
        for (ArchiveFile archiveFile : bsaFileSet) {
            if (archiveFile != null && archiveFile.hasDDS()) {
                // we want a archive with the same name but _ktx before the extension holding KTX files
                ddsBsas.add(archiveFile);
            }
        }

        // search for ktx existing and drop the dds if so
        HashMap<String, ArchiveFile> neededBsas = new HashMap<String, ArchiveFile>();
        for (ArchiveFile ddsArchive : ddsBsas) {
            // we want a archive with the same name but _ktx before the extension holding KTX files
            String ddsArchiveName = ddsArchive.getName();
            String ext = ddsArchiveName.substring(ddsArchiveName.lastIndexOf("."));
            String ktxArchiveName = ddsArchiveName.substring(0, ddsArchiveName.lastIndexOf("."));
            ktxArchiveName = ktxArchiveName + "_ktx" + ext;
            boolean found = false;
            for (ArchiveFile ktxArchive : bsaFileSet) {
                //TODO: should see  if it's got ktx in it, but for now let's just prey
                if (ktxArchive != null && ktxArchive.getName().equals(ktxArchiveName)) {
                    found = true;
                    //remove the dds version archive
                    try {
                        ddsArchive.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bsaFileSet.remove(ddsArchive);
                    break;
                }
            }

            if (!found) {
                neededBsas.put(ktxArchiveName, ddsArchive);
            }
        }

        // are there any that might be converted or possibly just run "on the fly"
        if (neededBsas.size() > 0) {

            CountDownLatch waitForAnswer = new CountDownLatch(1);
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        Thread t = new Thread() {
                            public void run() {
                                for (String ktxArchiveName : neededBsas.keySet()) {
                                    ArchiveFile ddsArchive = neededBsas.get(ktxArchiveName);
                                    String ddsArchiveName = ddsArchive.getName();
                                    //remove the dds version archive either way
                                    try {
                                        ddsArchive.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    bsaFileSet.remove(ddsArchive);
                                    ddsArchive = null;

                                    boolean found = false;
                                    for (ArchiveFile archiveFile : bsaFileSet) {
                                        //TODO: should see  if it's got ktx in it, but for now let's just prey
                                        if (archiveFile != null && archiveFile.getName().equals(ktxArchiveName)) {
                                            found = true;
                                            break;
                                        }
                                    }

                                    if (!found) {
                                        System.out.println("Not found: " + ktxArchiveName + " creating now");

                                        // I need the displayable version to convert so let's load a new copy of ddsArchive
                                        FileInputStream fis;
                                        try {
                                            long tstart2 = System.currentTimeMillis();
                                            DocumentFile ddsDF = rootFolder.findFile(ddsArchiveName);

                                            Uri ddsUri = ddsDF.getUri();
                                            System.out.println("Reloading as in displayable format " + ddsDF.getUri());

                                            ParcelFileDescriptor ddsPFD = parentActivity.getContentResolver().openFileDescriptor(ddsUri, "r");
                                            fis = new ParcelFileDescriptor.AutoCloseInputStream(ddsPFD);
                                            ArchiveFile archiveFile = ArchiveFile.createArchiveFile(fis.getChannel(), ddsArchiveName);
                                            archiveFile.load(true);//blocking call
                                            System.out.println("loaded as displayable " + ddsUri + " in " + (System.currentTimeMillis() - tstart2));
                                            //converting
                                            final long tstart = System.currentTimeMillis();
                                            // find it
                                            DocumentFile ktxDF = rootFolder.findFile(ktxArchiveName);
                                            // or create it (if not found)
                                            if (ktxDF == null) {
                                                ktxDF = rootFolder.createFile("application/octet-stream", ktxArchiveName);
                                            }

                                            ParcelFileDescriptor ktxPFD = parentActivity.getContentResolver().openFileDescriptor(ktxDF.getUri(), "rw");
                                            FileOutputStream fos = new ParcelFileDescriptor.AutoCloseOutputStream(ktxPFD);
                                            FileInputStream fisKtx = new ParcelFileDescriptor.AutoCloseInputStream(ktxPFD);

                                            //DO NOT delete file as it is hopefully a restartable //->fos.getChannel().truncate(0);//in case the file already exists somehow, this is a delete type action
                                            DDSToKTXBsaConverter.StatusUpdateListener sul = new DDSToKTXBsaConverter.StatusUpdateListener() {
                                                public void updateProgress(int currentProgress) {
                                                    parentActivity.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            progressBar.setProgress(currentProgress);
                                                            System.out.println("CurrentProgress " + currentProgress + "%  in " + (System.currentTimeMillis() - tstart) + "ms for " + ktxArchiveName);
                                                        }
                                                    });
                                                }
                                            };


                                            DDSToKTXBsaConverter convert = new DDSToKTXBsaConverter(fos.getChannel(), fisKtx.getChannel(), archiveFile, sul);
                                            System.out.println("Converting " + ddsArchiveName + " to ktx version, this may take ages!");
                                            parentActivity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    //screen still sleeps just after a long time, CPU processing appears to continue anyway
                                                    parentActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                                    progressBar.setIndeterminate(false);
                                                    progressBar.setVisibility(ProgressBar.VISIBLE);
                                                    progressBar.setProgress(0);
                                                    progressBar.setMax(100);
                                                }
                                            });

                                            convert.start();
                                            try {
                                                convert.join();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }

                                            parentActivity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    parentActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                                    progressBar.setVisibility(ProgressBar.GONE);
                                                }
                                            });
                                            System.out.println("" + (System.currentTimeMillis() - tstart) + "ms to compress " + ktxArchiveName);
                                            // have to re locate it for some reason to load it
                                            ktxDF = rootFolder.findFile(ktxArchiveName);
                                            ktxPFD = parentActivity.getContentResolver().openFileDescriptor(ktxDF.getUri(), "r");
                                            // now load that newly created file into the system
                                            fis = new ParcelFileDescriptor.AutoCloseInputStream(ktxPFD);
                                            bsaFileSet.loadFileAndWait(fis.getChannel(), ktxArchiveName);

                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (DBException e1) {
                                            e1.printStackTrace();
                                        } catch (IOException e1) {
                                            e1.printStackTrace();
                                        }

                                    }
                                }
                                waitForAnswer.countDown();
                            }
                        };
                        t.start();
                    } else {
                        waitForAnswer.countDown();
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    builder.setMessage("Convert " + neededBsas.size() + " bsa files from dds to ktx, this may take ages...").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }
            });

            try {
                waitForAnswer.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }




    private class KeyHandler extends KeyAdapter {
        public KeyHandler() {
			/*System.out.println("H toggle havok display");
			System.out.println("L toggle visual display");
			System.out.println("J toggle spin");
			System.out.println("K toggle animate model");
			System.out.println("P toggle background color");
			System.out.println("Space toggle cycle through files");*/
        }


        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_1) {
                BethRenderSettings.setOutlineFocused(!BethRenderSettings.isOutlineFocused());
            } else if (e.getKeyCode() == KeyEvent.VK_2) {
                BethRenderSettings.setOutlineChars(!BethRenderSettings.isOutlineChars());
            } else if (e.getKeyCode() == KeyEvent.VK_3) {
                BethRenderSettings.setOutlineConts(!BethRenderSettings.isOutlineConts());
            } else if (e.getKeyCode() == KeyEvent.VK_4) {
                BethRenderSettings.setOutlineDoors(!BethRenderSettings.isOutlineDoors());
            } else if (e.getKeyCode() == KeyEvent.VK_5) {
                BethRenderSettings.setOutlineParts(!BethRenderSettings.isOutlineParts());
            } else if (e.getKeyCode() == KeyEvent.VK_O) {
                parentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        OptionsDialog od = new OptionsDialog(parentActivity, simpleWalkSetup);
                        od.display();
                    }
                });
            }


        }
    }
}

