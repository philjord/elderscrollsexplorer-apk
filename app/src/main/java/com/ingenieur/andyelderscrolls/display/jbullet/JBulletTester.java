package com.ingenieur.andyelderscrolls.display.jbullet;

import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.amrdeveloper.treeview.TreeNode;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.ingenieur.andyelderscrolls.utils.BSAArchiveFileChooser;

import org.jogamp.vecmath.Vector3f;

import bsa.source.BsaMeshSource;
import bsa.source.BsaTextureSource;
import bsaio.ArchiveEntry;
import bsaio.ArchiveFile;
import bsaio.BSArchiveSet;
import bsaio.BSArchiveSetUri;
import nif.NifToJ3d;
import nif.appearance.NiGeometryAppearanceFactoryShader;
import nif.shader.NiGeometryAppearanceShader;
import nif.shader.ShaderSourceIO;
import nifbullet.BulletNifModelClassifier;
import scrollsexplorer.GameConfig;
import utils.source.MeshSource;
import utils.source.TextureSource;

public class JBulletTester {
    // Gravity
    private static Vector3f gravity = new Vector3f(0f, 0f, -9.81f);


    // this is the most important class
    private static DynamicsWorld dynamicsWorld = null;

    private static BroadphaseInterface broadphase;

    private static CollisionDispatcher dispatcher;

    private static ConstraintSolver solver;

    private static DefaultCollisionConfiguration collisionConfiguration;

    private FragmentActivity parentActivity;

    private GameConfig gameConfigToLoad;

    private ArchiveEntry selectedFile;

    private BSAArchiveFileChooser bsaArchiveFileChooser;

    private BSArchiveSet bsaFileSet;
    private MeshSource meshSource = null;
    private TextureSource textureSource = null;

    public JBulletTester(FragmentActivity parentActivity2, GameConfig gameConfigToLoad) {
        this.gameConfigToLoad = gameConfigToLoad;
        this.parentActivity = parentActivity2;
        NifToJ3d.SUPPRESS_EXCEPTIONS = false;
        NiGeometryAppearanceShader.OUTPUT_BINDINGS = true;
        ArchiveFile.USE_FILE_MAPS = false;
        ArchiveFile.USE_MINI_CHANNEL_MAPS = true;
        ArchiveFile.USE_NON_NATIVE_ZIP = false;

        NiGeometryAppearanceFactoryShader.setAsDefault();
        ShaderSourceIO.ES_SHADERS = true;

        BsaMeshSource.FALLBACK_TO_FILE_SOURCE = false;

        String[] BSARoots = new String[]{gameConfigToLoad.scrollsFolder};

        bsaFileSet = new BSArchiveSetUri(this.parentActivity, BSARoots, true);
        meshSource = new BsaMeshSource(bsaFileSet);
        textureSource = new BsaTextureSource(bsaFileSet);

        // collision configuration contains default setup for memory, collision setup
        collisionConfiguration = new DefaultCollisionConfiguration();

        // use the default collision dispatcher. For parallel processing you can use a diffent dispatcher (see Extras/BulletMultiThreaded)
        dispatcher = new CollisionDispatcher(collisionConfiguration);

        broadphase = new DbvtBroadphase();

        // the default constraint solver. For parallel processing you can use a different solver (see Extras/BulletMultiThreaded)
        SequentialImpulseConstraintSolver sol = new SequentialImpulseConstraintSolver();
        solver = sol;

        // TODO: needed for SimpleDynamicsWorld
        //sol.setSolverMode(sol.getSolverMode() & ~SolverMode.SOLVER_CACHE_FRIENDLY.getMask());

        dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);

        dynamicsWorld.setGravity(gravity);

        parentActivity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(parentActivity, "Please select a nif file to test load as jbullet", Toast.LENGTH_SHORT)
                        .show();

                //TODO: can't reshow teh same bsaArchiveFileChooser it shows nothing
                if (bsaArchiveFileChooser == null || true) {
                    //TODO: reuse teh file chooser so the selection is still the same?
                    bsaArchiveFileChooser = new BSAArchiveFileChooser(parentActivity, bsaFileSet).setExtension("nif").setFileListener(new BSAArchiveFileChooser.BsaFileSelectedListener() {
                        @Override
                        public boolean onTreeNodeLongClick(TreeNode treeNode, View view) {
                            return false;
                        }

                        @Override
                        public void onTreeNodeClick(TreeNode treeNode, View view) {
                            if (treeNode.getValue() instanceof ArchiveEntry) {
                                selectedFile = (ArchiveEntry) treeNode.getValue();

                                //BulletNifModelClassifier.testNif(selectedFile.toString(), meshSource);
                                //TODO: this doesn't show anything, is it trying to use teh jbullet test window?
                                BulletNifModelClassifier.createNifBullet(selectedFile.toString(), meshSource, 0).addToDynamicsWorld(
                                        dynamicsWorld);
                                bsaArchiveFileChooser.dismiss();
                            }
                        }
                    }).load();
                }

                bsaArchiveFileChooser.showDialog();
            }
        });
    }


}