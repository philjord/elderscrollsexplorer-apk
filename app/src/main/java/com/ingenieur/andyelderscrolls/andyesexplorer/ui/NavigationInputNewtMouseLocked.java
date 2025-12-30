package com.ingenieur.andyelderscrolls.andyesexplorer.ui;

import android.view.MotionEvent;
import android.view.View;

import com.jogamp.newt.Window;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.opengl.GLWindow;

import jogamp.newt.driver.android.WindowDriver;
import tools.WeakListenerList;
import tools3d.navigation.NavigationProcessorInterface;
import tools3d.navigation.NavigationRotationStateListener;

public class NavigationInputNewtMouseLocked implements View.OnCapturedPointerListener {

    public static float MOUSE_SENSITIVITY = 1f;

    // multiplyer to get from pixels difference to radian turnage
    // eg 0.01f mean 100 pixels makes for 1 PI per second or 180 degrees

    private static final float FREE_LOOK_GROSS_ROTATE_FACTOR = -0.002f;

    private static final float FINE_RATIO_OF_GROSS = 0.3f;

    private static final int MAX_PIXEL_FOR_FINE_MOVEMENT = 3;

    // The canvas this handler is operating on
    private GLWindow glWindow;

    private NavigationProcessorInterface navigationProcesor;

    //private Point previousMouseLocation = new Point();

    //private Point centerLocation = new Point();

    //boolean isRecentering = false;

    private WeakListenerList<NavigationRotationStateListener> navigationRotationStateListeners = new WeakListenerList<NavigationRotationStateListener>();

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
        // tell the listeners
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
            }
        }
    }

    public boolean hasGLWindow() {
        return glWindow != null;
    }

    private void recenterMouse() {
        if (glWindow != null) {
            // work out where the mouse should be
            glWindow.warpPointer(glWindow.getWidth() / 2, glWindow.getHeight() / 2);
        }
    }

    @Override
    public boolean onCapturedPointer(View view, MotionEvent e) {
        // Get the coordinates required by your app.
        float horizontalOffset = e.getX();
        System.err.println("Hi it's me, I've just seen a thing-> " + horizontalOffset);

        float dx = e.getX();
        float dy = e.getY();

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


        // Use the coordinates to update your view and return true if the event is
        // successfully processed.
        return true;
    }
}

