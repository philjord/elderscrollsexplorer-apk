package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.os.Bundle;
import android.os.Handler;
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

/**
 * This class owns the 4 views, but is in fact itself the main walk view, but it does engotiation
 * with it's activity parent to swpa to the other 3 views and back
 */
public class AndyESExplorerFragment extends NewtBaseFragment {
    private GLWindow gl_window;
    private ScrollsExplorer scrollsExplorer;
    private boolean scrollsExplorerInitCalled = false;

    private MoveNavigationView moveNavigationPanel;
    private LookNavigationView lookNavigationPanel;
    private GLWindowOverLay characterSheetOverlay;
    private GLWindowOverLay inventoryOverlay;
    private GLWindowOverLay mapOverlay;

    // used to keep track on what the simple walk is set to, so wehn other screens ar shown
    // it can swithc back correctly
    private boolean simpleWalkMouseLockState = false;

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

                    //UI elements must be created on the lopper so they can be modified later by using the looper
                    // right now I'm on "Selected Game Config Loader" thread that ends shortly
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            moveNavigationPanel = new MoveNavigationView(getContext(), getView(), scrollsExplorer.simpleWalkSetup.getNavigationProcessor());
                            lookNavigationPanel = new LookNavigationView(getContext(), getView(), scrollsExplorer.simpleWalkSetup.getNavigationProcessor());
                            scrollsExplorer.simpleWalkSetup.addMouseLockListener(new AndySimpleWalkSetup.MouseLockListener() {
                                @Override
                                public void mouseLockSet(boolean set) {
                                    // visually remove our 2 circles when mouse lock set
                                    moveNavigationPanel.setVisible(!set);
                                    lookNavigationPanel.setVisible(!set);
                                }
                            });

                            //TODO the mouse locked click is no going, here is the unlocked clicker
                            lookNavigationPanel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (scrollsExplorer.simpleWalkSetup != null && scrollsExplorer.simpleWalkSetup.getCameraMouseOver() != null)
                                        scrollsExplorer.simpleWalkSetup.getCameraMouseOver().doClick();
                                }
                            });


                            // notice the top right is always offset by the width of the overlay so as to no go offscreen (it's a bit odd)
                            characterSheetOverlay = new GLWindowOverLay(getContext(), getView(), R.layout.charactersheetoverlay, Gravity.RIGHT | Gravity.TOP, true, 50, 0);
                            characterSheetOverlay.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showCharacterSheet();
                                }
                            });
                            inventoryOverlay = new GLWindowOverLay(getContext(), getView(), R.layout.inventoryoverlay, Gravity.RIGHT | Gravity.TOP, true, 100, 0);
                            inventoryOverlay.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showInventory();
                                }
                            });
                            mapOverlay = new GLWindowOverLay(getContext(), getView(), R.layout.mapoverlay, Gravity.RIGHT | Gravity.TOP, true, 150, 0);
                            mapOverlay.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showMap();
                                }
                            });

                            //make these UI items visible on top of the gl window
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
        // does this help gc at all?
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

    /**
     * Make sure you un mouse lock before switching pages
     */
    public void showCharacterSheet() {
        AndyESExplorerActivity activity = (AndyESExplorerActivity) this.getActivity();
        int currentItem = activity.mViewPager.getCurrentItem();

        if (currentItem == 1) {
            // remember mouse lock state state for when we untoggle back to it
            simpleWalkMouseLockState = scrollsExplorer.simpleWalkSetup.isMouseLock();
        }

        activity.runOnUiThread(new Runnable() {
            public void run() {
                activity.mViewPager.setCurrentItem(0, true);
            }
        });
    }

    /**
     * If CharacterSheet is showing flip back to the main screen
     */
    public void toggleCharacterSheet() {
        AndyESExplorerActivity activity = (AndyESExplorerActivity) this.getActivity();
        int currentItem = activity.mViewPager.getCurrentItem();

        if (currentItem == 1) {
            // remember mouse lock state state for when we untoggle back to it
            simpleWalkMouseLockState = scrollsExplorer.simpleWalkSetup.isMouseLock();
        }

        // if we are map go back to walk
        if (currentItem == 0) {
            showSimpleWalk();
        } else {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    scrollsExplorer.simpleWalkSetup.setMouseLock(false);
                    activity.mViewPager.setCurrentItem(0, true);
                }
            });
        }
    }

    /**
     * Make sure you un mouse lock before switching pages
     */
    public void showInventory() {
        AndyESExplorerActivity activity = (AndyESExplorerActivity) this.getActivity();
        int currentItem = activity.mViewPager.getCurrentItem();

        if (currentItem == 1) {
            // remember mouse lock state state for when we untoggle back to it
            simpleWalkMouseLockState = scrollsExplorer.simpleWalkSetup.isMouseLock();
        }

        activity.runOnUiThread(new Runnable() {
            public void run() {
                scrollsExplorer.simpleWalkSetup.setMouseLock(false);
                activity.mViewPager.setCurrentItem(3, true);
            }
        });
    }

    /**
     * If Inventory is showing flip back to the main screen
     */
    public void toggleInventory() {
        AndyESExplorerActivity activity = (AndyESExplorerActivity) this.getActivity();
        int currentItem = activity.mViewPager.getCurrentItem();

        if (currentItem == 1) {
            // remember mouse lock state state for when we untoggle back to it
            simpleWalkMouseLockState = scrollsExplorer.simpleWalkSetup.isMouseLock();
        }

        // if we are map go back to walk
        if (currentItem == 3) {
            showSimpleWalk();
        } else {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    scrollsExplorer.simpleWalkSetup.setMouseLock(false);
                    activity.mViewPager.setCurrentItem(3, true);
                }
            });
        }
    }

    /**
     * Make sure you un mouse lock before switching pages
     */
    public void showMap() {
        AndyESExplorerActivity activity = (AndyESExplorerActivity) this.getActivity();
        int currentItem = activity.mViewPager.getCurrentItem();

        if (currentItem == 1) {
            // remember mouse lock state state for when we untoggle back to it
            simpleWalkMouseLockState = scrollsExplorer.simpleWalkSetup.isMouseLock();
        }
        activity.runOnUiThread(new Runnable() {
            public void run() {
                scrollsExplorer.simpleWalkSetup.setMouseLock(false);
                activity.mViewPager.setCurrentItem(2, true);
            }
        });
    }

    /**
     * If map is showing flip back to the main screen
     */
    public void toggleMap() {
        AndyESExplorerActivity activity = (AndyESExplorerActivity) this.getActivity();
        int currentItem = activity.mViewPager.getCurrentItem();

        if (currentItem == 1) {
            // remember mouse lock state state for when we untoggle back to it
            simpleWalkMouseLockState = scrollsExplorer.simpleWalkSetup.isMouseLock();
        }

        // if we are map go back to walk
        if (currentItem == 2) {
            showSimpleWalk();
        } else {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    scrollsExplorer.simpleWalkSetup.setMouseLock(false);
                    activity.mViewPager.setCurrentItem(2, true);
                }
            });
        }
    }

    public void showSimpleWalk() {
        AndyESExplorerActivity activity = (AndyESExplorerActivity) this.getActivity();
        activity.runOnUiThread(new Runnable() {
            public void run() {
                activity.mViewPager.setCurrentItem(1, true);
                scrollsExplorer.simpleWalkSetup.setMouseLock(simpleWalkMouseLockState);
            }
        });
    }
}
