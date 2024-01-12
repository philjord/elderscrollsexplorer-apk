package com.ingenieur.andyelderscrolls.display;

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

import org.jogamp.java3d.AmbientLight;
import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.Light;
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

import java.util.LinkedList;

import bsa.source.BsaMeshSource;
import bsa.source.BsaTextureSource;
import bsaio.ArchiveEntry;
import bsaio.ArchiveFile;
import bsaio.BSArchiveSet;
import bsaio.BSArchiveSetUri;
import nif.NifToJ3d;
import nif.appearance.NiGeometryAppearanceFactoryShader;
import nif.j3d.particles.tes3.J3dNiParticles;
import nif.shader.NiGeometryAppearanceShader;
import nif.shader.ShaderSourceIO;
import tools3d.camera.simple.SimpleCameraHandler;
import tools3d.mixed3d2d.Canvas3D2D;
import tools3d.utils.Utils3D;
import tools3d.utils.scenegraph.SpinTransform;
import utils.source.MeshSource;
import utils.source.TextureSource;

public abstract class DisplayTester implements DragMouseAdapter.Listener {

    private TransformGroup spinTransformGroup = new TransformGroup();

    private TransformGroup rotateTransformGroup = new TransformGroup();

    private SpinTransform spinTransform;

    private boolean cycle = true;

    protected boolean showHavok = true;

    protected boolean showVisual = true;

    protected boolean animateModel = true;

    private boolean spin = false;

    private SimpleUniverse simpleUniverse;

    private Background background = new Background();

    public Canvas3D2D canvas3D2D;

    private AndyFPSCounter fpsCounter;

    private DragMouseAdapter dragMouseAdapter = new DragMouseAdapter();

    protected SimpleCameraHandler simpleCameraHandler;

    protected TreeNode currentTreeNodeDisplayed;

    protected BranchGroup modelGroup = new BranchGroup();

    protected FragmentActivity parentActivity;

    protected BSAArchiveFileChooser bsaArchiveFileChooser;

    protected BSArchiveSet bsaFileSet;
    protected MeshSource meshSource = null;
    protected TextureSource textureSource = null;

    protected String rootDir;

    public DisplayTester(FragmentActivity parentActivity, GLWindow gl_window, String rootDir) {
        this.parentActivity = parentActivity;
        this.rootDir = rootDir;
        NifToJ3d.SUPPRESS_EXCEPTIONS = false;
        NiGeometryAppearanceShader.OUTPUT_BINDINGS = true;
        ArchiveFile.USE_FILE_MAPS = false;
        ArchiveFile.USE_MINI_CHANNEL_MAPS = true;
        ArchiveFile.USE_NON_NATIVE_ZIP = false;

        NiGeometryAppearanceFactoryShader.setAsDefault();
        ShaderSourceIO.ES_SHADERS = true;

        BsaMeshSource.FALLBACK_TO_FILE_SOURCE = false;

        String[] BSARoots = new String[]{rootDir};

        bsaFileSet = new BSArchiveSetUri(this.parentActivity, BSARoots, true);
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

        Color3f dlColor = new Color3f(0.8f, 0.8f, 0.6f);
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
        bg.addChild(dirLight);

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

        simpleUniverse.addBranchGraph(bg);

        canvas3D2D.getView().setBackClipDistance(2000);
        canvas3D2D.getView().setFrontClipDistance(0.01f);
        canvas3D2D.getGLWindow().addKeyListener(new KeyHandler());

        dragMouseAdapter.setListener(this);
        canvas3D2D.getGLWindow().addMouseListener(dragMouseAdapter);

    }

    private void nextItem() {
        changeModelInTree(+1);
    }

    private void prevItem() {
        changeModelInTree(-1);
    }

    private void changeModelInTree(int step) {
        if (currentTreeNodeDisplayed != null) {
            TreeNode parentDir = currentTreeNodeDisplayed.getParent();
            if (parentDir != null) {
                LinkedList<TreeNode> siblings = new LinkedList<TreeNode>(parentDir.getChildren());
                // strip folders
                for (int i = 0; i < siblings.size(); i++) {
                    if(siblings.get(i) instanceof BSAArchiveFileChooser.FolderNode) {
                        siblings.remove(i);
                        i--;
                    }
                }

                if (siblings.size() > 0) {
                    // find ourselves
                    int currentIdx = siblings.indexOf(currentTreeNodeDisplayed);
                    if (currentIdx >= 0) {
                        // display model in step direction (if there is a one)
                        // if it's off check for existing cousin instead
                        int newPos = currentIdx + step;
                        if (newPos < 0 || newPos >= siblings.size()) {
                            TreeNode grandParentDir = parentDir.getParent();
                            if (grandParentDir != null) {
                                LinkedList<TreeNode> auntsuncles = new LinkedList<TreeNode>(grandParentDir.getChildren());
                                // only folders
                                for (int i = 0; i < siblings.size(); i++) {
                                    if(!(siblings.get(i) instanceof BSAArchiveFileChooser.FolderNode)) {
                                        siblings.remove(i);
                                        i--;
                                    }
                                }
                                if (auntsuncles.size() > 0) {
                                    int parentIdx = auntsuncles.indexOf(parentDir);
                                    if (parentIdx >= 0) {
                                        TreeNode auntuncle = null;
                                        if (newPos < 0 && parentIdx > 0) {
                                            auntuncle = auntsuncles.get(parentIdx - 1);
                                        } else if (newPos >= siblings.size() && parentIdx < auntsuncles.size() -1) {
                                            auntuncle = auntsuncles.get(parentIdx + 1);
                                        }

                                        if(auntuncle != null ) {
                                            LinkedList<TreeNode> cousins = new LinkedList<TreeNode>(auntuncle.getChildren());
                                            if (cousins != null) {
                                                // strip folders
                                                for (int i = 0; i < siblings.size(); i++) {
                                                    if (siblings.get(i) instanceof BSAArchiveFileChooser.FolderNode) {
                                                        siblings.remove(i);
                                                        i--;
                                                    }
                                                }

                                                if(cousins.size() > 0) {
                                                    TreeNode cousin = null;
                                                    if (newPos < 0) {
                                                        cousin = cousins.get(cousins.size() - 1);
                                                    } else if (newPos >= siblings.size()) {
                                                        cousin = cousins.get(0);
                                                    }

                                                    if (cousin != null) {
                                                        treeNodeToDisplay(cousin);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // if value is a folder not nif then this call will do nothing
                            treeNodeToDisplay(siblings.get(newPos));
                        }
                    }
                }
            }
        }
    }

    /**
     * TreeNode with a value not of ArchiveEntry are ignored
     *
     * @param treeNode
     */
    protected void treeNodeToDisplay(TreeNode treeNode) {
        if (treeNode.getValue() instanceof ArchiveEntry) {
            currentTreeNodeDisplayed = treeNode;
            displayItem((ArchiveEntry) treeNode.getValue());
        }
    }

    protected void toggleSpin() {
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


    @Override
    public void dragComplete(final MouseEvent e, DragMouseAdapter.DRAG_TYPE dragType) {
        if (dragType == DragMouseAdapter.DRAG_TYPE.UP) {
            // show Keyboard
            boolean kbVis = ((com.jogamp.newt.Window) e.getSource()).isKeyboardVisible();
            ((com.jogamp.newt.Window) e.getSource()).setKeyboardVisible(!kbVis);
        } else if (dragType == DragMouseAdapter.DRAG_TYPE.DOWN) {
            showFileChooser();
        } else if (dragType == DragMouseAdapter.DRAG_TYPE.LEFT) {
            prevItem();
        } else if (dragType == DragMouseAdapter.DRAG_TYPE.RIGHT) {
            nextItem();
        }
    }

    protected abstract void displayItem(ArchiveEntry archiveEntry);
    protected abstract void update();
    protected abstract void showFileChooser();

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
                nextItem();
            } else if (e.getKeyCode() == KeyEvent.VK_M) {
                prevItem();
            }
        }
    }

    public static String right(String s, int c) {
        if(s!=null && c<=s.length())
            return s.substring(s.length()-c);
        else
            return s;
    }
}