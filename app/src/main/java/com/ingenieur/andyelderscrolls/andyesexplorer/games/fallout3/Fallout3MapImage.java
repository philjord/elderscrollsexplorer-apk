package com.ingenieur.andyelderscrolls.andyesexplorer.games.fallout3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import com.ingenieur.andyelderscrolls.andyesexplorer.MapFragment;
import com.ingenieur.andyelderscrolls.andyesexplorer.ScrollsExplorer;

import org.jogamp.vecmath.Vector3f;

import bsa.source.BsaTextureSource;
import bsaio.BsaUtils;

public class Fallout3MapImage extends MapFragment.MapImageInterface {

    Paint defaultPaint = new Paint();

    public Fallout3MapImage(Context context, ScrollsExplorer scrollsExplorer, BsaTextureSource textureSource) {
        super(context, scrollsExplorer, textureSource);
        Bitmap mapBitmap = BsaUtils.getBitmapFromTextureSource("textures\\interface\\worldmap\\wasteland_2048_no_map.ktx", textureSource);
        setImageBitmap(mapBitmap);

        // zoom in a bit
        setZoom(5, 0, 0, ScaleType.FIT_CENTER);
        // don't go too far out
        setMinZoom(3);
        banner = 0;
        margin = 0;

        xMin = -1700;
        xMax = 1220;
        yMin = -1260;
        yMax = 1900;

        // for glowing background
        //https://medium.com/@yuriyskul/different-ways-to-create-glowing-shapes-in-android-canvas-8b73010411fe
    }


    @Override
    protected void onDrawCustom(Canvas canvas) {
        defaultPaint.setStrokeWidth(5);

        Vector3f loc = new Vector3f();
        scrollsExplorer.simpleWalkSetup.getAvatarLocation().get(loc);

        PointF p = transformToImageCoords(loc);

        defaultPaint.setARGB(255, 255, 255, 255);
        canvas.drawLine(p.x - 20, p.y - 20, p.x + 20, p.y + 20, defaultPaint);
        canvas.drawLine(p.x + 20, p.y - 20, p.x - 20, p.y + 20, defaultPaint);

        drawLayoutPoints(canvas);
    }



}
