package com.ingenieur.andyelderscrolls.andyesexplorer.ui;

import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;

import com.jogamp.newt.Window;
import com.jogamp.newt.opengl.GLWindow;

import jogamp.newt.driver.android.WindowDriver;
import tools.WeakListenerList;
import tools3d.navigation.NavigationProcessorInterface;
import tools3d.navigation.NavigationRotationStateListener;

public class NavigationInputNewtMouseLocked implements View.OnCapturedPointerListener,
        View.OnClickListener {

    public static float MOUSE_SENSITIVITY = 1f;

    // multiplyer to get from pixels difference to radian turnage
    // eg 0.01f mean 100 pixels makes for 1 PI per second or 180 degrees

    private static final float FREE_LOOK_GROSS_ROTATE_FACTOR = -0.002f;

    private static final float FINE_RATIO_OF_GROSS = 0.3f;

    private static final int MAX_PIXEL_FOR_FINE_MOVEMENT = 3;

    // The canvas this handler is operating on
    private GLWindow glWindow;

    private NavigationProcessorInterface navigationProcesor;

    private WeakListenerList<NavigationRotationStateListener> navigationRotationStateListeners = new WeakListenerList<NavigationRotationStateListener>();


    //https://developer.android.com/games/playgames/input-mouse
    enum MouseButton {
        LEFT,
        RIGHT,
        MIDDLE,
        UNKNOWN;
        static MouseButton fromMotionEvent(MotionEvent motionEvent) {
            switch (motionEvent.getActionButton()) {
                case MotionEvent.BUTTON_PRIMARY:
                    return MouseButton.LEFT;
                case MotionEvent.BUTTON_SECONDARY:
                    return MouseButton.RIGHT;
                default:
                    return MouseButton.UNKNOWN;
            }
        }
    }


    public NavigationInputNewtMouseLocked() {

    }

    public void addNavigationRotationStateListener(NavigationRotationStateListener navigationRotationStateListener) {
        navigationRotationStateListeners.add(navigationRotationStateListener);
    }

    public void removeNavigationRotationStateListener(NavigationRotationStateListener navigationRotationStateListener) {
        navigationRotationStateListeners.remove(navigationRotationStateListener);
    }

    public void setNavigationProcessor(NavigationProcessorInterface navigationProcesor) {
        this.navigationProcesor = navigationProcesor;
    }

    private void fireListeners(boolean turnLeft, boolean turnRight, boolean turnUp, boolean turnDown) {
        for (NavigationRotationStateListener nrsl : navigationRotationStateListeners) {
            nrsl.inputStateChanged(turnLeft, turnRight, turnUp, turnDown);
        }
    }

    public void setWindow(GLWindow newGlWindow) {
        //https://stackoverflow.com/questions/20502876/set-mouse-position-in-software
        //https://developer.android.com/develop/ui/views/touch-and-input/gestures/movement#pointer-capture

        // remove the old canvas listening
        if (glWindow != null) {
            final Window delegateWindow = glWindow.getDelegatedWindow();
            if (delegateWindow instanceof WindowDriver) {
                WindowDriver wd = (WindowDriver) delegateWindow;
                // ready to grab locked pointer events
                wd.getAndroidView().setOnCapturedPointerListener(null);
            }
        }

        glWindow = newGlWindow;
        if (glWindow != null) {
            final Window delegateWindow = glWindow.getDelegatedWindow();
            if (delegateWindow instanceof WindowDriver) {
                WindowDriver wd = (WindowDriver) delegateWindow;
                // ready to grab locked pointer events
                wd.getAndroidView().setOnCapturedPointerListener(this);
                //click listener
                wd.getAndroidView().setOnGenericMotionListener((view, motionEvent) -> {
                    if (motionEvent.isFromSource(InputDevice.SOURCE_CLASS_POINTER)) {
                        switch (motionEvent.getAction()) {
                            case MotionEvent.ACTION_BUTTON_PRESS:
                                Log.d("MA", MouseButton.fromMotionEvent(motionEvent) + " pressed at " + motionEvent.getX() + ", " + motionEvent.getY());
                                break;
                            case MotionEvent.ACTION_BUTTON_RELEASE:
                                Log.d("MA", MouseButton.fromMotionEvent(motionEvent) + " released at " + motionEvent.getX() + ", " + motionEvent.getY());
                                break;
                        }
                        return true;
                    }
                    return false;
                });
                //scroll listener
                wd.getAndroidView().setOnGenericMotionListener((view, motionEvent) -> {
                    if (motionEvent.isFromSource(InputDevice.SOURCE_CLASS_POINTER)) {
                        switch (motionEvent.getAction()) {
                            case MotionEvent.ACTION_SCROLL:
                                float scrollX = motionEvent.getAxisValue(MotionEvent.AXIS_HSCROLL);
                                float scrollY = motionEvent.getAxisValue(MotionEvent.AXIS_VSCROLL);
                                Log.d("MA", "Mouse scrolled " + scrollX + ", " + scrollY);
                                break;
                        }
                        return true;
                    }
                    return false;
                });
            }
        }
    }

    public boolean hasGLWindow() {
        return glWindow != null;
    }

    @Override
    public boolean onCapturedPointer(View view, MotionEvent motionEvent) {
        float dx = motionEvent.getX();
        float dy = motionEvent.getY();

        if (dx != 0 || dy != 0) {
            double scaledDeltaX = (double) dx * FREE_LOOK_GROSS_ROTATE_FACTOR * MOUSE_SENSITIVITY;
            double scaledDeltaY = (double) dy * FREE_LOOK_GROSS_ROTATE_FACTOR * MOUSE_SENSITIVITY;

            if (Math.abs(dy) < MAX_PIXEL_FOR_FINE_MOVEMENT && Math.abs(dx) < MAX_PIXEL_FOR_FINE_MOVEMENT) {
                scaledDeltaY *= FINE_RATIO_OF_GROSS;
                scaledDeltaX *= FINE_RATIO_OF_GROSS;
            }

            if (navigationProcesor != null) {
                navigationProcesor.changeRotation(scaledDeltaY, scaledDeltaX);
            }

        }
        //TODO: but how do I send all stopped messages? I'm on a move listener.
        fireListeners(dx < 0, dx > 0, dy > 0, dy < 0);

        if (motionEvent.isFromSource(InputDevice.SOURCE_CLASS_POINTER)) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_BUTTON_PRESS:
                    Log.d("MA", MouseButton.fromMotionEvent(motionEvent) + " pressed at " + motionEvent.getX() + ", " + motionEvent.getY() + "********************************************************");
                    break;
                case MotionEvent.ACTION_BUTTON_RELEASE:
                    Log.d("MA", MouseButton.fromMotionEvent(motionEvent) + " released at " + motionEvent.getX() + ", " + motionEvent.getY());
                    break;
            }
            return true;
        }
        // Use the coordinates to update your view and return true if the event is successfully processed.
        return true;
    }

    @Override
    public void onClick(View v) {
        System.err.println("************************************* click");
        System.err.println("************************************* click");
        System.err.println("************************************* click");
    }
}

