package com.ingenieur.andyelderscrolls.andyesexplorer.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.bulletphysics.dynamics.character.KinematicCharacterController;
import com.ingenieur.andyelderscrolls.R;
import com.ingenieur.andyelderscrolls.andyesexplorer.AndySimpleWalkSetup;

import nifbullet.NavigationProcessorBullet;
import tools3d.navigation.NavigationProcessorInterface;
import tools3d.navigation.twocircles.NavigationInputNewtMove;

public class MoveNavigationView extends GLWindowOverLay {


    public final static float FORWARD_RATE = 8.0f;

    public static float FAST_FORWARD_RATE = 100.0f;

    public final static float BACKWARD_RATE = 3.5f;

    public final static float STRAFF_RATE = 3.0f;

    public static float VERTICAL_RATE = 10.0f; //not final to allow faster flight

    private boolean allowVerticalMovement = false;

    private AndySimpleWalkSetup simpleWalkSetup;

    public static int FORWARD_KEY = 0;

    public static int FAST_KEY = 1;

    public static int BACK_KEY = 2;

    public static int LEFT_KEY = 3;

    public static int RIGHT_KEY = 4;

    public static int UP_KEY = 5;

    public static int DOWN_KEY = 6;

    // My extra key pushing bits
    private boolean walkHeldDown = false;

    private boolean runHeldDown = false;

    private boolean backHeldDown = false;

    private boolean strafLeftHeldDown = false;

    private boolean strafRightHeldDown = false;

    private boolean upHeldDown = false;

    private boolean downHeldDown = false;

    public MoveNavigationView(Context context, View parent, AndySimpleWalkSetup simpleWalkSetup) {
        super(context, parent, R.layout.navigationpanelmovepopup, Gravity.LEFT | Gravity.BOTTOM);
        this.simpleWalkSetup = simpleWalkSetup;

        NavigationInputNewtMove.VERTICAL_RATE = 50f;// allow jumping
        new MoveNavigationButton(FORWARD_KEY, getButton(R.id.navPanelForwardButton));
        new MoveNavigationButton(FAST_KEY, getButton(R.id.navPanelFastButton));
        new MoveNavigationButton(BACK_KEY, getButton(R.id.navPanelBackButton));
        new MoveNavigationButton(LEFT_KEY, getButton(R.id.navPanelLeftButton));
        new MoveNavigationButton(RIGHT_KEY, getButton(R.id.navPanelRightButton));
        new MoveNavigationButton(UP_KEY, getButton(R.id.navPanelUpButton));
        new MoveNavigationButton(DOWN_KEY, getButton(R.id.navPanelDownButton));
    }

    public void setVisible(boolean vis) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                int newVis = vis ? View.VISIBLE : View.GONE;
                getButton(R.id.navPanelForwardButton).setVisibility(newVis);
                getButton(R.id.navPanelBackButton).setVisibility(newVis);
                getButton(R.id.navPanelLeftButton).setVisibility(newVis);
                getButton(R.id.navPanelRightButton).setVisibility(newVis);
                getButton(R.id.navPanelUpButton).setVisibility(newVis);
                getButton(R.id.navPanelDownButton).setVisibility(newVis);
            }
        });
    }

    private void keyPressed(int keyCode) {
        //TODO: now on Newt the below may be incorrect
        // NOTE keyPressed will occur multiple times if a key is
        // held down by the auto repeat system.
        // hence this crap below
        if (keyCode == FAST_KEY && !runHeldDown) {
            runHeldDown = true;
            setTranslationChange();
        } else if (keyCode == FORWARD_KEY && !walkHeldDown) {
            walkHeldDown = true;
            setTranslationChange();
        } else if (keyCode == BACK_KEY && !backHeldDown) {
            backHeldDown = true;
            setTranslationChange();
        } else if (keyCode == LEFT_KEY && !strafLeftHeldDown) {
            strafLeftHeldDown = true;
            setTranslationChange();
        } else if (keyCode == RIGHT_KEY && !strafRightHeldDown) {
            strafRightHeldDown = true;
            setTranslationChange();
        } else if (isAllowVerticalMovement() && keyCode == UP_KEY && !upHeldDown) {
            upHeldDown = true;
            setTranslationChange();
        } else if (isAllowVerticalMovement() && keyCode == DOWN_KEY && !downHeldDown) {
            downHeldDown = true;
            setTranslationChange();
        }
    }

    public void keyReleased(int keyCode) {
        if (keyCode == FAST_KEY) {
            runHeldDown = false;
            setTranslationChange();
        } else if (keyCode == FORWARD_KEY) {
            walkHeldDown = false;
            setTranslationChange();
        } else if (keyCode == BACK_KEY) {
            backHeldDown = false;
            setTranslationChange();
        } else if (keyCode == LEFT_KEY) {
            strafLeftHeldDown = false;
            setTranslationChange();
        } else if (keyCode == RIGHT_KEY) {
            strafRightHeldDown = false;
            setTranslationChange();
        }

        if (isAllowVerticalMovement()) {
            if (keyCode == UP_KEY) {
                upHeldDown = false;
                setTranslationChange();
            } else if (keyCode == DOWN_KEY) {
                downHeldDown = false;
                setTranslationChange();
            }
        } else {
            if (keyCode == UP_KEY) {
                if (simpleWalkSetup != null && simpleWalkSetup.getPhysicsSystem().getNBControlledChar() != null) {
                    KinematicCharacterController kcc = simpleWalkSetup.getPhysicsSystem().getNBControlledChar().getCharacterController();
                    if (kcc.canJump()) {
                        kcc.jump(1);
                    }
                }
            }
        }

    }

    private void setTranslationChange() {
        if (runHeldDown) {
            // slowly increase the speed
            Thread upT = new Thread() {
                @Override
                public void run() {
                    FAST_FORWARD_RATE = 0f;
                    for (int i = 12; i < 20; i++) {
                        if (runHeldDown) {// skip out when key released
                            FAST_FORWARD_RATE += i;
                            simpleWalkSetup.getNavigationProcessor().setZChange(FAST_FORWARD_RATE);
                        } else
                            break;

                        try {
                            Thread.sleep(175);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            };
            upT.start();
        } else if (backHeldDown) {
            simpleWalkSetup.getNavigationProcessor().setZChange(-BACKWARD_RATE);
        } else if (walkHeldDown) {
            simpleWalkSetup.getNavigationProcessor().setZChange(FORWARD_RATE);
        } else {
            simpleWalkSetup.getNavigationProcessor().setZChange(0);
        }

        if (strafLeftHeldDown && !strafRightHeldDown) {
            simpleWalkSetup.getNavigationProcessor().setXChange(-STRAFF_RATE);
        } else if (strafRightHeldDown && !strafLeftHeldDown) {
            simpleWalkSetup.getNavigationProcessor().setXChange(STRAFF_RATE);
        } else {
            simpleWalkSetup.getNavigationProcessor().setXChange(0);
        }


        if (isAllowVerticalMovement()) {
            if (upHeldDown && !downHeldDown) {
                // slowly increase the rise speed
                Thread upT = new Thread() {
                    @Override
                    public void run() {
                        VERTICAL_RATE = 0f;
                        for (int i = 5; i < 20; i++) {
                            if (upHeldDown) {// skip out when key released
                                VERTICAL_RATE += i;
                                simpleWalkSetup.getNavigationProcessor().setYChange(VERTICAL_RATE);
                            } else
                                break;

                            try {
                                Thread.sleep(60);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                };
                upT.start();
            } else if (downHeldDown && !upHeldDown) {// slowly increase the lower speed
                Thread upT = new Thread() {
                    @Override
                    public void run() {
                        VERTICAL_RATE = 0f;
                        for (int i = 5; i < 20; i++) {
                            if (downHeldDown) {// skip out when key released
                                VERTICAL_RATE += i;
                                simpleWalkSetup.getNavigationProcessor().setYChange(-VERTICAL_RATE);
                            } else
                                break;

                            try {
                                Thread.sleep(60);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                };
                upT.start();
            } else {
                simpleWalkSetup.getNavigationProcessor().setYChange(0);
            }
        } else {
            //jumping see NewtJumpKeyListener bit more complex
            if (upHeldDown && !downHeldDown) {

            } else if (downHeldDown && !upHeldDown) {

            }
        }

    }

    public boolean isAllowVerticalMovement() {
        return allowVerticalMovement;
    }

    public void setAllowVerticalMovement(boolean allowVerticalMovement) {
        this.allowVerticalMovement = allowVerticalMovement;
    }

    private class MoveNavigationButton {

        public MoveNavigationButton(final int action,
                                    android.view.View button) {
            button.setOnTouchListener(new android.view.View.OnTouchListener() {
                @Override
                public boolean onTouch(android.view.View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        keyPressed(action);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        keyReleased(action);
                    }
                    return true;
                }

            });
        }
    }
}
