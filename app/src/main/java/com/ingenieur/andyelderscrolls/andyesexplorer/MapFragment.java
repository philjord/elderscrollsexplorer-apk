package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import com.ingenieur.andyelderscrolls.R;
import com.ingenieur.andyelderscrolls.utils.TouchImageView;

import org.jogamp.vecmath.Quat4f;
import org.jogamp.vecmath.Vector3f;

import tools3d.utils.scenegraph.LocationUpdateListener;

public class MapFragment extends Fragment {
	protected View holder;
	protected View rootView;
	protected MapImageInterface map;

	protected GestureDetector mDetector;

	protected ScrollsExplorer scrollsExplorer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.map_panel, container, false);
		holder = rootView.findViewById(R.id.map);

		ImageButton closeButton = (ImageButton) rootView.findViewById(R.id.closeMap);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((AndyESExplorerActivity) getActivity()).mViewPager.setCurrentItem(1, true);
			}
		});

		mDetector = new GestureDetector(getActivity().getApplicationContext(), new GestureDetector.OnGestureListener() {
			@Override
			public boolean onDown(MotionEvent e)
			{
				return false;
			}

			@Override
			public void onShowPress(MotionEvent e) {

			}

			@Override
			public boolean onSingleTapUp(MotionEvent e)
			{
				return false;
			}

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				return false;
			}

			@Override
			public void onLongPress(MotionEvent e) {

				if (scrollsExplorer != null && scrollsExplorer.simpleWalkSetup != null) {
					Vector3f warpTo = map.transformToMWCoords(new PointF(e.getX(), e.getY()));


					//FIXME: very much need to work out a good height to warp to
					warpTo.y = 100;
					scrollsExplorer.simpleWalkSetup.warp(warpTo);

					// tes out does warp work inside, it should really teleport you outside just like a door! look into teleport to target of
					// wow this guy causes memory build and death!
					// in fact moving inside and out cause memory build up too!
					//scrollsExplorer.getSimpleBethCellManager().changeToCell(null, warpTo, new Quat4f(0, 0, 0, 1));//null for cell 0


					AndyESExplorerActivity.logFireBaseContent("simpleWalkSetup.warp " + warpTo);
				}

			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				return false;
			}
		});



		return rootView;
	}

	public void setUpMap(MapImageInterface map) {
		this.map = map;
		map.setMaxZoom(12f);
		this.scrollsExplorer = map.scrollsExplorer;
		map.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				return mDetector.onTouchEvent(event);
			}
		});
		replaceView(holder,map);
	}


	protected void replaceView(View oldV,View newV){
		ViewGroup par = (ViewGroup)oldV.getParent();
		if(par == null){return;}
		int i1 = par.indexOfChild(oldV);
		par.removeViewAt(i1);
		par.addView(newV,i1);
	}

	public static abstract class MapImageInterface extends TouchImageView  {
		public ScrollsExplorer scrollsExplorer;

		private LocationUpdateListener locationUpdateListener;

		private Paint defaultPaint = new Paint();

		public MapImageInterface(Context context, ScrollsExplorer scrollsExplorer) {
			super(context);
			this.scrollsExplorer = scrollsExplorer;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			if (scrollsExplorer == null) {
				this.scrollsExplorer = ((AndyESExplorerActivity) getContext()).scrollsExplorer;
			}
			if (scrollsExplorer != null) {
				// could be a delay in setting this up so retry to add a listener if not done yet
				if (locationUpdateListener == null && scrollsExplorer.simpleWalkSetup != null) {
					locationUpdateListener = new LocationUpdateListener()
					{
						@Override
						public void locationUpdated(Quat4f quat4f, Vector3f vector3f)
						{
							postInvalidate();
						}
					};
					scrollsExplorer.simpleWalkSetup.getAvatarLocation().addAvatarLocationListener(locationUpdateListener);
				}

				if (scrollsExplorer != null && scrollsExplorer.simpleWalkSetup != null) {
					onDrawCustom(canvas);
				}
			}
		}

		protected abstract void onDrawCustom(Canvas canvas);

		protected abstract PointF transformToImageCoords(Vector3f loc);

		protected abstract Vector3f transformToMWCoords(PointF mousePoint);
	}
}