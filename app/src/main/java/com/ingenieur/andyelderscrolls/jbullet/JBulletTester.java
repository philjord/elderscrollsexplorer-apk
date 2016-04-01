package com.ingenieur.andyelderscrolls.jbullet;


import android.app.Activity;
import android.widget.Toast;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;

import com.ingenieur.andyelderscrolls.utils.FileChooser;

import java.io.File;

import javax.vecmath.Vector3f;

import nif.NifToJ3d;
import nifbullet.BulletNifModelClassifier;
import utils.source.file.FileMediaRoots;
import utils.source.file.FileMeshSource;

public class JBulletTester
{
	// Gravity
	private static Vector3f gravity = new Vector3f(0f, 0f, -9.81f);


	// this is the most important class
	private static DynamicsWorld dynamicsWorld = null;

	private static BroadphaseInterface broadphase;

	private static CollisionDispatcher dispatcher;

	private static ConstraintSolver solver;

	private static DefaultCollisionConfiguration collisionConfiguration;

	private Activity parentActivity;

	private File chooserStartFolder;



	public JBulletTester(Activity parentActivity2, File rootDir)
	{
		chooserStartFolder = new File(rootDir, "Meshes");
		this.parentActivity = parentActivity2;
		NifToJ3d.SUPPRESS_EXCEPTIONS = false;


		FileMediaRoots.setFixedRoot(rootDir.getAbsolutePath());


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

		parentActivity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				Toast.makeText(parentActivity, "Please select a nif file to test load as jbullet", Toast.LENGTH_SHORT)
						.show();

				new FileChooser(parentActivity, chooserStartFolder).setExtension("nif").setFileListener(new FileChooser.FileSelectedListener()
				{
					@Override
					public void fileSelected(final File file)
					{
						chooserStartFolder = file;
						BulletNifModelClassifier.testNif(file.getAbsolutePath(), new FileMeshSource());
						BulletNifModelClassifier.createNifBullet(file.getAbsolutePath(), new FileMeshSource(), 0).addToDynamicsWorld(
								dynamicsWorld);
					}
				}).showDialog();
			}
		});
	}


}