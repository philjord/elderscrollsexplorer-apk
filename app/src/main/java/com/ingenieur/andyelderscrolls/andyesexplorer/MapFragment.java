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

import com.ingenieur.andyelderscrolls.R;
import com.ingenieur.andyelderscrolls.utils.TouchImageView;

import org.jogamp.vecmath.Quat4f;
import org.jogamp.vecmath.Vector3f;

import java.util.Random;

import androidx.fragment.app.Fragment;
import tools3d.utils.scenegraph.LocationUpdateListener;

public class MapFragment extends Fragment {
	private View rootView;
	private MapImage map;

	private GestureDetector mDetector;


	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState)
	{
		rootView = inflater.inflate(R.layout.map_panel, container, false);

		ImageButton furnitureCatalogLeftSwiper = (ImageButton) rootView.findViewById(R.id.closeMap);
		furnitureCatalogLeftSwiper.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				((AndyESExplorerActivity) getActivity()).mViewPager.setCurrentItem(1, true);
			}
		});


		map = (MapImage) rootView.findViewById(R.id.map);
		map.setMaxZoom(12f);

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
				ScrollsExplorer scrollsExplorer = ((AndyESExplorerActivity) getContext()).scrollsExplorer;

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
		map.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				return mDetector.onTouchEvent(event);
			}
		});


		return rootView;
	}


	public static class MapImage extends TouchImageView {
		private ScrollsExplorer scrollsExplorer;

		Paint defaultPaint = new Paint();

		public MapImage(Context context) {
			super(context);
		}

		public MapImage(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public MapImage(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}


		private LocationUpdateListener locationUpdateListener;

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
					defaultPaint.setStrokeWidth(5);

					Vector3f loc = new Vector3f();
					scrollsExplorer.simpleWalkSetup.getAvatarLocation().get(loc);

					PointF p = transformToImageCoords(loc);

					defaultPaint.setARGB(128, 0, 0, 255);
					canvas.drawLine(p.x - 20, p.y - 20, p.x + 20, p.y + 20, defaultPaint);
					canvas.drawLine(p.x + 20, p.y - 20, p.x - 20, p.y + 20, defaultPaint);

					//zero
					p = transformToImageCoords(new Vector3f(0, 0, 0));
					defaultPaint.setARGB(255, 255, 255, 255);
					canvas.drawLine(p.x - 20, p.y - 20, p.x + 20, p.y + 20, defaultPaint);
					canvas.drawLine(p.x + 20, p.y - 20, p.x - 20, p.y + 20, defaultPaint);
					//sydaneen
					Random rnd = new Random();
					p = transformToImageCoords(new Vector3f(-108, 3, 936));
					defaultPaint.setARGB(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
					canvas.drawLine(p.x - 20, p.y - 20, p.x + 20, p.y + 20, defaultPaint);
					canvas.drawLine(p.x + 20, p.y - 20, p.x - 20, p.y + 20, defaultPaint);
					//vivec
					p = transformToImageCoords(new Vector3f(423, 8, 1079));
					defaultPaint.setARGB(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
					canvas.drawLine(p.x - 20, p.y - 20, p.x + 20, p.y + 20, defaultPaint);
					canvas.drawLine(p.x + 20, p.y - 20, p.x - 20, p.y + 20, defaultPaint);
					//vos
					p = transformToImageCoords(new Vector3f(1225, 19, -1465));
					defaultPaint.setARGB(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
					canvas.drawLine(p.x - 20, p.y - 20, p.x + 20, p.y + 20, defaultPaint);
					canvas.drawLine(p.x + 20, p.y - 20, p.x - 20, p.y + 20, defaultPaint);
					//center right
					p = transformToImageCoords(new Vector3f(2500, 19, -465));
					defaultPaint.setARGB(255, 255, 0, 0);
					canvas.drawLine(p.x - 20, p.y - 20, p.x + 20, p.y + 20, defaultPaint);
					canvas.drawLine(p.x + 20, p.y - 20, p.x - 20, p.y + 20, defaultPaint);
					//2/3rds up left
					p = transformToImageCoords(new Vector3f(-1880, 19, -1330));
					defaultPaint.setARGB(255, 0, 255, 0);
					canvas.drawLine(p.x - 20, p.y - 20, p.x + 20, p.y + 20, defaultPaint);
					canvas.drawLine(p.x + 20, p.y - 20, p.x - 20, p.y + 20, defaultPaint);
					//top center
					p = transformToImageCoords(new Vector3f(450, 19, -2660));
					defaultPaint.setARGB(255, 0, 255, 255);
					canvas.drawLine(p.x - 20, p.y - 20, p.x + 20, p.y + 20, defaultPaint);
					canvas.drawLine(p.x + 20, p.y - 20, p.x - 20, p.y + 20, defaultPaint);
					//bottom center
					p = transformToImageCoords(new Vector3f(450, 19, 1660));
					defaultPaint.setARGB(255, 255, 0, 255);
					canvas.drawLine(p.x - 20, p.y - 20, p.x + 20, p.y + 20, defaultPaint);
					canvas.drawLine(p.x + 20, p.y - 20, p.x - 20, p.y + 20, defaultPaint);
				}
			}
		}

		private PointF transformToImageCoords(Vector3f loc) {
			PointF p = new PointF(loc.x, loc.z);

			// ok all work on the image needs to be done in normalize coords
			// put loc into normalized morrowind coords (versus the map image bounds)

			// note the map image has a banner which is well abve the center top figure of -2660
			// and a bit of a margin too, but these figures are in morrowind coords not pixels
			int banner = 350;
			int margin = 100;
			p.x += 1880 + margin; //-1880 x lowest
			p.y += 2660 + banner + margin; //(2660 lowest value)

			p.x /= 2550 + 1880 + (margin * 2); //x highest 2400
			p.y /= 1660 + 2660 + banner + (margin * 2); // 16060 highest y value

			// z is naturally in y down mode (big z is more south) no swap required

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


		private Vector3f transformToMWCoords(PointF mousePoint) {
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

			//FIXME: feels like I'm out by about 100 or so, so perhaps something needs fixing up here?
			int banner = 400;
			int margin = 100;
			loc.x *= 2400 + 1880 + margin; //x highest 2400
			loc.z *= 1660 + 2660 + banner + margin; // 1660 highest y value

			loc.x -= 1880 + margin; //-1880 x lowest
			loc.z -= 2660 + banner + margin; //(2660 lowest value)

			return loc;
		}
	}
}