package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;


public class NavigationPanel {
	private Context context;
	private PopupWindow popupWindow;
	private View popupView;
	private int mCurrentX = 0;
	private int mCurrentY = 0;
	private int gravity;
	private View parent;


	public NavigationPanel(Context context, View parent, int viewId, int gravity) {
		this.context = context;
		this.parent = parent;
		this.gravity = gravity;
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		popupView = layoutInflater.inflate(viewId, null);

		// convert to pixel for the location
		mCurrentX = (int)(mCurrentX * context.getResources().getDisplayMetrics().density);
		mCurrentY = (int)(mCurrentY * context.getResources().getDisplayMetrics().density);

		popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);


	}

	public void showTooltip()
	{
		popupWindow.showAtLocation(parent, gravity, mCurrentX, mCurrentY);

		// add a listener for drags that aren't caught by other buttons
		popupView.setOnTouchListener(new View.OnTouchListener() {
			private float mDx;
			private float mDy;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					mDx = mCurrentX - event.getRawX();
					mDy = mCurrentY - event.getRawY();
				} else
				if (action == MotionEvent.ACTION_MOVE) {
					mCurrentX = (int) (event.getRawX() + mDx);
					mCurrentY = (int) (event.getRawY() + mDy);
					popupWindow.update(mCurrentX, mCurrentY, -1, -1);
				}
				return true;
			}
		});
	}

	public void hideTooltip() {
		if (popupWindow != null & popupWindow.isShowing() && !((Activity) NavigationPanel.this.context).isFinishing() )
			popupWindow.dismiss();
	}



	public View getButton(int viewId) {
		return popupView.findViewById(viewId);
	}

}
