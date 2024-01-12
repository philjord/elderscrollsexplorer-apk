package com.ingenieur.andyelderscrolls.display.nifdisplay;

import android.os.Bundle;

import com.ingenieur.andyelderscrolls.display.DisplayActivity;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

public class NifDisplayActivity extends DisplayActivity {
    private NifDisplayTester nifDisplay;

    @Override
    public void onCreate(final Bundle state) {
        super.onCreate(state);

        gl_window.addGLEventListener(new GLEventListener() {
                                         @Override
                                         public void init(@SuppressWarnings("unused") final GLAutoDrawable drawable) {
                                             try {
                                                 //NOTE Canvas3D requires a fully initialized glWindow (in the android setup) so we must call
                                                 //KfDisplayTester from this init function
                                                 nifDisplay = new NifDisplayTester(NifDisplayActivity.this, gl_window, gameConfigToLoad.scrollsFolder);
                                                 canvas3D2D = nifDisplay.canvas3D2D;
                                                 // addNotify will start up the renderer and kick things off
                                                 nifDisplay.canvas3D2D.addNotify();
                                             } catch (Exception e) {
                                                 e.printStackTrace();
                                             }
                                         }

                                         @Override
                                         public void reshape(final GLAutoDrawable drawable, final int x, final int y,
                                                             final int w, final int h) {
                                         }

                                         @Override
                                         public void display(final GLAutoDrawable drawable) {
                                         }

                                         @Override
                                         public void dispose(final GLAutoDrawable drawable) {
                                         }
                                     }

        );
    }
}
