package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.os.Bundle;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.ingenieur.andyelderscrolls.R;
import com.ingenieur.andyelderscrolls.andyesexplorer.ui.GLWindowOverLay;
import com.ingenieur.andyelderscrolls.andyesexplorer.ui.LookNavigationView;
import com.ingenieur.andyelderscrolls.andyesexplorer.ui.MoveNavigationView;
import com.jogamp.nativewindow.NativeWindowException;
import com.jogamp.newt.Window;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;

import jogamp.newt.WindowImpl;
import jogamp.newt.driver.android.NewtBaseFragment;
import jogamp.newt.driver.android.WindowDriver;


public class AndyESExplorerFragment extends NewtBaseFragment {
    private GLWindow gl_window;
    private ScrollsExplorer scrollsExplorer;
    private boolean scrollsExplorerInitCalled = false;

    private GLWindowOverLay moveNavigationPanel;
    private GLWindowOverLay lookNavigationPanel;
    private GLWindowOverLay characterSheetOverlay;
    private GLWindowOverLay inventoryOverlay;
    private GLWindowOverLay mapOverlay;


    @Override
    public void onCreate(final Bundle state) {
        super.onCreate(state);
        createGLWindow();
    }

    private void createGLWindow() {
        final GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GLES2));
        caps.setDoubleBuffered(true);
        caps.setDepthBits(16);
        caps.setStencilBits(8);
        caps.setHardwareAccelerated(true);
        caps.setBackgroundOpaque(true);
        if (AndyESExplorerActivity.antialias) {
            caps.setSampleBuffers(true);//TODO: I wrote death! no touch! but it seems fine?
            caps.setNumSamples(2);
        }

        gl_window = GLWindow.create(caps);
        //gl_window.setFullscreen(true);

        AndyESExplorerActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "GLWindow.create(caps)", null);

        final Window delegateWindow = gl_window.getDelegatedWindow();
        if (delegateWindow instanceof WindowDriver) {
            WindowDriver wd = (WindowDriver) delegateWindow;

            wd.setNativeWindowExceptionListener(new WindowImpl.NativeWindowExceptionListener() {
                // return true to indicate success, false will throw the exception
                public boolean handleException(NativeWindowException nwp) {
                    AndyESExplorerActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "NativeWindowException", null);
                    String message = "insufficient3dResourcesMessage";
                    String title = "insufficient3dResourcesTitle";
                    //JOptionPane.showMessageDialog(getActivity(), message, title, JOptionPane.ERROR_MESSAGE);
                    AndyESExplorerActivity act = (AndyESExplorerActivity) AndyESExplorerFragment.this.getActivity();
                    Looper.prepare();
                    Toast.makeText(act, message, Toast.LENGTH_LONG);
                    return true;
                }

                // return true to indicate success, false will throw the exception
                public boolean handleRuntimeException(RuntimeException re) {
                    AndyESExplorerActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "NativeWindowException", null);
                    String message = "insufficient3dResourcesMessage";
                    String title = "insufficient3dResourcesTitle";
                    //JOptionPane.showMessageDialog(getActivity(), message, title, JOptionPane.ERROR_MESSAGE);
                    AndyESExplorerActivity act = (AndyESExplorerActivity) AndyESExplorerFragment.this.getActivity();
                    Looper.prepare();
                    Toast.makeText(act, message, Toast.LENGTH_LONG);
                    return true;
                }
            });
        }

        gl_window.addGLEventListener(glWindowInitListener);

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
                // this is called on a resume as well, so only init once
                if (!scrollsExplorerInitCalled) {
                    scrollsExplorerInitCalled = true;
                    final AndyESExplorerActivity activity = (AndyESExplorerActivity) AndyESExplorerFragment.this.getActivity();

                    scrollsExplorer = new ScrollsExplorer(activity, gl_window, activity.gameName, activity.gameConfigId, AndyESExplorerFragment.this);
                    activity.scrollsExplorer = scrollsExplorer;

                    moveNavigationPanel = new MoveNavigationView(getContext(), getView(), scrollsExplorer.simpleWalkSetup.getNavigationProcessor());
                    lookNavigationPanel = new LookNavigationView(getContext(), getView(), scrollsExplorer.simpleWalkSetup.getNavigationProcessor());
                    lookNavigationPanel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(scrollsExplorer.simpleWalkSetup != null && scrollsExplorer.simpleWalkSetup.getCameraMouseOver() != null)
                                scrollsExplorer.simpleWalkSetup.getCameraMouseOver().doClick();
                        }
                    });

                    // notice the top right is always offset by the width of the overlay so as to no go offscreen (it's a bit odd)
                    characterSheetOverlay = new GLWindowOverLay(getContext(), getView(), R.layout.charactersheetoverlay, Gravity.RIGHT | Gravity.TOP, true, 50, 0);
                    characterSheetOverlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activity.mViewPager.setCurrentItem(0, true);
                        }
                    });
                    inventoryOverlay = new GLWindowOverLay(getContext(), getView(), R.layout.inventoryoverlay, Gravity.RIGHT | Gravity.TOP, true, 100, 0);
                    inventoryOverlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activity.mViewPager.setCurrentItem(3, true);
                        }
                    });
                    mapOverlay = new GLWindowOverLay(getContext(), getView(), R.layout.mapoverlay, Gravity.RIGHT | Gravity.TOP, true, 150, 0);
                    mapOverlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activity.mViewPager.setCurrentItem(2, true);
                        }
                    });
                    // showing the nav panel can only be done on the UI thread
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            moveNavigationPanel.showTooltip();
                            lookNavigationPanel.showTooltip();
                            characterSheetOverlay.showTooltip();
                            inventoryOverlay.showTooltip();
                            mapOverlay.showTooltip();
                        }
                    });
                } else {
                    // possibly hasn't been created yet
                    if (scrollsExplorer != null) {
                        // this is from a resume (start renderer calls addNotify)
                        scrollsExplorer.startRenderer(gl_window);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void dispose(final GLAutoDrawable drawable) {
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.setHasOptionsMenu(true);

        View rootView = getContentView(this.getWindow(), gl_window);
        getActivity().getActionBar().hide();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        AndyESExplorerActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onStart", null);
    }

    @Override
    public void onPause() {
        if (scrollsExplorer != null) {
            scrollsExplorer.closingTime();
            // note stop renderer also calls removenotify
            scrollsExplorer.stopRenderer();
        }

        super.onPause();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        gl_window.destroy();
        gl_window = null;
        super.onDestroy();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (scrollsExplorer != null) {
            if (isVisibleToUser) {
                getActivity().getActionBar().hide();
                // this stops the 3d view when changin to other pages, but it doesn't look cool
                //	scrollsExplorer.startRenderer(gl_window);
            } else {
                // action bar is a pain
                //getActivity().getActionBar().show();
                //	scrollsExplorer.stopRenderer();
            }

            if (isVisibleToUser) {
                moveNavigationPanel.showTooltip();
                lookNavigationPanel.showTooltip();
                characterSheetOverlay.showTooltip();
                inventoryOverlay.showTooltip();
                mapOverlay.showTooltip();
            } else {
                moveNavigationPanel.hideTooltip();
                lookNavigationPanel.hideTooltip();
                characterSheetOverlay.hideTooltip();
                inventoryOverlay.hideTooltip();
                mapOverlay.hideTooltip();
            }
        }

        super.setUserVisibleHint(isVisibleToUser);
    }

    public GLWindowOverLay getCharacterSheetOverlay() {
        return characterSheetOverlay;
    }
    public GLWindowOverLay getInventoryOverlay() {
        return inventoryOverlay;
    }
    public GLWindowOverLay getMapOverlay() {
        return mapOverlay;
    }
}
