package com.ingenieur.andyelderscrolls.display.nifdisplay;

import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.amrdeveloper.treeview.TreeNode;
import com.ingenieur.andyelderscrolls.display.DisplayActivity;
import com.ingenieur.andyelderscrolls.display.DisplayTester;
import com.ingenieur.andyelderscrolls.utils.BSAArchiveFileChooser;
import com.ingenieur.andyelderscrolls.utils.DragMouseAdapter;
import com.jogamp.newt.opengl.GLWindow;

import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Node;

import java.util.ArrayList;
import java.util.Iterator;

import bsaio.ArchiveEntry;
import nif.BgsmSource;
import nif.NifJ3dVisPhysRoot;
import nif.NifToJ3d;
import nif.character.NifJ3dSkeletonRoot;
import nif.j3d.J3dNiAVObject;
import nif.j3d.J3dNiNode;
import nif.j3d.J3dNiSkinInstance;
import nif.j3d.NiToJ3dData;
import nif.niobject.NiNode;
import utils.PerFrameUpdateBehavior;

public class NifDisplayTester  extends DisplayTester implements DragMouseAdapter.Listener {

    public NifDisplayTester(DisplayActivity parentActivity, GLWindow gl_window, String rootDir) {
        super(parentActivity, gl_window, rootDir);

        toggleSpin();// enable spin for straight models for funsies
    }
    protected void loaded() {
        showFileChooser();
    }

    protected void showFileChooser() {

        Thread t = new Thread() {
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

    protected void displayItem(ArchiveEntry archiveEntry) {
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parentActivity.getDisplayOverlay().setText(right(archiveEntry.toString(),48));
            }
        });

        BgsmSource.setBgsmSource(meshSource);
        //System.out.println("displayNif selected file: " + archiveEntry);
        display(NifToJ3d.loadNif(archiveEntry.toString(), meshSource, textureSource));
    }

    private BranchGroup hbg;

    private BranchGroup vbg;

    protected void update() {
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

            //TODO: spin makes things leave view! why are they not spinning around the center? nifskope shows the center! paintbrush

            float divisor = 1.0f;
            //TODO: not right but close to what's needed
            if (hasRootBone(nif.getNiToJ3dData())) {
                divisor = 15.0f;
            }
            //animated characters have a very large static bounds to reduce recalc of bounds, thou I can't see the code now
            BoundingSphere bs = (BoundingSphere) vbg.getBounds();
            bs.setRadius(bs.getRadius()/divisor);
            simpleCameraHandler.viewBounds(bs);


        } else {
            System.out.println("why you give display a null eh?");
        }

    }
    public static boolean hasRootBone(NiToJ3dData niToJ3dData) {
        Iterator iter = niToJ3dData.j3dNiAVObjectValues().iterator();

        while(iter.hasNext()) {
            J3dNiAVObject j3dNiAVObject = (J3dNiAVObject)iter.next();
            if (j3dNiAVObject.getClass() == J3dNiNode.class) {
                J3dNiNode j3dNiNode = (J3dNiNode)j3dNiAVObject;
                NiNode niNode = (NiNode)j3dNiNode.getNiAVObject();
                if (NifJ3dSkeletonRoot.isRootBoneName(niNode.name)) {
                    return true;
                }
            }
        }

        return false;
    }
}