package com.ingenieur.andyelderscrolls.display;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ingenieur.andyelderscrolls.ElderScrollsActivity;
import com.jogamp.newt.event.MonitorEvent;
import com.jogamp.newt.event.MonitorModeListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;

import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;

import jogamp.newt.driver.android.NewtBaseFragmentActivity;
import scrollsexplorer.GameConfig;
import tools3d.mixed3d2d.Canvas3D2D;


public class DisplayActivity extends NewtBaseFragmentActivity {
    protected Canvas3D2D canvas3D2D;
    protected GLWindow gl_window;
    protected GameConfig gameConfigToLoad;

    @Override
    public void onCreate(final Bundle state) {
        System.setProperty("j3d.cacheAutoComputeBounds", "true");
        System.setProperty("j3d.defaultReadCapability", "false");
        System.setProperty("j3d.defaultNodePickable", "false");
        System.setProperty("j3d.defaultNodeCollidable", "false");

        SimpleShaderAppearance.setVersionES300();

        super.onCreate(state);

        Intent intent = getIntent();
        String gameName = intent.getStringExtra(ElderScrollsActivity.SELECTED_GAME);
        gameConfigToLoad = ElderScrollsActivity.getGameConfig(gameName);

        final GLCapabilities caps =
                new GLCapabilities(GLProfile.get(GLProfile.GLES2));
        caps.setDoubleBuffered(true);
        caps.setDepthBits(16);
        caps.setStencilBits(8);
        caps.setHardwareAccelerated(true);
        //caps.setSampleBuffers(true);death no touch!
        //caps.setNumSamples(2);

        gl_window = GLWindow.create(caps);
        gl_window.setFullscreen(true);

        this.setContentView(this.getWindow(), gl_window);


        gl_window.getScreen().addMonitorModeListener(new MonitorModeListener() {
                                                         @Override
                                                         public void monitorModeChangeNotify(MonitorEvent monitorEvent) {
                                                         }

                                                         @Override
                                                         public void monitorModeChanged(MonitorEvent monitorEvent, boolean b) {
                                                             Log.e("System.err", "monitorModeChanged: " + monitorEvent);
                                                         }
                                                     }

        );
        gl_window.setVisible(true);
    }

    @Override
    public void onPause() {
        if (canvas3D2D != null) {
            canvas3D2D.stopRenderer();
            canvas3D2D.removeNotify();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        if (canvas3D2D != null) {
            canvas3D2D.addNotify();
            canvas3D2D.startRenderer();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        gl_window.destroy();
        gl_window = null;

        super.onDestroy();
    }
}
