package com.ingenieur.andyelderscrolls.andyesexplorer;

import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.ingenieur.andyelderscrolls.utils.AndyFPSCounter;
import com.ingenieur.andyelderscrolls.utils.AndyHUDCompass;
import com.ingenieur.andyelderscrolls.utils.AndyHUDPosition;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.sun.j3d.utils.universe.ViewingPlatform;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Group;
import javax.media.j3d.Light;
import javax.media.j3d.ShaderError;
import javax.media.j3d.ShaderErrorListener;
import javax.vecmath.Color3f;
import javax.vecmath.Point2f;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import esmj3d.j3d.BethRenderSettings;
import nif.appearance.NiGeometryAppearanceFactoryShader;
import nifbullet.NavigationProcessorBullet;
import nifbullet.cha.NBControlledChar;
import scrollsexplorer.IDashboard;
import scrollsexplorer.simpleclient.NewtJumpKeyListener;
import scrollsexplorer.simpleclient.SimpleBethCellManager;
import scrollsexplorer.simpleclient.SimpleWalkSetupInterface;
import scrollsexplorer.simpleclient.mouseover.ActionableMouseOverHandler;
import scrollsexplorer.simpleclient.mouseover.AdminMouseOverHandler;
import scrollsexplorer.simpleclient.physics.PhysicsSystem;
import scrollsexplorer.simpleclient.scenegraph.LoadingInfoBehavior;
import tools3d.camera.CameraPanel;
import tools3d.camera.HeadCamDolly;
import tools3d.camera.ICameraPanel;
import tools3d.camera.TrailerCamDolly;
import tools3d.mixed3d2d.Canvas3D2D;
import tools3d.mixed3d2d.curvehud.elements.HUDCrossHair;
import tools3d.mixed3d2d.curvehud.elements.HUDText;
import tools3d.navigation.AvatarCollisionInfo;
import tools3d.navigation.AvatarLocation;
import tools3d.navigation.NavigationTemporalBehaviour;
import tools3d.navigation.twocircles.NavigationInputNewtLook;
import tools3d.navigation.twocircles.NavigationInputNewtMove;
import tools3d.universe.VisualPhysicalUniverse;
import utils.source.MeshSource;

/**
 * Created by phil on 3/11/2016.
 */


/**
 * A class to pull the keyboard nav, bullet phys, nif displayable, canvas2d3d overlays,
 * physics display together,
 * <p/>
 * but no particular way to load nifs, esm, comms or anything else
 *
 * @author philip
 */
public class AndySimpleWalkSetup implements SimpleWalkSetupInterface
{
	public static boolean TRAILER_CAM = false;
	private boolean enabled = false;

	public VisualPhysicalUniverse universe;

	private BranchGroup modelGroup = new BranchGroup();

	private BranchGroup physicsGroup;

	private BranchGroup visualGroup;

	private BranchGroup behaviourBranch;

	private NavigationTemporalBehaviour navigationTemporalBehaviour;

	private NavigationProcessorBullet navigationProcessor;

	private ICameraPanel cameraPanel;

	private AvatarLocation avatarLocation = new AvatarLocation();

	private AvatarCollisionInfo avatarCollisionInfo = new AvatarCollisionInfo(avatarLocation, 0.5f, 1.8f, 0.35f, 0.8f);

	//private NavigationInputNewtKey keyNavigationInputNewt;
	private NavigationInputNewtMove keyNavigationInputNewt;

	//private NavigationInputNewtMouseDraggedLocked newtMouseInputListener;
	private NavigationInputNewtLook newtMouseInputListener;

	//private NavigationInputNewtLookMove newtMouseInputListener;


	private NewtJumpKeyListener jumpKeyListener;

	private NewtMiscKeyHandler newtMiscKeyHandler = new NewtMiscKeyHandler();

	private boolean showHavok = false;

	private boolean showVisual = true;

	private AndyFPSCounter fpsCounter;

	private AndyHUDCompass hudcompass;

	private HUDCrossHair hudCrossHair;

	private AndyHUDPosition hudPos;

	private HUDText loadInfo;
	private LoadingInfoBehavior loadingInfoBehavior;

	private PhysicsSystem physicsSystem;

	private ActionableMouseOverHandler cameraMouseOver;

	private AdminMouseOverHandler cameraAdminMouseOverHandler;

	private boolean freefly = false;

	private AmbientLight ambLight = null;

	private DirectionalLight dirLight = null;


	//Can't use as threading causes massive trouble for scene loading
	//	private StructureUpdateBehavior structureUpdateBehavior;

	private NavigationProcessorBullet.NbccProvider nbccProvider = new NavigationProcessorBullet.NbccProvider()
	{
		@Override
		public NBControlledChar getNBControlledChar()
		{
			return physicsSystem.getNBControlledChar();
		}
	};

	public AndySimpleWalkSetup(String frameName, GLWindow gl_window)
	{
		NiGeometryAppearanceFactoryShader.setAsDefault();

		//kick off with a universe ***************************
		universe = new VisualPhysicalUniverse();

		//basic model and physics branch ************************
		modelGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		modelGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);

		physicsGroup = new BranchGroup();
		physicsGroup.setCapability(BranchGroup.ALLOW_DETACH);
		physicsGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		physicsGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
		//modelGroup.addChild(physicsGroup); added if toggled on

		visualGroup = new BranchGroup();
		visualGroup.setCapability(BranchGroup.ALLOW_DETACH);
		visualGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		visualGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
		modelGroup.addChild(visualGroup);

		universe.addToVisualBranch(modelGroup);
		behaviourBranch = new BranchGroup();
		behaviourBranch.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		behaviourBranch.setCapability(Group.ALLOW_CHILDREN_WRITE);

		// Create ambient light	and add it ************************
		float ambl = BethRenderSettings.getGlobalAmbLightLevel();
		Color3f alColor = new Color3f(ambl, ambl, ambl);
		ambLight = new AmbientLight(true, alColor);
		//ambLight.setCapability(Light.ALLOW_INFLUENCING_BOUNDS_WRITE);
		ambLight.setInfluencingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
		ambLight.setCapability(Light.ALLOW_COLOR_WRITE);
		float dirl = BethRenderSettings.getGlobalDirLightLevel();
		Color3f dirColor = new Color3f(dirl, dirl, dirl);
		dirLight = new DirectionalLight(true, dirColor, new Vector3f(0f, -1f, 0f));
		//dirLight.setCapability(Light.ALLOW_INFLUENCING_BOUNDS_WRITE);
		dirLight.setInfluencingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
		dirLight.setCapability(Light.ALLOW_COLOR_WRITE);
		BranchGroup lightsBG = new BranchGroup();
		lightsBG.addChild(ambLight);
		lightsBG.addChild(dirLight);
		universe.addToVisualBranch(lightsBG);

		// turn off default gesture handler as drags get lost etc
		gl_window.setDefaultGesturesEnabled(false);

		//mouse/keyboard
		navigationTemporalBehaviour = new NavigationTemporalBehaviour();

		//jbullet
		navigationProcessor = new NavigationProcessorBullet(nbccProvider, avatarLocation);
		navigationTemporalBehaviour.addNavigationProcessor(navigationProcessor);
		behaviourBranch.addChild(navigationTemporalBehaviour);

		//add mouse and keyboard inputs ************************
		keyNavigationInputNewt = new NavigationInputNewtMove();
		keyNavigationInputNewt.setNavigationProcessor(navigationProcessor);
		NavigationInputNewtMove.VERTICAL_RATE = 50f;


		//mouseInputListener = new NavigationInputAWTMouseLocked();
		//mouseInputListener.setNavigationProcessor(navigationProcessor);
		newtMouseInputListener = new NavigationInputNewtLook();
		newtMouseInputListener.setNavigationProcessor(navigationProcessor);
		NavigationInputNewtMove.VERTICAL_RATE = 50f;

		//add jump key and vis/phy toggle key listeners for fun ************************
		jumpKeyListener = new NewtJumpKeyListener(nbccProvider);

		//some hud gear
		fpsCounter = new AndyFPSCounter();
		hudPos = new AndyHUDPosition();
		hudcompass = new AndyHUDCompass();
		hudCrossHair = new HUDCrossHair();

	//	behaviourBranch.addChild(fpsCounter.getBehaviorBranchGroup());

		loadInfo = new HUDText(new Point2f(-0.95f, 0f), 18, "Loading...");
		loadingInfoBehavior = new LoadingInfoBehavior(loadInfo);
		behaviourBranch.addChild(loadingInfoBehavior);

	//	avatarLocation.addAvatarLocationListener(hudPos);
	//	avatarLocation.addAvatarLocationListener(hudcompass);

		universe.addToBehaviorBranch(behaviourBranch);

		// Add a ShaderErrorListener
		universe.addShaderErrorListener(new ShaderErrorListener()
		{
			public void errorOccurred(ShaderError error)
			{
				error.printVerbose();
				//JOptionPane.showMessageDialog(null, error.toString(), "ShaderError", JOptionPane.ERROR_MESSAGE);
			}
		});

		setupGraphicsSetting(gl_window);

		this.cameraPanel.getCanvas3D2D().addNotify();
		this.cameraPanel.startRendering();
	}

	public void startRenderer(GLWindow gl_window)
	{
		if (!cameraPanel.isRendering())
		{
			this.cameraPanel.getCanvas3D2D().setNewGLWindow(gl_window);
			this.cameraPanel.getCanvas3D2D().addNotify();
			this.cameraPanel.startRendering();
		}
	}

	public void stopRenderer()
	{
		if (cameraPanel.isRendering())
		{
			cameraPanel.stopRendering();
		}
	}


	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#closingTime()
	 */
	@Override
	public void closingTime()
	{
		//stopRenderer();
	}

	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#getWindow()
	 */
	@Override
	public GLWindow getWindow()
	{
		return cameraPanel.getCanvas3D2D().getGLWindow();
	}

	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#changeLocation(javax.vecmath.Quat4f, javax.vecmath.Vector3f)
	 */
	@Override
	public void changeLocation(Quat4f rot, Vector3f trans)
	{
		System.out.println("Moving to " + trans);
		//TODO: should I call warp now? not needed if only change cell uses the above
		warp(trans);
		getAvatarLocation().setTranslation(trans);
		getAvatarLocation().setRotation(rot);
	}

	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#warp(javax.vecmath.Vector3f)
	 */
	@Override
	public void warp(Vector3f origin)
	{
		if (physicsSystem != null && physicsSystem.getNBControlledChar() != null)
		{
			physicsSystem.getNBControlledChar().getCharacterController().warp(origin);
		}

	}

	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#setGlobalAmbLightLevel(float)
	 */
	@Override
	public void setGlobalAmbLightLevel(float f)
	{
		Color3f alColor = new Color3f(f, f, f);
		ambLight.setColor(alColor);
	}

	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#setGlobalDirLightLevel(float)
	 */
	@Override
	public void setGlobalDirLightLevel(float f)
	{
		Color3f dirColor = new Color3f(f, f, f);
		dirLight.setColor(dirColor);
	}

	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#configure(utils.source.MeshSource, scrollsexplorer.simpleclient.SimpleBethCellManager)
	 */
	@Override
	public void configure(MeshSource meshSource, SimpleBethCellManager simpleBethCellManager)
	{
		// set up and run the physics system************************************************

		physicsSystem = new PhysicsSystem(simpleBethCellManager, avatarCollisionInfo, behaviourBranch, meshSource);

		IDashboard.dashboard.setPhysicSystem(physicsSystem);

		cameraMouseOver = new ActionableMouseOverHandler(physicsSystem, simpleBethCellManager, true)
		{
			// only allow clicks in top half of screen for interaction
			public void doMouseReleased(MouseEvent e)
			{
				int ex = e.getX();
				int ey = e.getY();

				//  the top half of screen
				if (ey < (cameraPanel.getCanvas3D2D().getGLWindow().getHeight() / 2))
				{
					super.doMouseReleased(e);
				}
			}
		}

		;

		cameraAdminMouseOverHandler = new AdminMouseOverHandler(physicsSystem, true);

	}

	public void setupGraphicsSetting(GLWindow gl_window)
	{

		if (cameraPanel == null)
		{
			// must record start state to restore later
			boolean isLive = enabled;

			if (isLive)
			{
				setEnabled(false);
			}

			//if HMD fails or not HMD
			if (cameraPanel == null)
			{

				if (gl_window == null)
					cameraPanel = new CameraPanel(universe);
				else
					cameraPanel = new CameraPanel(universe, gl_window);

				// and the dolly it rides on
				if (TRAILER_CAM)
				{
					TrailerCamDolly trailerCamDolly = new TrailerCamDolly(avatarCollisionInfo, new WalkTrailorCamCollider());
					cameraPanel.setDolly(trailerCamDolly);
				}
				else
				{
					HeadCamDolly headCamDolly = new HeadCamDolly(avatarCollisionInfo);
					cameraPanel.setDolly(headCamDolly);
				}
			}


			avatarLocation.addAvatarLocationListener(cameraPanel.getDolly());
			cameraPanel.getDolly().locationUpdated(avatarLocation.get(new Quat4f()), avatarLocation.get(new Vector3f()));

			Canvas3D2D canvas3D2D = cameraPanel.getCanvas3D2D();
			//canvas3D2D.getGLWindow().addKeyListener(keyNavigationInputNewt);
			canvas3D2D.getGLWindow().addKeyListener(jumpKeyListener);
			canvas3D2D.getGLWindow().addKeyListener(newtMiscKeyHandler);

			//fpsCounter.addToCanvas(canvas3D2D);
			//hudPos.addToCanvas(canvas3D2D);
			//hudcompass.addToCanvas(canvas3D2D);
			hudCrossHair.addToCanvas(canvas3D2D);
			loadInfo.addToCanvas(canvas3D2D);


			if (isLive)
			{
				setEnabled(true);
			}
		}
	}



	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#resetGraphicsSetting()
	 */
	@Override
	public void resetGraphicsSetting()
	{
	}

	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enable)
	{
		if (enable != enabled)
		{
			System.out.println("Setting Enabled " + enable);
			// start the processor up ************************
			navigationProcessor.setActive(enable);
			if (enable)
			{
				cameraMouseOver.setConfig(cameraPanel.getCanvas3D2D());
				//cameraAdminMouseOverHandler.setConfig(cameraPanel.getCanvas3D2D());
				physicsSystem.unpause();
				loadInfo.removeFromCanvas();
				loadingInfoBehavior.setEnable(false);
			}
			else
			{
				cameraMouseOver.setConfig(null);
				//cameraAdminMouseOverHandler.setConfig(null);
				physicsSystem.pause();
				loadInfo.addToCanvas(cameraPanel.getCanvas3D2D());
				loadingInfoBehavior.setEnable(true);
			}
			enabled = enable;
		}

	}


	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#setFreeFly(boolean)
	 */
	@Override
	public void setFreeFly(boolean ff)
	{
		if (physicsSystem.getNBControlledChar() != null)
		{
			physicsSystem.getNBControlledChar().getCharacterController().setFreeFly(ff);
		}
		keyNavigationInputNewt.setAllowVerticalMovement(ff);

	}

	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#getPhysicsSystem()
	 */
	@Override
	public PhysicsSystem getPhysicsSystem()
	{
		return physicsSystem;
	}

	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#getVisualBranch()
	 */
	@Override
	public BranchGroup getVisualBranch()
	{
		return visualGroup;
	}

	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#getPhysicalBranch()
	 */
	@Override
	public BranchGroup getPhysicalBranch()
	{
		return physicsGroup;
	}

	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#toggleHavok()
	 */
	@Override
	public void toggleHavok()
	{
		showHavok = !showHavok;
		if (showHavok && physicsGroup.getParent() == null)
		{
			modelGroup.addChild(physicsGroup);
		}
		else if (!showHavok && physicsGroup.getParent() != null)
		{
			physicsGroup.detach();
		}
	}

	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#toggleVisual()
	 */
	@Override
	public void toggleVisual()
	{
		showVisual = !showVisual;
		if (showVisual && visualGroup.getParent() == null)
		{
			//Bad no good
			//structureUpdateBehavior.add(modelGroup, visualGroup);
			modelGroup.addChild(visualGroup);
		}
		else if (!showVisual && visualGroup.getParent() != null)
		{
			//structureUpdateBehavior.remove(modelGroup, visualGroup);
			visualGroup.detach();
		}
	}

	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#setVisualDisplayed(boolean)
	 */
	@Override
	public void setVisualDisplayed(boolean newShowVisual)
	{
		if (newShowVisual && visualGroup.getParent() == null)
		{
			modelGroup.addChild(visualGroup);

		}
		else if (!newShowVisual && visualGroup.getParent() != null)
		{
			visualGroup.detach();
		}

		showVisual = newShowVisual;
	}

	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#getAvatarLocation()
	 */
	@Override
	public AvatarLocation getAvatarLocation()
	{
		return avatarLocation;
	}

	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#setPhysicsEnabled(boolean)
	 */
	@Override
	public void setPhysicsEnabled(boolean enable)
	{
		physicsSystem.getPhysicsLocaleDynamics().setSkipStepSim(!enable);
	}

	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#getAvatarCollisionInfo()
	 */
	@Override
	public AvatarCollisionInfo getAvatarCollisionInfo()
	{
		return avatarCollisionInfo;
	}

	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#getViewingPlatform()
	 */
	@Override
	public ViewingPlatform getViewingPlatform()
	{
		// this won't work for the HMD version for now, as it it 2 platforms
		return (ViewingPlatform) cameraPanel.getDolly();
	}

	@Override
	public void setAzerty(boolean b)
	{
		//Nothing!
	}

	/* (non-Javadoc)
	 * @see scrollsexplorer.simpleclient.SimpleWalkSetupInterface#setMouseLock(boolean)
	 */
	@Override
	public void setMouseLock(boolean mouseLock)
	{
		if (!mouseLock)
		{
			keyNavigationInputNewt.setWindow(null);
			newtMouseInputListener.setWindow(null);
		}
		else
		{
			newtMouseInputListener.setWindow(cameraPanel.getCanvas3D2D().getGLWindow());
			keyNavigationInputNewt.setWindow(cameraPanel.getCanvas3D2D().getGLWindow());
		}
	}

	@Override
	public boolean isTrailorCam()
	{
		return TRAILER_CAM;
	}

	private class NewtMiscKeyHandler implements KeyListener
	{
		public NewtMiscKeyHandler()
		{
		}

		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_H)
			{
				toggleHavok();
			}
			else if (e.getKeyCode() == KeyEvent.VK_L)
			{
				toggleVisual();
			}
			else if (e.getKeyCode() == KeyEvent.VK_F)
			{
				freefly = !freefly;
				setFreeFly(freefly);
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0)
		{

		}
	}
	private class WalkTrailorCamCollider implements TrailerCamDolly.TrailorCamCollider
	{
		private Vector3f rayFrom = new Vector3f();

		private Vector3f rayTo = new Vector3f();

		@Override
		public float getCollisionFraction(Point3d lookAt, Vector3d cameraVector)
		{
			rayFrom.set(lookAt);
			//CAREFUL!!! 3d and 3f conversion requires non-trivial container usage!!!only the set method takes a 3d,
			//the add doesn't, so rayFrom is being used as a temp holder
			rayTo.set(cameraVector);
			rayTo.add(rayFrom, rayTo);

			if (physicsSystem != null)
			{
				CollisionWorld.ClosestRayResultCallback crrc = physicsSystem.findRayIntersect(rayFrom, rayTo, -1);
				if (crrc != null)
				{
					return crrc.closestHitFraction;
				}
			}

			return 1f;
		}
	}
}

