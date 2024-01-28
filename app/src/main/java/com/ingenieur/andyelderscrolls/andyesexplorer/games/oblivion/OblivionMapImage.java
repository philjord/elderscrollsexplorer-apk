package com.ingenieur.andyelderscrolls.andyesexplorer.games.oblivion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;


import com.ingenieur.andyelderscrolls.R;
import com.ingenieur.andyelderscrolls.andyesexplorer.MapFragment;
import com.ingenieur.andyelderscrolls.andyesexplorer.ScrollsExplorer;

import org.jogamp.vecmath.Vector3f;

import bsa.source.BsaTextureSource;
import bsaio.BsaUtils;


public class OblivionMapImage extends MapFragment.MapImageInterface {

    private Bitmap youAreHere;
    private Paint youAreHerePaint = new Paint();

    public OblivionMapImage(Context context, ScrollsExplorer scrollsExplorer, BsaTextureSource textureSource) {
        super(context, scrollsExplorer, textureSource);

        Bitmap mapBitmap = BsaUtils.getBitmapFromTextureSource("textures\\menus\\map\\world\\cyrodiil_resized.ktx", textureSource);
        // image used has a big ass black line to make it square
        mapBitmap = Bitmap.createBitmap(mapBitmap, 0, 0, 2048, 2048-372);
        setImageBitmap(mapBitmap);

        youAreHere = BsaUtils.getBitmapFromTextureSource("textures\\menus\\map\\world\\world_map_marker_you_are_here.ktx", textureSource);

        // zoom in a bit
        setZoom(5, 0, 0, ScaleType.FIT_CENTER);
        // don't go too far out
        setMinZoom(3);

        banner = 0;
        margin = 0;

        xMin = -3000;
        xMax = 3000;
        yMin = -2320;
        yMax = 2850;
    }

    @Override
    protected void onDrawCustom(Canvas canvas) {
        defaultPaint.setStrokeWidth(5);

        Vector3f loc = new Vector3f();
        scrollsExplorer.simpleWalkSetup.getAvatarLocation().get(loc);

        PointF p = transformToImageCoords(loc);

        //p.x -= youAreHere.getWidth()/2;
        //p.y -= youAreHere.getHeight()/2;
        // todo translate to dp so it's consistent
        RectF dst = new RectF(p.x - 40, p.y - 40, p.x + 40, p.y + 40);
        //FIXME: what the hell!!! what's the correct blend mode?
        youAreHerePaint.setBlendMode(BlendMode.SRC_OVER);
        canvas.drawBitmap(youAreHere, null, dst, youAreHerePaint);

        drawLayoutPoints(canvas);
    }
}
