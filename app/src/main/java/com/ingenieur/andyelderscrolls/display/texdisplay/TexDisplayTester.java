package com.ingenieur.andyelderscrolls.display.texdisplay;

import android.view.View;

import com.amrdeveloper.treeview.TreeNode;
import com.ingenieur.andyelderscrolls.display.DisplayActivity;
import com.ingenieur.andyelderscrolls.display.DisplayTester;
import com.ingenieur.andyelderscrolls.utils.BSAArchiveFileChooser;
import com.ingenieur.andyelderscrolls.utils.DragMouseAdapter;
import com.jogamp.newt.opengl.GLWindow;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.IndexedTriangleArray;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Texture;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.compressedtexture.CompressedTextureLoader;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.TexCoord2f;

import java.io.InputStream;

import bsa.source.BsaTextureSource;
import bsaio.ArchiveEntry;

public class TexDisplayTester extends DisplayTester implements DragMouseAdapter.Listener {

    public TexDisplayTester(DisplayActivity parentActivity, GLWindow gl_window, String rootDir) {
        super(parentActivity, gl_window, rootDir);

    }
    protected void loaded() {
        showFileChooser();
    }

    protected void displayItem(final ArchiveEntry archiveEntry ) {
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parentActivity.getDisplayOverlay().setText(right(archiveEntry.toString(),48));
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

    protected void update() {
    }

    private void display(BranchGroup texBG) {
        if (texBG != null) {
            modelGroup.removeAllChildren();
            modelGroup.addChild(texBG);

            //FOR forcing a center distance
            simpleCameraHandler.setView(new Point3d(0, 0, 2), new Point3d(0, 0, 0));
        } else {
            System.out.println("why you give display a null eh?");
        }

    }

    protected void showFileChooser() {

        Thread t = new Thread() {
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
                // show file chooser
                parentActivity.runOnUiThread(new Runnable() {
                    public void run() {


                        bsaArchiveFileChooser.showDialog();
                        bsaArchiveFileChooser.expandToTreeNode(currentTreeNodeDisplayed);
                    }
                });
            }
        };
        t.start();
    }

}