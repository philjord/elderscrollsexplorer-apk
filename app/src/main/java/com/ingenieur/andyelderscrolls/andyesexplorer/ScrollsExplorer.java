package com.ingenieur.andyelderscrolls.andyesexplorer;

import static scrollsexplorer.GameConfig.allGameConfigs;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.FragmentActivity;

import com.amrdeveloper.treeview.TreeNode;
import com.ingenieur.andyelderscrolls.ElderScrollsActivity;
import com.ingenieur.andyelderscrolls.MorrowindActivity;
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
import java.util.ArrayList;

import bsa.source.BsaMeshSource;
import bsa.source.BsaSoundSource;
import bsa.source.BsaTextureSource;
import bsaio.ArchiveFile;
import bsaio.BSArchiveSet;
import bsaio.BSArchiveSetUri;
import esmio.common.data.plugin.PluginGroup;
import esmio.common.data.record.Record;
import esmio.loader.ESMManager;
import esmio.loader.ESMManagerUri;
import esmio.loader.IESMManager;
import esmio.utils.ESMUtils;
import esmio.utils.source.EsmSoundKeyToName;
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
import utils.source.TextureSource;

/**
 * Created by phil on 3/10/2016.
 */

public class ScrollsExplorer implements BethRenderSettings.UpdateListener, LocationUpdateListener, DragMouseAdapter.Listener {
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

    private GameConfig gameConfigToLoad;

    private Tes3Extensions tes3Extensions;

    private boolean stayAlive = true;

    private MediaPlayer musicMediaPlayer;

    private boolean saveLoadConfig = false;


    public ScrollsExplorer(FragmentActivity parentActivity2, GLWindow gl_window, String gameName, int gameConfigId) {

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

        BsaTextureSource.allowedTextureFormats = BsaTextureSource.AllowedTextureFormats.DDS;

        //these 3 test the "no dds support" issue and solution on phones
        CompressedTextureLoader.RETURN_DECOMPRESSED_DDS = true;
        javaawt.image.BufferedImage.installBufferedImageDelegate(VMBufferedImage.class);
        javaawt.imageio.ImageIO.installBufferedImageImpl(VMImageIO.class);
        javaawt.EventQueue.installEventQueueImpl(VMEventQueue.class);

        BethRenderSettings.setFarLoadGridCount(4);
        BethRenderSettings.setNearLoadGridCount(2);
        BethRenderSettings.setLOD_LOAD_DIST_MAX(32);
        BethRenderSettings.setObjectFade(100);
        BethRenderSettings.setItemFade(60);
        BethRenderSettings.setActorFade(35);
        BethRenderSettings.setOutlineFocused(true);
        BethRenderSettings.setEnablePlacedLights(true);
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

            // for syda neen performance
            //J3dCELL.DO_DUMP = true;
            BethRenderSettings.setFarLoadGridCount(4);
            BethWorldVisualBranch.FOG_START = 75;
            BethWorldVisualBranch.FOG_END = 150;

            //long distance view
            //BethRenderSettings.setFarLoadGridCount(16);
            //BethRenderSettings.setFogEnabled(false);
        } else {
            AndyESExplorerActivity.logFireBaseLevelUp("LoadNonMorrowind", gameConfigToLoad.gameName);

            //TODO: must make a per game setting recorder for this gear!

            //oblivion goes hard, others are cautious for now
            if (!gameConfigToLoad.folderKey.equals("OblivionFolder")) {
                BethRenderSettings.setFarLoadGridCount(0);
                BethRenderSettings.setNearLoadGridCount(2);
                BethRenderSettings.setLOD_LOAD_DIST_MAX(0);
                BethWorldVisualBranch.FOG_START = 75;
                BethWorldVisualBranch.FOG_END = 150;
            }
        }

        // default to none
        Tes3Extensions.HANDS = Tes3Extensions.hands.NONE;

        simpleWalkSetup = new AndySimpleWalkSetup("SimpleBethCellManager", gl_window);
        simpleWalkSetup.setAzerty(false);

        simpleBethCellManager = new SimpleBethCellManager(simpleWalkSetup);

        BethRenderSettings.addUpdateListener(this);

        simpleWalkSetup.getAvatarLocation().addAvatarLocationListener(this);

        dragMouseAdapter.setListener(this);

        // start teh actual game up!
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
        // our anonymous class holds a reference to this instance
        SimpleSounds.mp3SystemMediaPlayer = null;
        //simpleWalkSetup.destroy();
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

                    // load up the esm file
                    IDashboard.dashboard.setEsmLoading(1);

                    DocumentFile rootFolder = DocumentFile.fromTreeUri(ScrollsExplorer.this.parentActivity, Uri.parse(selectedGameConfig.scrollsFolder));
                    DocumentFile esmDF = rootFolder.findFile(selectedGameConfig.mainESMFile);
                    esmManager = ESMManagerUri.getESMManager(ScrollsExplorer.this.parentActivity, esmDF.getUri());
                    IDashboard.dashboard.setEsmLoading(-1);

                    if (esmManager != null) {
                        new EsmSoundKeyToName(esmManager);
                        MeshSource meshSource;
                        TextureSource textureSource;
                        SoundSource soundSource;

                        //TODO TODO:  KTX file are not in my bsa at all, I need ot convert on the fly and cache out
                        // TODO: dump the obb stuff and simply convert compressed texture on the fly and record them somewhere(in the game folder?)
                        bsaFileSet = new BSArchiveSetUri(ScrollsExplorer.this.parentActivity, selectedGameConfig.scrollsFolder, false);


                        if (bsaFileSet == null || bsaFileSet.size() == 0) {
                            System.err.println("bsa is null or size is 0 :(" + selectedGameConfig.scrollsFolder);
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

                        // -1 means show the cell picker
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
                            // display the cell picker and create a start location form the selected cell
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
                                                esmCellChooser.dismiss();

                                                int formToLoad = ((Record) treeNode.getValue()).getFormID();
                                                YawPitch yp = selectedGameConfig.startYP;
                                                Vector3f trans = selectedGameConfig.startLocation;

                                                // need to set Trans to a door somewhere?
                                                findADoor(formToLoad, yp, trans);

                                                AndyESExplorerActivity.logFireBaseContent("selectedGameConfig", "startCellId " + formToLoad + " trans " + trans);

                                                simpleWalkSetup.getAvatarLocation().set(yp.get(new Quat4f()), trans);
                                                display(((Record) treeNode.getValue()).getFormID());
                                            }
                                        }
                                    }).load();

                                    esmCellChooser.showDialog();
                                }
                            });

                        }

                    } else {
                        System.out.println("esm manger is null, I just don't know why..." + esmDF.getUri());
                    }
                }
            }
        };
        t.start();
    }

    private void findADoor(int formToLoad, YawPitch yp, Vector3f trans) {
        J3dICellFactory j3dCellFactory = selectedGameConfig.j3dCellFactory;
        if (j3dCellFactory != null) {

            ArrayList<CommonREFR> doors = new ArrayList<>();
            if (selectedGameConfig.gameName != "TESIII: Morrowind") {
                // if SimpleBethCellManager.setSources has been called the persistent children will have been loaded
                PluginGroup cellChildGroups = j3dCellFactory.getPersistentChildrenOfCell(formToLoad);
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
                    Transform3D t3d = new Transform3D(r,new Vector3f(0,0,0), 1f);
                    Vector3f f = new Vector3f(0,0,1);
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

