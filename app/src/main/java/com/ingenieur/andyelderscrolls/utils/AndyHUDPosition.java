package com.ingenieur.andyelderscrolls.utils;

import com.jogamp.graph.font.FontFactory;
import com.jogamp.graph.ui.shapes.Label;

import org.jogamp.vecmath.Quat4f;
import org.jogamp.vecmath.Vector3f;

import java.io.IOException;

import tools3d.mixed3d2d.Canvas3D2D;
import tools3d.utils.scenegraph.LocationUpdateListener;

/**
 * Created by phil on 3/11/2016.
 */
public class AndyHUDPosition implements LocationUpdateListener
{
	private Label posLabel;
	public static float HEIGHT = 0.00006F;

	public AndyHUDPosition()
	{
	}

	public void addToCanvas(Canvas3D2D canvas3d2d)
	{
		float pixelSizeFPS = HEIGHT * (float) canvas3d2d.getGLWindow().getSurfaceHeight();
		try
		{
			posLabel = new Label(0, FontFactory.get(0).getDefault(), pixelSizeFPS, "");
			canvas3d2d.addUIShape(posLabel);
			posLabel.setEnabled(true);
			posLabel.moveTo(-1F, 0.85F, 0f);
			posLabel.setColor(0.3f, 0.0f, 0.3f, 0.85f);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void removeFromCanvas(Canvas3D2D canvas)
	{

	}

	public void locationUpdated(Quat4f rot, Vector3f trans)
	{
		if (posLabel != null)
			posLabel.setText("" + (int) trans.x + ", " + (int) trans.y + ", " + (int) trans.z);
	}

}
