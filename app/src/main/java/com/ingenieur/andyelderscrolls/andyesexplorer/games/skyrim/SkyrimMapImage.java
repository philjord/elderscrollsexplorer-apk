package com.ingenieur.andyelderscrolls.andyesexplorer.games.skyrim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;

import com.ingenieur.andyelderscrolls.R;
import com.ingenieur.andyelderscrolls.andyesexplorer.MapFragment;
import com.ingenieur.andyelderscrolls.andyesexplorer.ScrollsExplorer;

import org.jogamp.vecmath.Vector3f;

import java.util.Random;

import tools3d.utils.scenegraph.LocationUpdateListener;

public class SkyrimMapImage extends MapFragment.MapImageInterface {

    Paint defaultPaint = new Paint();

    public SkyrimMapImage(Context context, ScrollsExplorer scrollsExplorer) {
        super(context, scrollsExplorer);
        setImageResource(R.drawable.vr_icon);
    }

    @Override
    protected void onDrawCustom(Canvas canvas) {
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
        //top left
        p = transformToImageCoords(new Vector3f(-1880, 19, -2660));
        defaultPaint.setARGB(255, 0, 255, 255);
        canvas.drawLine(p.x - 20, p.y - 20, p.x + 20, p.y + 20, defaultPaint);
        canvas.drawLine(p.x + 20, p.y - 20, p.x - 20, p.y + 20, defaultPaint);
        //bottom right
        p = transformToImageCoords(new Vector3f(450, 19, 1660));
        defaultPaint.setARGB(255, 255, 0, 255);
        canvas.drawLine(p.x - 20, p.y - 20, p.x + 20, p.y + 20, defaultPaint);
        canvas.drawLine(p.x + 20, p.y - 20, p.x - 20, p.y + 20, defaultPaint);

    }

    protected PointF transformToImageCoords(Vector3f loc) {
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


    protected Vector3f transformToMWCoords(PointF mousePoint) {
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
