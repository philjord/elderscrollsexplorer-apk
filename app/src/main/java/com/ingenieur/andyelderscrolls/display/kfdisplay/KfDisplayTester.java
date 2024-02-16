package com.ingenieur.andyelderscrolls.display.kfdisplay;

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

import bsaio.ArchiveEntry;
import nif.BgsmSource;
import nif.character.AttachedParts;
import nif.character.NifCharacter;
import nif.character.NifCharacterTes3;
import nif.character.NifJ3dSkeletonRoot;
import nif.j3d.J3dNiSkinInstance;
import nif.j3d.animation.tes3.J3dNiSequenceStreamHelper;
import utils.source.MediaSources;

public class KfDisplayTester extends DisplayTester implements DragMouseAdapter.Listener {

    private ArchiveEntry skeletonNifModelFile;

    private ArrayList<String> skinNifFiles = new ArrayList<String>();

    public KfDisplayTester(DisplayActivity parentActivity, GLWindow gl_window, String rootDir) {
        super(parentActivity, gl_window, rootDir);

    }
    protected void loaded() {

        Thread t = new Thread() {
            public void run() {

                bsaArchiveFileChooser = new BSAArchiveFileChooser(parentActivity, bsaFileSet).setExtension("nif");
                bsaArchiveFileChooser.setFilter(new BSAArchiveFileChooser.BSAArchiveFileChooserFilter() {
                    @Override
                    public boolean accept(ArchiveEntry ae) {
                        return ae.getFileName().contains("skeleton");
                    }
                });
                bsaArchiveFileChooser.setFileListener(new BSAArchiveFileChooser.BsaFileSelectedListener() {
                    @Override
                    public boolean onTreeNodeLongClick(TreeNode treeNode, View view) {
                        return false;
                    }

                    @Override
                    public void onTreeNodeClick(TreeNode treeNode, View view) {
                        if (treeNode.getValue() instanceof ArchiveEntry) {
                            skeletonNifModelFile = (ArchiveEntry) treeNode.getValue();
                            currentTreeNodeDisplayed = treeNode;
                            bsaArchiveFileChooser.dismiss();
                            selectSkins();
                        }
                    }
                }).load();

                // show file chooser
                parentActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(parentActivity, "Please select a skeleton nif file", Toast.LENGTH_SHORT)
                                .show();
                        bsaArchiveFileChooser.showDialog();
                    }
                });
            }};
        t.start();
    }

    private void selectSkins() {
        parentActivity.runOnUiThread(new Runnable() {
            public void run() {
                bsaArchiveFileChooser = new BSAArchiveFileChooser(parentActivity, bsaFileSet).setExtension("nif").setMultiple(true).setFileListener(new BSAArchiveFileChooser.BsaFileSelectedListener() {
                    @Override
                    public boolean onTreeNodeLongClick(TreeNode treeNode, View view) {
                        if (treeNode.getValue() instanceof ArchiveEntry) {
                            treeNode.setSelected(!treeNode.isSelected());
                            bsaArchiveFileChooser.updateNodesState();
                            currentTreeNodeDisplayed = treeNode;
                            if(treeNode.isSelected())
                                skinNifFiles.add(((ArchiveEntry) treeNode.getValue()).toString());
                            else
                                skinNifFiles.remove(((ArchiveEntry) treeNode.getValue()).toString());
                            return true;
                        } else {
                            return false;
                        }
                    }

                    @Override
                    public void onTreeNodeClick(TreeNode treeNode, View view) {
                        // null indicates done button pressed, not a node clicked
                        if(treeNode == null ){
                            bsaArchiveFileChooser.dismiss();
                            showFileChooser();
                        } else if (treeNode.getValue() instanceof ArchiveEntry) {
                            treeNode.setSelected(!treeNode.isSelected());
                            bsaArchiveFileChooser.updateNodesState();
                            currentTreeNodeDisplayed = treeNode;
                            if(treeNode.isSelected())
                                skinNifFiles.add(((ArchiveEntry) treeNode.getValue()).toString());
                            else
                                skinNifFiles.remove(((ArchiveEntry) treeNode.getValue()).toString());
                        }
                    }
                }).load();

                Toast.makeText(parentActivity, "Please select skin file(s)", Toast.LENGTH_SHORT)
                        .show();
                bsaArchiveFileChooser.showDialog();
                bsaArchiveFileChooser.expandToTreeNode(currentTreeNodeDisplayed);
            }
        });

    }

    protected void showFileChooser() {
        if (!rootDir.toLowerCase().contains("morrowind")) {
            parentActivity.runOnUiThread(new Runnable() {
                public void run() {


                    if (bsaArchiveFileChooser == null || true) {
                        bsaArchiveFileChooser = new BSAArchiveFileChooser(parentActivity, bsaFileSet).setExtension("kf").setFileListener(new BSAArchiveFileChooser.BsaFileSelectedListener() {
                            @Override
                            public boolean onTreeNodeLongClick(TreeNode treeNode, View view) {
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
                    Toast.makeText(parentActivity, "Please select kf file(s)", Toast.LENGTH_SHORT)
                            .show();
                    bsaArchiveFileChooser.showDialog();
                    bsaArchiveFileChooser.expandToTreeNode(currentTreeNodeDisplayed);
                }
            });

        } else {
            //morrowind has a single kf files named after skeleton
            if (nifCharacterTes3 == null)
                displayTes3();

            // now display all sequences from the kf file for user to pickage
            final J3dNiSequenceStreamHelper j3dNiSequenceStreamHelper = nifCharacterTes3.getJ3dNiSequenceStreamHelper();

            parentActivity.runOnUiThread(new Runnable() {
                public void run() {
                    if(j3dNiSequenceStreamHelper != null ) {
                        new Tes3AnimChooser(parentActivity, "Anims availible", j3dNiSequenceStreamHelper.getAllSequences()).setAnimListener(new Tes3AnimChooser.AnimSelectedListener() {
                            @Override
                            public void animSelected(String anim) {
                                nifCharacterTes3.startAnimation(anim, false);
                            }
                        }).showDialog();
                    } else {
                        Toast.makeText(parentActivity, "j3dNiSequenceStreamHelper null", Toast.LENGTH_LONG)
                                .show();
                    }
                }
            });
        }
    }
    protected void update() {
    }

    protected void displayItem(ArchiveEntry archiveEntry) {
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parentActivity.getDisplayOverlay().setText(right(archiveEntry.toString(),48));
            }
        });
        if (!rootDir.toLowerCase().contains("morrowind")) {
            modelGroup.removeAllChildren();

            BranchGroup bg = new BranchGroup();
            bg.setCapability(BranchGroup.ALLOW_DETACH);

            NifJ3dSkeletonRoot.showBoneMarkers = true;
            J3dNiSkinInstance.showSkinBoneMarkers = false;//TODO: this doesn't show anything?

            BgsmSource.setBgsmSource(meshSource);
            MediaSources mediaSources = new MediaSources(meshSource, textureSource, null);

            ArrayList<String> idleAnimations = new ArrayList<String>();

            if (archiveEntry != null) {
                idleAnimations.add(archiveEntry.toString());
            }

            // now add the root to the scene so the controller sequence is live
            NifCharacter nifCharacter = new NifCharacter(skeletonNifModelFile.toString(), skinNifFiles, mediaSources, idleAnimations);
            nifCharacter.setCapability(Node.ALLOW_BOUNDS_READ);
            bg.addChild(nifCharacter);

            modelGroup.addChild(bg);

            //animated characters have a very large static bounds to reduce recalc of bounds, thou I can't see the code now
            BoundingSphere bs = (BoundingSphere) nifCharacter.getBounds();
            bs.setRadius(bs.getRadius()/15.0f);
            simpleCameraHandler.viewBounds(bs);

        } else {
            displayTes3();
        }
    }

    /**
     * Note called once at start, not re called like display above
     */
    private NifCharacterTes3 nifCharacterTes3;

    private void displayTes3() {

        modelGroup.removeAllChildren();

        BranchGroup bg = new BranchGroup();
        bg.setCapability(BranchGroup.ALLOW_DETACH);

        NifJ3dSkeletonRoot.showBoneMarkers = true;
        J3dNiSkinInstance.showSkinBoneMarkers = false;//TODO: this doesn't show anything?

        BgsmSource.setBgsmSource(meshSource);
        MediaSources mediaSources = new MediaSources(meshSource, textureSource, null);

        AttachedParts attachFileNames = new AttachedParts();
        attachFileNames.addPart(AttachedParts.Part.Root, skinNifFiles.get(0));

        nifCharacterTes3 = new NifCharacterTes3(skeletonNifModelFile.toString(), attachFileNames, mediaSources);
        bg.addChild(nifCharacterTes3);

        modelGroup.addChild(bg);
        //simpleCameraHandler.viewBounds(nifCharacterTes3.getBounds());
    }
}