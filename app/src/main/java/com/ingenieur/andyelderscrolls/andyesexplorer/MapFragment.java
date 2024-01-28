package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
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

import bsa.source.BsaTextureSource;
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
					Vector3f warpTo = map.transformToESCoords(new PointF(e.getX(), e.getY()));


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

		if(map != null)
			replaceView(holder,map);

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
		if(holder != null)
			replaceView(holder, map);
	}


	protected void replaceView(View oldV,View newV){
		ViewGroup par = (ViewGroup)oldV.getParent();
		if(par == null){return;}
		int i1 = par.indexOfChild(oldV);
		par.removeViewAt(i1);
		par.addView(newV,i1);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if(map != null)
			map.panToCharacter();
	}

	public static abstract class MapImageInterface extends TouchImageView {

		protected int banner = 0;
		protected int margin = 0;

		protected float xMin = 0;
		protected float xMax = 0;
		protected float yMin = 0;
		protected float yMax = 0;

		protected ScrollsExplorer scrollsExplorer;

		private LocationUpdateListener locationUpdateListener;

		protected Paint defaultPaint = new Paint();

		protected BsaTextureSource textureSource;

		public MapImageInterface(Context context, ScrollsExplorer scrollsExplorer, BsaTextureSource textureSource) {
			super(context);
			this.scrollsExplorer = scrollsExplorer;
			this.textureSource = textureSource;
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

		/**
		 * from EXExplorer location x,y
		 * @param loc
		 * @return
		 */
		protected PointF toImageNormalizedCoords(Vector3f loc) {
			PointF p = new PointF(loc.x, loc.z);

			p.x += -xMin + margin;
			p.y += -yMin + banner + margin;

			p.x /= -xMin+xMax + (margin * 2); //range plus each margin so now normalized
			p.y /= -yMin+yMax + banner + (margin * 2); //range plus each margin so now normalized

			// z is naturally in y down mode (big z is more south) no swap required
			return p;
		}

		protected PointF transformToImageCoords(Vector3f loc) {
			PointF p = toImageNormalizedCoords(loc);

			int width = this.getWidth();
			float imageWidth = getImageWidth();
			int height = this.getHeight();
			float imageHeight = getImageHeight();// this includes the zoom scale and is the height after scale to fit it as well it's literaally the on screen size

			p.x *= imageWidth;
			p.y *= imageHeight;

			float imageWidthStart = (width / 2) - (imageWidth / 2);
			p.x += imageWidthStart;
			float imageHeightStart = (height / 2) - (imageHeight / 2);
			p.y += imageHeightStart;

			PointF scroll = getScrollPosition();
			p.x -= (scroll.x - 0.5) * imageWidth;
			p.y -= (scroll.y - 0.5) * imageHeight;
			return p;
		}


		protected Vector3f transformToESCoords(PointF mousePoint) {
			Vector3f loc = new Vector3f(mousePoint.x, 0, mousePoint.y);

			int width = this.getWidth();
			float imageWidth = getImageWidth();
			int height = this.getHeight();
			float imageHeight = getImageHeight();

			PointF scroll = getScrollPosition();
			loc.x += (scroll.x - 0.5) * imageWidth;
			loc.z += (scroll.y - 0.5) * imageHeight;

			float imageWidthStart = (width / 2) - (imageWidth / 2);
			loc.x -= imageWidthStart;
			float imageHeightStart = (height / 2) - (imageHeight / 2);
			loc.z -= imageHeightStart;

			loc.x /= imageWidth;
			loc.z /= imageHeight;

			loc.x *= -xMin+xMax + margin; //range plus margin
			loc.z *= -yMin+yMax + banner + margin; //range plus margin

			loc.x -= -xMin + margin;
			loc.z -= -yMin + banner + margin;

			return loc;
		}

		protected void  drawLayoutPoints(Canvas canvas){
			//zero
			PointF p = transformToImageCoords(new Vector3f(0, 0, 0));
			defaultPaint.setARGB(255, 255, 0, 255);
			canvas.drawLine(p.x - 20, p.y - 20, p.x + 20, p.y + 20, defaultPaint);
			canvas.drawLine(p.x + 20, p.y - 20, p.x - 20, p.y + 20, defaultPaint);
			//top left
			p = transformToImageCoords(new Vector3f(xMin, 19, yMin));
			defaultPaint.setARGB(255, 0, 255, 255);
			canvas.drawLine(p.x - 20, p.y - 20, p.x + 20, p.y + 20, defaultPaint);
			canvas.drawLine(p.x + 20, p.y - 20, p.x - 20, p.y + 20, defaultPaint);
			//bottom right
			p = transformToImageCoords(new Vector3f(xMax, 19, yMax));
			defaultPaint.setARGB(255, 255, 255, 0);
			canvas.drawLine(p.x - 20, p.y - 20, p.x + 20, p.y + 20, defaultPaint);
			canvas.drawLine(p.x + 20, p.y - 20, p.x - 20, p.y + 20, defaultPaint);
		}

		protected abstract void onDrawCustom(Canvas canvas);

		public void panToCharacter() {
			Vector3f trans = new Vector3f();
			scrollsExplorer.simpleWalkSetup.getAvatarLocation().get(trans);
			PointF charLoc = toImageNormalizedCoords(trans);
			this.setScrollPosition(charLoc.x,charLoc.y);
		}
	}
}