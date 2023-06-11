package simpleandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;

import com.ingenieur.andyelderscrolls.andyesexplorer.AndyESExplorerActivity;
import com.ingenieur.andyelderscrolls.andyesexplorer.AndyESExplorerFragment;
import com.ingenieur.andyelderscrolls.andyesexplorer.ScrollsExplorer;
import com.ingenieur.andyelderscrolls.utils.AndyFPSCounter;
import com.ingenieur.andyelderscrolls.utils.SopInterceptor;
import com.jogamp.common.GlueGenVersion;
import com.jogamp.common.util.VersionUtil;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.JoglVersion;

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.RotationInterpolator;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.shader.Cube;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Point3d;

import java.io.PrintStream;

import javaawt.GraphicsConfiguration;
import jogamp.common.os.PlatformPropsImpl;
import jogamp.newt.driver.android.NewtBaseActivity;
import jogamp.newt.driver.android.WindowDriver;
import tools3d.camera.CameraPanel;
import tools3d.mixed3d2d.Canvas3D2D;
import tools3d.universe.VisualPhysicalUniverse;

public class JoglHelloWorldActivity extends NewtBaseActivity {
    public JoglHelloWorldActivity() {
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        //make sure the System.out calls get onto the log
        PrintStream interceptor = new SopInterceptor(System.out, "sysout");
        System.setOut(interceptor);
        PrintStream interceptor2 = new SopInterceptor(System.err, "syserr");
        System.setErr(interceptor2);

        super.onCreate(savedInstanceState);

        setFullscreenFeature(getWindow(), true);

        final android.view.ViewGroup viewGroup = new android.widget.FrameLayout(getActivity().getApplicationContext());
        getWindow().setContentView(viewGroup);

        //final TextView tv = new TextView(getActivity());
        //final ScrollView scroller = new ScrollView(getActivity());
        //scroller.addView(tv);
        // viewGroup.addView(scroller, new android.widget.FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));

        //final String info1 = "JOGL Version Info" + PlatformPropsImpl.NEWLINE + VersionUtil.getPlatformInfo() + PlatformPropsImpl.NEWLINE + GlueGenVersion.getInstance() + PlatformPropsImpl.NEWLINE + JoglVersion.getInstance() + PlatformPropsImpl.NEWLINE;
        // tv.setText(info1);

        final GLProfile glp;
        if (GLProfile.isAvailable(GLProfile.GL2ES2)) {
            glp = GLProfile.get(GLProfile.GL2ES2);
        } else if (GLProfile.isAvailable(GLProfile.GL2ES1)) {
            glp = GLProfile.get(GLProfile.GL2ES1);
        } else {
            glp = null;
            // tv.append("No GLProfile GL2ES2 nor GL2ES1 available!");
        }
        if (null != glp) {
            // create GLWindow (-> incl. underlying NEWT Display, Screen & Window)
            final GLCapabilities caps = new GLCapabilities(glp);
            final GLWindow glWindow = GLWindow.create(caps);
            glWindow.setUndecorated(true);
            glWindow.setSize(32, 32);
            glWindow.setPosition(0, 0);
            final android.view.View androidGLView = ((WindowDriver) glWindow.getDelegatedWindow()).getAndroidView();
            viewGroup.addView(androidGLView, new android.widget.FrameLayout.LayoutParams(glWindow.getSurfaceWidth(), glWindow.getSurfaceHeight(), Gravity.BOTTOM | Gravity.RIGHT));

            // Create Canvas3D and SimpleUniverse; add canvas to drawing panel
            c = createUniverse(glWindow);

            // Create the content branch and add it to the universe
            scene = createSceneGraph();
            univ.addBranchGraph(scene);
            univ.getViewingPlatform().setNominalViewingTransform();
            glWindow.addGLEventListener(glWindowInitListener);

        }
    }

    GLEventListener glWindowInitListener = new GLEventListener() {
        @Override
        public void init(@SuppressWarnings("unused") final GLAutoDrawable drawable) {
        }

        @Override
        public void reshape(final GLAutoDrawable drawable, final int x, final int y,
                            final int w, final int h) {
        }

        @Override
        public void display(final GLAutoDrawable drawable) {
            try {

                c.addNotify();
                c.startRenderer();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void dispose(final GLAutoDrawable drawable) {
        }
    };


    private SimpleUniverse univ = null;
    private BranchGroup scene = null;
    private Canvas3D c = null;
    public BranchGroup createSceneGraph()
    {
        // Create the root of the branch graph
        BranchGroup objRoot = new BranchGroup();

        // Create the TransformGroup node and initialize it to the
        // identity. Enable the TRANSFORM_WRITE capability so that
        // our behavior code can modify it at run time. Add it to
        // the root of the subgraph.
        TransformGroup objTrans = new TransformGroup();
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objRoot.addChild(objTrans);

        // Create a simple Shape3D node; add it to the scene graph.
        objTrans.addChild(new Cube(0.4));

        // Create a new Behavior object that will perform the
        // desired operation on the specified transform and add
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

    private Canvas3D createUniverse(GLWindow glWindow)
    {
        // Create a Canvas3D using the preferred configuration
        Canvas3D c = new Canvas3D(glWindow);

        // Create simple universe with view branch
        univ = new SimpleUniverse(c);

        // This will move the ViewPlatform back a bit so the
        // objects in the scene can be viewed.
        univ.getViewingPlatform().setNominalViewingTransform();

        // Ensure at least 5 msec per frame (i.e., < 200Hz)
        univ.getViewer().getView().setMinimumFrameCycleTime(5);

        //univ.getViewer().getView().addCanvas3D(c);



        return c;
    }


}


