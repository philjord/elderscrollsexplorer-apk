package com.ingenieur.andyelderscrolls.andyesexplorer.games.skyrim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import com.ingenieur.andyelderscrolls.R;
import com.ingenieur.andyelderscrolls.andyesexplorer.MapFragment;
import com.ingenieur.andyelderscrolls.andyesexplorer.ScrollsExplorer;

import org.jogamp.vecmath.Vector3f;

import bsa.source.BsaTextureSource;

public class SkyrimMapImage extends MapFragment.MapImageInterface {

    public SkyrimMapImage(Context context, ScrollsExplorer scrollsExplorer, BsaTextureSource textureSource) {
        super(context, scrollsExplorer, textureSource);
        //FIXME: jebus these images are 2Mb on disk but decompressed to 20Mb when used!
        setImageResource(R.drawable.map_mw_map_vvardenfell);

        banner = 0;
        margin = 300;

        xMin = -1700;
        xMax = 1220;
        yMin = -1260;
        yMax = 1900;
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
        drawGrid(canvas, 180);
    }

}
