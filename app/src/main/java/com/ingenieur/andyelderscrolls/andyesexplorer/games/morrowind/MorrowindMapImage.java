package com.ingenieur.andyelderscrolls.andyesexplorer.games.morrowind;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;

import com.ingenieur.andyelderscrolls.R;
import com.ingenieur.andyelderscrolls.andyesexplorer.MapFragment;
import com.ingenieur.andyelderscrolls.andyesexplorer.ScrollsExplorer;

import org.jogamp.vecmath.Vector3f;

import java.util.Random;

import bsa.source.BsaTextureSource;
import tools3d.utils.scenegraph.LocationUpdateListener;

public class MorrowindMapImage extends MapFragment.MapImageInterface {

    public MorrowindMapImage(Context context, ScrollsExplorer scrollsExplorer, BsaTextureSource textureSource) {
        super(context, scrollsExplorer, textureSource);
        setImageResource(R.drawable.map_mw_map_vvardenfell);

        // zoom in a bit
        setZoom(5, 0, 0, ScaleType.FIT_CENTER);
        // don't go too far out
        setMinZoom(3);

        banner = 300;
        margin = 50;

        xMin = -1560;
        xMax = 2200;
        yMin = -2660;
        yMax = 1580;
    }

    private LocationUpdateListener locationUpdateListener;

    @Override
    protected void onDrawCustom(Canvas canvas) {
        defaultPaint.setStrokeWidth(5);

        Vector3f loc = new Vector3f();
        scrollsExplorer.simpleWalkSetup.getAvatarLocation().get(loc);

        PointF p = transformToImageCoords(loc);

        defaultPaint.setARGB(255, 255, 255, 255);
        canvas.drawLine(p.x - 20, p.y - 20, p.x + 20, p.y + 20, defaultPaint);
        canvas.drawLine(p.x + 20, p.y - 20, p.x - 20, p.y + 20, defaultPaint);

        //drawLayoutPoints(canvas);
        //drawGrid(canvas, 200);
    }

}
