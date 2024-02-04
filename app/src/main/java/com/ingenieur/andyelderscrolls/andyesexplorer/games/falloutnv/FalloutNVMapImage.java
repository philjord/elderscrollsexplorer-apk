package com.ingenieur.andyelderscrolls.andyesexplorer.games.falloutnv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import com.ingenieur.andyelderscrolls.R;
import com.ingenieur.andyelderscrolls.andyesexplorer.MapFragment;
import com.ingenieur.andyelderscrolls.andyesexplorer.ScrollsExplorer;

import org.jogamp.vecmath.Vector3f;

import bsa.source.BsaTextureSource;
import bsaio.BsaUtils;

public class FalloutNVMapImage extends MapFragment.MapImageInterface {

    private int gridspace = 180;

    public FalloutNVMapImage(Context context, ScrollsExplorer scrollsExplorer, BsaTextureSource textureSource) {
        super(context, scrollsExplorer, textureSource);
        Bitmap mapBitmap = BsaUtils.getBitmapFromTextureSource("textures\\interface\\worldmap\\wasteland_nv_2048_no_map.ktx", textureSource);
        setImageBitmap(mapBitmap);

        // zoom in a bit
        setZoom(5, 0, 0, ScaleType.FIT_CENTER);
        // don't go too far out
        setMinZoom(3);
        banner = 0;

        // map grids are about 180 each 17grid up/down
        margin = (int) (gridspace * 3.66f);


        //zero is offset by 1.5 and 1.75 grids worth
        float xoff = 1.5f*gridspace;
        float yoff = 1.75f*gridspace;

        xMin = -(gridspace*7f) - xoff;
        xMax = (gridspace*7f) - xoff;
        yMin = -(gridspace*7f) - yoff;
        yMax = (gridspace*7f) - yoff;
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

        //drawLayoutPoints(canvas);
        //drawGrid(canvas, gridspace);
    }


}
