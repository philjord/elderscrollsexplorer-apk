package com.ingenieur.andyelderscrolls.andyesexplorer.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.ingenieur.andyelderscrolls.R;

import nifbullet.NavigationProcessorBullet;
import tools3d.navigation.twocircles.NavigationInputNewtMove;

public class MoveNavigationView extends GLWindowOverLay {
	public MoveNavigationView(Context context, View parent, NavigationProcessorBullet npb) {
		super(context, parent, R.layout.navigationpanelmovepopup,Gravity.LEFT | Gravity.BOTTOM);
		NavigationInputNewtMove.VERTICAL_RATE = 50f;// allow jumping
		new MoveNavigationButton(NavigationInputNewtMove.FORWARD_RATE, 0, 0, npb, getButton(R.id.navPanelForwardButton));
		new MoveNavigationButton(-NavigationInputNewtMove.BACKWARD_RATE, 0, 0, npb, getButton(R.id.navPanelBackButton));
		new MoveNavigationButton(0, -NavigationInputNewtMove.STRAFF_RATE, 0,  npb, getButton(R.id.navPanelLeftButton));
		new MoveNavigationButton(0, NavigationInputNewtMove.STRAFF_RATE, 0, npb, getButton(R.id.navPanelRightButton));
		new MoveNavigationButton(0, 0, NavigationInputNewtMove.VERTICAL_RATE, npb, getButton(R.id.navPanelUpButton));
		new MoveNavigationButton(0, 0, -NavigationInputNewtMove.VERTICAL_RATE, npb, getButton(R.id.navPanelDownButton));
	}

	private static class MoveNavigationButton  {

		public MoveNavigationButton(final float zRate,
																final float xRate,
																final float yRate,
																final NavigationProcessorBullet npb,
																android.view.View button) {
			button.setOnTouchListener(new android.view.View.OnTouchListener() {
				@Override
				public boolean onTouch(android.view.View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						if(zRate!=0)
							npb.setZChange(zRate);
						if(xRate!=0)
							npb.setXChange(xRate);
						if(yRate!=0)
							npb.setYChange(yRate);
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						if(zRate!=0)
							npb.setZChange(0);
						if(xRate!=0)
							npb.setXChange(0);
						if(yRate!=0)
							npb.setYChange(0);
					}
					return true;
				}

			});
		}
	}
}
