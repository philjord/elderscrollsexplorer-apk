package com.ingenieur.andyelderscrolls.andyesexplorer.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.ingenieur.andyelderscrolls.R;

import nifbullet.NavigationProcessorBullet;

public class LookNavigationView extends GLWindowOverLay {
	public LookNavigationView(Context context, View parent, NavigationProcessorBullet npb) {
		super(context, parent, R.layout.navigationpanellookpopup, Gravity.RIGHT | Gravity.BOTTOM);
		new LookNavigationButton(npb, getButton(R.id.looky));
	}

	private static class LookNavigationButton  {
		private static final float FREE_LOOK_GROSS_ROTATE_FACTOR = -0.005f;
		private float mouseDownLocationx;
		private float mouseDownLocationy;
		public LookNavigationButton(	final NavigationProcessorBullet npb,
																	android.view.View button) {
			button.setOnTouchListener(new android.view.View.OnTouchListener() {
				@Override
				public boolean onTouch(android.view.View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						mouseDownLocationx = event.getX();
						mouseDownLocationy = event.getY();
					} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
						float ex = event.getX();
						float ey = event.getY();

						float dx = ex - mouseDownLocationx;
						float dy = ey - mouseDownLocationy;

						if (dx != 0 || dy != 0) {
							double scaledDeltaX = (double) dx * FREE_LOOK_GROSS_ROTATE_FACTOR;
							double scaledDeltaY = (double) dy * FREE_LOOK_GROSS_ROTATE_FACTOR;

							if (npb != null) {
								npb.changeRotation(scaledDeltaY, scaledDeltaX);
							}

							mouseDownLocationx = ex;
							mouseDownLocationy = ey;
						}
					}
					return true;
				}

			});
		}
	}
}
