package com.ingenieur.andyelderscrolls.utils;

import android.graphics.Point;

import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.opengl.GLWindow;

import jogamp.newt.driver.android.WindowDriver;

/**
 * Created by phil on 3/7/2016.
 */
public class DragMouseAdapter extends MouseAdapter {
    public enum DRAG_TYPE {
        DOWN, UP, LEFT, RIGHT
    }

    private boolean draggingDown = false;
    private boolean draggingUp = false;
    private boolean draggingLeft = false;
    private boolean draggingRight = false;

    private Point startMousePosition = null;

    private boolean bottomHalfOnly = false;

    private Listener listener;

    public Listener getListener() {
        return listener;
    }


    public DragMouseAdapter() {

    }

    public DragMouseAdapter(boolean bottomHalfOnly) {
        this.bottomHalfOnly = bottomHalfOnly;
    }


    public void setListener(Listener listener) {
        this.listener = listener;
    }


    public interface Listener {
        public void dragComplete(final MouseEvent e, DRAG_TYPE dragType);
    }


    @Override
    public void mousePressed(final MouseEvent e) {
        //clear everything
        draggingDown = false;
        draggingUp = false;
        draggingLeft = false;
        draggingRight = false;

        startMousePosition = new Point(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        if (listener != null && startMousePosition != null) {

            int x = e.getX();
            int y = e.getY();
            int sw = 600;
            int sh = 600;
            if(e.getSource() instanceof GLWindow) {
                sw = ((GLWindow) e.getSource()).getWidth();
                sh = ((GLWindow) e.getSource()).getHeight();
            } else if (e.getSource() instanceof WindowDriver) {
                sw = ((WindowDriver) e.getSource()).getWidth();
                sh = ((WindowDriver) e.getSource()).getHeight();
            }

            if (!bottomHalfOnly || e.getY() > sh / 2) {
                int dx = x - startMousePosition.x;
                int dy = y - startMousePosition.y;// notice y=0 top of screen

                if (draggingDown && dy > (sh / 8)) {
                    listener.dragComplete(e, DRAG_TYPE.DOWN);
                } else if (draggingUp && dy < -(sh / 8)) {
                    listener.dragComplete(e, DRAG_TYPE.UP);
                } else if (draggingLeft && dx < -(sw / 4)) {
                    listener.dragComplete(e, DRAG_TYPE.LEFT);
                } else if (draggingRight && dx > (sw / 4)) {
                    listener.dragComplete(e, DRAG_TYPE.RIGHT);
                }
            }
        }
        //clear everything
        draggingDown = false;
        draggingUp = false;
        draggingLeft = false;
        draggingRight = false;
        startMousePosition = null;
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        // are if still under way with no cancel?
        if (startMousePosition != null) {
            int x = e.getX();
            int y = e.getY();

            int sw = 600;
            int sh = 600;
            if(e.getSource() instanceof GLWindow) {
                sw = ((GLWindow) e.getSource()).getWidth();
                sh = ((GLWindow) e.getSource()).getHeight();
            } else if (e.getSource() instanceof WindowDriver) {
                sw = ((WindowDriver) e.getSource()).getWidth();
                sh = ((WindowDriver) e.getSource()).getHeight();
            }

            if (!bottomHalfOnly || e.getY() > sh / 2) {
                int dx = x - startMousePosition.x;
                int dy = y - startMousePosition.y;// notice y=0 top of screen

                // are we kicking off now
                if (draggingDown == false &&
                        draggingUp == false &&
                        draggingLeft == false &&
                        draggingRight == false) {
                    // which way have we gone?
                    if (dy > 0) draggingDown = true;
                    if (dy < 0) draggingUp = true;
                    if (dx < 0) draggingLeft = true;
                    if (dx > 0) draggingRight = true;
                } else {
                    // have we stayed on course?
                    if ((draggingDown || draggingUp) && (dx > 80 || dx < -80)) {
                        // cancelled
                        draggingDown = false;
                        draggingUp = false;
                    }

                    if ((draggingLeft || draggingRight) && (dy > 80 || dy < -80)) {
                        // cancelled
                        draggingLeft = false;
                        draggingRight = false;
                    }

                    // throw away the start for fun
                    if (draggingDown == false &&
                            draggingUp == false &&
                            draggingLeft == false &&
                            draggingRight == false)
                        startMousePosition = null;
                }
            }
        }

    }
}
