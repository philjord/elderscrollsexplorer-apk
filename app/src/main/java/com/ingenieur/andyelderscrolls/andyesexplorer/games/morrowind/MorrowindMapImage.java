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

        banner = 350;
        margin = 100;

        xMin = -1880;
        xMax = 2550;
        yMin = -2660;
        yMax = 1660;
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



        drawLayoutPoints(canvas);
    }

}
