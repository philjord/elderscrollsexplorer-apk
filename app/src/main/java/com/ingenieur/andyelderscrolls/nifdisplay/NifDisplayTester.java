package com.ingenieur.andyelderscrolls.nifdisplay;


import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.amrdeveloper.treeview.TreeNode;
import com.ingenieur.andyelderscrolls.utils.AndyFPSCounter;
import com.ingenieur.andyelderscrolls.utils.BSAArchiveFileChooser;
import com.ingenieur.andyelderscrolls.utils.DragMouseAdapter;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.AmbientLight;
import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.Light;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.PointLight;
import org.jogamp.java3d.RotationInterpolator;
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

import java.util.ArrayList;
import java.util.LinkedList;

import bsa.source.BsaMeshSource;
import bsa.source.BsaTextureSource;
import bsaio.ArchiveEntry;
import bsaio.ArchiveFile;
import bsaio.BSArchiveSet;
import bsaio.BSArchiveSetUri;
import nif.BgsmSource;
import nif.NifJ3dVisPhysRoot;
import nif.NifToJ3d;
import nif.appearance.NiGeometryAppearanceFactoryShader;
import nif.character.NifJ3dSkeletonRoot;
import nif.j3d.J3dNiAVObject;
import nif.j3d.J3dNiSkinInstance;
import nif.j3d.particles.tes3.J3dNiParticles;
import nif.shaders.NiGeometryAppearanceShader;
import tools3d.camera.simple.SimpleCameraHandler;
import tools3d.mixed3d2d.Canvas3D2D;
import tools3d.utils.ShaderSourceIO;
import tools3d.utils.Utils3D;
import tools3d.utils.scenegraph.SpinTransform;
import utils.PerFrameUpdateBehavior;
import utils.source.MeshSource;
import utils.source.TextureSource;

public class NifDisplayTester implements DragMouseAdapter.Listener {
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

    private BranchGroup scene;

    private AndyFPSCounter fpsCounter;

    private FragmentActivity parentActivity;

    private TreeNode currentTreeNodeDisplayed;

    private DragMouseAdapter dragMouseAdapter = new DragMouseAdapter();

    private BSArchiveSet bsaFileSet;
    private MeshSource meshSource = null;
    private TextureSource textureSource = null;

    public NifDisplayTester(FragmentActivity parentActivity, GLWindow gl_window, String rootDir) {
        this.parentActivity = parentActivity;
        NifToJ3d.SUPPRESS_EXCEPTIONS = false;
        NiGeometryAppearanceShader.OUTPUT_BINDINGS = true;
        ArchiveFile.USE_FILE_MAPS = false;
        ArchiveFile.USE_MINI_CHANNEL_MAPS = true;
        ArchiveFile.USE_NON_NATIVE_ZIP = false;

        NiGeometryAppearanceFactoryShader.setAsDefault();
        ShaderSourceIO.ES_SHADERS = true;

        BsaMeshSource.FALLBACK_TO_FILE_SOURCE = false;

        String[] BSARoots = new String[]{rootDir};

        bsaFileSet = new BSArchiveSetUri(this.parentActivity, BSARoots, true, true);
        meshSource = new BsaMeshSource(bsaFileSet);
        textureSource = new BsaTextureSource(bsaFileSet);

        canvas3D2D = new Canvas3D2D(gl_window);

        canvas3D2D.getGLWindow().addWindowListener(new WindowAdapter() {
            @Override
            public void windowResized(final WindowEvent e) {
                J3dNiParticles.setScreenWidth(canvas3D2D.getGLWindow().getWidth());
            }
        });
        J3dNiParticles.setScreenWidth(canvas3D2D.getGLWindow().getWidth());

        simpleUniverse = new SimpleUniverse(canvas3D2D);
        CompressedTextureLoader.setAnisotropicFilterDegree(8);

        fpsCounter = new AndyFPSCounter();

        spinTransformGroup.addChild(rotateTransformGroup);
        rotateTransformGroup.addChild(modelGroup);
        simpleCameraHandler = new SimpleCameraHandler(simpleUniverse.getViewingPlatform(), simpleUniverse.getCanvas(), modelGroup,
                rotateTransformGroup, false, true);

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

        Color3f dlColor = new Color3f(0.1f, 0.1f, 0.6f);
        DirectionalLight dirLight = new DirectionalLight(true, dlColor, new Vector3f(0f, -1f, 0f));
        dirLight.setCapability(Light.ALLOW_INFLUENCING_BOUNDS_WRITE);
        dirLight.setInfluencingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));

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
        tg.addChild(new Cube(0.01f));
        bg.addChild(tg);

        tg = new TransformGroup();
        t = new Transform3D(new Quat4f(0, 0, 0, 1), new Vector3f(0, 0, 0.015f), 1);
        tg.setTransform(t);
        BranchGroup bgc = new BranchGroup();
        bgc.addChild(tg);
        bgc.setCapability(BranchGroup.ALLOW_DETACH);
        tg.addChild(new Cube(0.002f));
        spinTransformGroup.addChild(bgc);
        toggleSpin();

        simpleUniverse.addBranchGraph(bg);

        canvas3D2D.getView().setBackClipDistance(5000);
        canvas3D2D.getView().setFrontClipDistance(0.1f);
        canvas3D2D.getGLWindow().addKeyListener(new KeyHandler());

        dragMouseAdapter.setListener(this);
        canvas3D2D.getGLWindow().addMouseListener(dragMouseAdapter);

        // Create the content branch and add it to the universe
        scene = createSceneGraph();
        simpleUniverse.addBranchGraph(scene);

        showNifFileChooser();
    }

    public BranchGroup createSceneGraph() {
        // Create the root of the branch graph
        BranchGroup objRoot = new BranchGroup();

        // Create the TransformGroup node and initialize it to the identity. Enable the TRANSFORM_WRITE capability so that
        // our behavior code can modify it at run time. Add it to the root of the subgraph.
        TransformGroup objTrans = new TransformGroup();
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objRoot.addChild(objTrans);

        // Create a new Behavior object that will perform the desired operation on the specified transform and add
        // it into the scene graph.
        Transform3D yAxis = new Transform3D();
        Alpha rotationAlpha = new Alpha(-1, 4000);

        RotationInterpolator rotator = new RotationInterpolator(rotationAlpha, objTrans, yAxis, 0.0f, (float) Math.PI * 2.0f);
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
        rotator.setSchedulingBounds(bounds);
        objRoot.addChild(rotator);

        // Have Java 3D perform optimizations on this scene graph.
        objRoot.compile();

        return objRoot;
    }

    /**
     * TreeNode with a value not of ArchiveEntry are ignored
     *
     * @param treeNode
     */
    private void treeNodeToDisplay(TreeNode treeNode) {
        if (treeNode.getValue() instanceof ArchiveEntry) {
            currentTreeNodeDisplayed = treeNode;
            displayNif((ArchiveEntry) treeNode.getValue());
        }
    }

    private void nextModel() {
        changeModelInTree(+1);
    }

    private void prevModel() {
        changeModelInTree(-1);
    }

    private void changeModelInTree(int step) {
        if (currentTreeNodeDisplayed != null) {
            TreeNode parentDir = currentTreeNodeDisplayed.getParent();
            if (parentDir != null) {
                LinkedList<TreeNode> siblings = parentDir.getChildren();
                if (siblings != null) {
                    // find ourselves
                    int currentIdx = siblings.indexOf(currentTreeNodeDisplayed);
                    if (currentIdx >= 0) {
                        // display model in step direction (if there is a one)
                        if (currentIdx + step > 0 && currentIdx + step < siblings.size()) {
                            // if value is a folder not nif then this call will do nothing
                            treeNodeToDisplay(siblings.get(currentIdx + step));
                        }
                    }
                }
            }
        }
    }

    private void toggleSpin() {
        spin = !spin;
        if (spinTransform != null) {
            spinTransform.setEnable(spin);
        }
    }

    private void toggleAnimateModel() {
        animateModel = !animateModel;
        update();
    }

    private void toggleHavok() {
        showHavok = !showHavok;
        update();
    }

    private void toggleVisual() {
        showVisual = !showVisual;
        update();
    }

    private void toggleBackground() {
        if (background.getApplicationBounds() == null) {
            background.setApplicationBounds(Utils3D.defaultBounds);
        } else {
            background.setApplicationBounds(null);
        }
    }

    private void toggleCycling() {
        cycle = !cycle;
    }

    public void displayNif(ArchiveEntry archiveEntry) {
        showNif(archiveEntry, meshSource, textureSource);
    }

    public void showNif(final ArchiveEntry archiveEntry, MeshSource meshSource, TextureSource textureSource) {
        parentActivity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(parentActivity, "Displaying " + archiveEntry, Toast.LENGTH_SHORT)
                        .show();
            }
        });
        BgsmSource.setBgsmSource(meshSource);
        //System.out.println("displayNif selected file: " + archiveEntry);
        display(NifToJ3d.loadNif(archiveEntry.toString(), meshSource, textureSource));
    }

    private BranchGroup hbg;

    private BranchGroup vbg;

    private void update() {
        modelGroup.removeAllChildren();
        if (showHavok) {
            modelGroup.addChild(hbg);
        }
        if (showVisual) {
            modelGroup.addChild(vbg);
        }
    }

    private ArrayList<J3dNiSkinInstance> allSkins;
    private NifJ3dSkeletonRoot inputSkeleton;

    private void display(NifJ3dVisPhysRoot nif) {
        if (nif != null) {

            J3dNiAVObject havok = nif.getHavokRoot();
            if (nif.getVisualRoot().getJ3dNiControllerManager() != null && animateModel) {
                //note self cleaning uping
                ControllerInvokerThread controllerInvokerThread = new ControllerInvokerThread(nif.getVisualRoot().getName(),
                        nif.getVisualRoot().getJ3dNiControllerManager(), havok.getJ3dNiControllerManager());
                controllerInvokerThread.start();
            }

            modelGroup.removeAllChildren();

            hbg = new BranchGroup();
            hbg.setCapability(BranchGroup.ALLOW_DETACH);

            if (showHavok && havok != null) {
                hbg.addChild(havok);
                modelGroup.addChild(hbg);
            }

            vbg = new BranchGroup();
            vbg.setCapability(BranchGroup.ALLOW_DETACH);
            vbg.setCapability(Node.ALLOW_BOUNDS_READ);

            if (showVisual) {
                // check for skins!
                if (NifJ3dSkeletonRoot.isSkeleton(nif.getNiToJ3dData())) {
                    inputSkeleton = new NifJ3dSkeletonRoot(nif.getVisualRoot(), nif.getNiToJ3dData());
                    // create skins from the skeleton and skin nif
                    allSkins = J3dNiSkinInstance.createSkins(nif.getNiToJ3dData(), inputSkeleton);

                    if (allSkins.size() > 0) {
                        // add the skins to the scene
                        for (J3dNiSkinInstance j3dNiSkinInstance : allSkins) {
                            vbg.addChild(j3dNiSkinInstance);
                        }

                        PerFrameUpdateBehavior pub = new PerFrameUpdateBehavior(new PerFrameUpdateBehavior.CallBack() {
                            @Override
                            public void update() {
                                // must be called to update the accum transform
                                inputSkeleton.updateBones();
                                for (J3dNiSkinInstance j3dNiSkinInstance : allSkins) {
                                    j3dNiSkinInstance.processSkinInstance();
                                }
                            }

                        });
                        vbg.addChild(inputSkeleton);
                        vbg.addChild(pub);
                        modelGroup.addChild(vbg);
                    }
                } else {
                    vbg.addChild(nif.getVisualRoot());

                    //vbg.outputTraversal();
                    vbg.compile();// oddly this does NOT get called automatically
                    modelGroup.addChild(vbg);
                }
            }

            System.out.println("vbg.getBounds() " + vbg.getBounds());
            simpleCameraHandler.viewBounds(vbg.getBounds());

            //TODO: the bounds was 0.21 (seems good) and eye was set to 0.8 but this this seems too close?
            if (vbg.getBounds() instanceof BoundingSphere) {
                if (((BoundingSphere) vbg.getBounds()).getRadius() < 1f)
                    simpleCameraHandler.setView(new Point3d(0, 0, 2), new Point3d());
            }

            //FOR forcing a center distance
            simpleCameraHandler.setView(new Point3d(0, 1, 4), new Point3d(0, 1, 0));

            spinTransform.setEnable(spin);

        } else {
            System.out.println("why you give display(NifJ3dVisPhysRoot nif) a null eh?");
        }

    }

    private BSAArchiveFileChooser bsaArchiveFileChooser;

    private void showNifFileChooser() {
        // show file chooser
        parentActivity.runOnUiThread(new Runnable() {
            public void run() {
                if (bsaArchiveFileChooser == null) {
                    bsaArchiveFileChooser = new BSAArchiveFileChooser(parentActivity, bsaFileSet).setExtension("nif").setFileListener(new BSAArchiveFileChooser.BsaFileSelectedListener() {
                        @Override
                        public boolean onTreeNodeLongClick(TreeNode treeNode, View view) {
                            //TODO could pop up a dialog of info or something fun
                            if (treeNode.getValue() instanceof ArchiveEntry) {
                                treeNodeToDisplay(treeNode);
                                bsaArchiveFileChooser.dismiss();
                            }
                            return false;
                        }

                        @Override
                        public void onTreeNodeClick(TreeNode treeNode, View view) {
                            if (treeNode.getValue() instanceof ArchiveEntry) {
                                treeNodeToDisplay(treeNode);
                                bsaArchiveFileChooser.dismiss();
                            }
                        }
                    }).load();
                }

                bsaArchiveFileChooser.showDialog();
            }
        });
    }

    @Override
    public void dragComplete(final MouseEvent e, DragMouseAdapter.DRAG_TYPE dragType) {
        // this drag adapter is VERY flakey on drag left? am I dropping touch events again?? doesn't look like it
        // but jogamp.newt.WindowImpl.doPointerEvent needs examination!
        if (dragType == DragMouseAdapter.DRAG_TYPE.UP) {
            // show Keyboard
            boolean kbVis = ((com.jogamp.newt.Window) e.getSource()).isKeyboardVisible();
            ((com.jogamp.newt.Window) e.getSource()).setKeyboardVisible(!kbVis);
        } else if (dragType == DragMouseAdapter.DRAG_TYPE.DOWN) {
            showNifFileChooser();
        } else if (dragType == DragMouseAdapter.DRAG_TYPE.LEFT) {
            prevModel();
        } else if (dragType == DragMouseAdapter.DRAG_TYPE.RIGHT) {
            nextModel();
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
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                toggleCycling();
            } else if (e.getKeyCode() == KeyEvent.VK_H) {
                toggleHavok();
            } else if (e.getKeyCode() == KeyEvent.VK_J) {
                toggleSpin();
            } else if (e.getKeyCode() == KeyEvent.VK_K) {
                toggleAnimateModel();
            } else if (e.getKeyCode() == KeyEvent.VK_L) {
                toggleVisual();
            } else if (e.getKeyCode() == KeyEvent.VK_P) {
                toggleBackground();
            } else if (e.getKeyCode() == KeyEvent.VK_N) {
                nextModel();
            } else if (e.getKeyCode() == KeyEvent.VK_M) {
                prevModel();
            }
        }
    }
}