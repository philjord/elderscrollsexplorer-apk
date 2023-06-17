package com.ingenieur.andyelderscrolls.andyesexplorer.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.ingenieur.andyelderscrolls.R;

import nifbullet.NavigationProcessorBullet;

public class LookNavigationView extends GLWindowOverLay {
    private static final float FREE_LOOK_GROSS_ROTATE_FACTOR = -0.005f;

    private View.OnClickListener onClickListener;
    private final float SCROLL_THRESHOLD = 2;
    private View button;

    public LookNavigationView(Context context, View parent, NavigationProcessorBullet npb) {
        super(context, parent, R.layout.navigationpanellookpopup, Gravity.RIGHT | Gravity.BOTTOM);
        button = getButton(R.id.looky);
        button.setOnTouchListener(new DragOrTouchListener(npb));
    }

    @Override
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    private class DragOrTouchListener implements View.OnTouchListener {
        private final NavigationProcessorBullet npb;
        private boolean isClick = false;
        private float mDownX;
        private float mDownY;

        public DragOrTouchListener(NavigationProcessorBullet npb) {
            this.npb = npb;
        }

        @Override
        public boolean onTouch(View v, MotionEvent ev) {
            switch (ev.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    isClick = true;
                    mDownX = ev.getX();
                    mDownY = ev.getY();
                    break;
                case MotionEvent.ACTION_CANCEL://fall through
                case MotionEvent.ACTION_UP:
                    // is this a click not a drag?
                    if (isClick && onClickListener != null) {
                        onClickListener.onClick(v);
                        isClick = false;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    float ex = ev.getX();
                    float ey = ev.getY();
                    float dx = ex - mDownX;
                    float dy = ey - mDownY;
                    if (Math.abs(mDownX - ex) > SCROLL_THRESHOLD || Math.abs(mDownY - ey) > SCROLL_THRESHOLD) {
                        isClick = false;

                        if (dx != 0 || dy != 0) {
                            double scaledDeltaX = (double) dx * FREE_LOOK_GROSS_ROTATE_FACTOR;
                            double scaledDeltaY = (double) dy * FREE_LOOK_GROSS_ROTATE_FACTOR;

                            if (npb != null) {
                                npb.changeRotation(scaledDeltaY, scaledDeltaX);
                            }

                            mDownX = ex;
                            mDownY = ey;
                        }
                    }
                    break;
                default:
                    break;
            }
            return true;
        }


    }
}
