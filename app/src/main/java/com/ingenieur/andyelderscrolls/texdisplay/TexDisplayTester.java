package com.ingenieur.andyelderscrolls.texdisplay;

import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.amrdeveloper.treeview.TreeNode;
import com.ingenieur.andyelderscrolls.nifdisplay.NifDisplayTester;
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
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.IndexedTriangleArray;
import org.jogamp.java3d.Light;
import org.jogamp.java3d.PointLight;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.RotationInterpolator;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Texture;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.compressedtexture.CompressedTextureLoader;
import org.jogamp.java3d.utils.behaviors.mouse.MouseRotate;
import org.jogamp.java3d.utils.shader.Cube;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Quat4f;
import org.jogamp.vecmath.TexCoord2f;
import org.jogamp.vecmath.Vector3f;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;

import bsa.source.BsaMeshSource;
import bsa.source.BsaTextureSource;
import bsaio.ArchiveEntry;
import bsaio.ArchiveFile;
import bsaio.BSArchiveSet;
import bsaio.BSArchiveSetUri;
import nif.appearance.NiGeometryAppearanceFactoryShader;
import nif.character.NifJ3dSkeletonRoot;
import nif.j3d.J3dNiSkinInstance;
import nif.j3d.particles.tes3.J3dNiParticles;
import nif.shader.ShaderSourceIO;
import tools3d.camera.simple.SimpleCameraHandler;
import tools3d.mixed3d2d.Canvas3D2D;
import tools3d.utils.Utils3D;
import tools3d.utils.scenegraph.SpinTransform;
import utils.source.MeshSource;
import utils.source.TextureSource;

public class TexDisplayTester implements DragMouseAdapter.Listener {
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

    private FragmentActivity parentActivity;

    private TreeNode currentTreeNodeDisplayed;

    private DragMouseAdapter dragMouseAdapter = new DragMouseAdapter();

    private BSArchiveSet bsaFileSet;
    private TextureSource textureSource = null;

    public TexDisplayTester(FragmentActivity parentActivity, GLWindow gl_window, String rootDir) {
        this.parentActivity = parentActivity;

        ArchiveFile.USE_FILE_MAPS = false;
        ArchiveFile.USE_MINI_CHANNEL_MAPS = true;
        ArchiveFile.USE_NON_NATIVE_ZIP = false;

        NiGeometryAppearanceFactoryShader.setAsDefault();
        ShaderSourceIO.ES_SHADERS = true;

        BsaMeshSource.FALLBACK_TO_FILE_SOURCE = false;

        String[] BSARoots = new String[]{rootDir};

        bsaFileSet = new BSArchiveSetUri(this.parentActivity, BSARoots, true);
        textureSource = new BsaTextureSource(bsaFileSet);

        canvas3D2D = new Canvas3D2D(gl_window);

        simpleUniverse = new SimpleUniverse(canvas3D2D);
        CompressedTextureLoader.setAnisotropicFilterDegree(8);

        spinTransformGroup.addChild(rotateTransformGroup);
        rotateTransformGroup.addChild(modelGroup);
        simpleCameraHandler = new SimpleCameraHandler(simpleUniverse.getViewingPlatform(), simpleUniverse.getCanvas(), modelGroup,
                rotateTransformGroup, false, true);

        spinTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        spinTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        modelGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        modelGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);

        // Create ambient light	and add it

        BranchGroup bg = new BranchGroup();

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

        TransformGroup tg = new TransformGroup();
        Transform3D t = new Transform3D();
        t.rotY(Math.PI / 8);
        t.setTranslation(new Vector3f(0, 0, 0));
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

        canvas3D2D.getView().setBackClipDistance(5000);
        canvas3D2D.getView().setFrontClipDistance(0.1f);
        canvas3D2D.getGLWindow().addKeyListener(new TexDisplayTester.KeyHandler());

        dragMouseAdapter.setListener(this);
        canvas3D2D.getGLWindow().addMouseListener(dragMouseAdapter);

        // Create the content branch and add it to the universe
        scene = createSceneGraph();
        simpleUniverse.addBranchGraph(scene);

        showTexFileChooser();
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
            displayTex((ArchiveEntry) treeNode.getValue());
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

    public void displayTex(ArchiveEntry archiveEntry) {
        showTex(archiveEntry, textureSource);
    }

    public void showTex(final ArchiveEntry archiveEntry, TextureSource textureSource) {
        parentActivity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(parentActivity, "Displaying " + archiveEntry, Toast.LENGTH_SHORT)
                        .show();
            }
        });

        //System.out.println("displayNif selected file: " + archiveEntry);
        display(createSceneGraph(archiveEntry.toString(), new BsaTextureSource(bsaFileSet).getInputStream(archiveEntry.toString())));
    }

    /**
     * Builds a scenegraph for the application to render.
     * @return the root level of the scenegraph
     */
    private BranchGroup createSceneGraph(String filename, InputStream inputStream) {
        final BranchGroup objRoot = new BranchGroup();

        Texture tex = CompressedTextureLoader.UNKNOWN.getTexture(filename, inputStream);

        double w = tex.getWidth();
        double h = tex.getHeight();
        float sw = (float)(h > w ? 0.5f : 0.5f * (w / h));
        float sh = (float)(w > h ? 0.5f : 0.5f * (h / w));


        Shape3D shape = new Shape3D();
        IndexedTriangleArray tri = new IndexedTriangleArray(4,
                GeometryArray.USE_COORD_INDEX_ONLY | GeometryArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2, 6);
        tri.setCoordinate(0, new Point3f(-sw, -sh, 0.0f));
        tri.setCoordinate(1, new Point3f(sw, -sh, 0.0f));
        tri.setCoordinate(2, new Point3f(sw, sh, 0.0f));
        tri.setCoordinate(3, new Point3f(-sw, sh, 0.0f));

        tri.setTextureCoordinate(0, 0, new TexCoord2f(0.0f, 1.0f));
        tri.setTextureCoordinate(0, 1, new TexCoord2f(1.0f, 1.0f));
        tri.setTextureCoordinate(0, 2, new TexCoord2f(1.0f, 0.0f));
        tri.setTextureCoordinate(0, 3, new TexCoord2f(0.0f, 0.0f));

        tri.setCoordinateIndex(0, 0);
        tri.setCoordinateIndex(1, 1);
        tri.setCoordinateIndex(2, 2);
        tri.setCoordinateIndex(3, 0);
        tri.setCoordinateIndex(4, 3);
        tri.setCoordinateIndex(5, 2);

        shape.setGeometry(tri);

        // Because we're about to spin this triangle, be sure to draw
        // backfaces.  If we don't, the back side of the triangle is invisible.
        SimpleShaderAppearance ap = new SimpleShaderAppearance();
        PolygonAttributes pa = new PolygonAttributes();
        pa.setCullFace(PolygonAttributes.CULL_NONE);
        ap.setPolygonAttributes(pa);

        ap.setTexture(tex);

        shape.setAppearance(ap);

        TransformGroup tg2 = new TransformGroup();
        tg2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        tg2.addChild(shape);
        objRoot.addChild(tg2);

        objRoot.setCapability(BranchGroup.ALLOW_DETACH);

        return objRoot;
    }

    private BranchGroup vbg;

    private ArrayList<J3dNiSkinInstance> allSkins;
    private NifJ3dSkeletonRoot inputSkeleton;

    private void display(BranchGroup texBG) {
        if (texBG != null) {
            modelGroup.removeAllChildren();
            modelGroup.addChild(texBG);

            //FOR forcing a center distance
            simpleCameraHandler.setView(new Point3d(0, -2, 2), new Point3d(0, 1, 0));
        } else {
            System.out.println("why you give display(NifJ3dVisPhysRoot nif) a null eh?");
        }

    }

    private BSAArchiveFileChooser bsaArchiveFileChooser;

    private void showTexFileChooser() {
        // show file chooser
        parentActivity.runOnUiThread(new Runnable() {
            public void run() {
                if (bsaArchiveFileChooser == null) {
                    bsaArchiveFileChooser = new BSAArchiveFileChooser(parentActivity, bsaFileSet).setExtension("ktx").setFileListener(new BSAArchiveFileChooser.BsaFileSelectedListener() {
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
        //TODO: DragMouseAdapter is my own code so check there first
        // this drag adapter is VERY flakey on drag left? am I dropping touch events again?? doesn't look like it
        // but jogamp.newt.WindowImpl.doPointerEvent needs examination!
        if (dragType == DragMouseAdapter.DRAG_TYPE.UP) {
            // show Keyboard
            boolean kbVis = ((com.jogamp.newt.Window) e.getSource()).isKeyboardVisible();
            ((com.jogamp.newt.Window) e.getSource()).setKeyboardVisible(!kbVis);
        } else if (dragType == DragMouseAdapter.DRAG_TYPE.DOWN) {
            showTexFileChooser();
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