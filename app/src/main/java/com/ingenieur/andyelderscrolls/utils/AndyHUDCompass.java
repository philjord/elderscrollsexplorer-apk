package com.ingenieur.andyelderscrolls.utils;

import com.jogamp.graph.font.FontFactory;
import com.jogamp.graph.ui.shapes.Label;

import org.jogamp.vecmath.Quat4f;
import org.jogamp.vecmath.Vector3f;

import java.io.IOException;

import tools.CompassRotation;
import tools3d.mixed3d2d.Canvas3D2D;
import tools3d.utils.YawPitch;
import tools3d.utils.scenegraph.LocationUpdateListener;

/**
 * Created by phil on 3/11/2016.
 */
public class AndyHUDCompass implements LocationUpdateListener
{
	private Label textElementBear;
	private Label textElementAzi;
	public static float HEIGHT = 0.00006F;


	public AndyHUDCompass()
	{
	}

	public void addToCanvas(Canvas3D2D canvas)
	{
		float pixelSizeFPS = HEIGHT * (float) canvas.getGLWindow().getSurfaceHeight();
		try
		{
			textElementBear = new Label(0, FontFactory.get(0).getDefault(), pixelSizeFPS, "");
			canvas.addUIShape(textElementBear);
			textElementBear.setEnabled(true);
			textElementBear.moveTo(-1F, 0.80F, 0f);
			textElementBear.setColor(0.3f, 0f, 0.3f, 0.85f);

			textElementAzi = new Label(0, FontFactory.get(0).getDefault(), pixelSizeFPS, "");
			canvas.addUIShape(textElementAzi);
			textElementAzi.setEnabled(true);
			textElementAzi.moveTo(-1f, 0.75f, 0f);
			textElementAzi.setColor(0.3f, 0f, 0.3f, 0.85f);

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}


	}


	// deburner
	private String[] bearings = new String[]{"N", "NE", "E", "SE", "S", "SW", "W", "NW", "eh?"};

	private YawPitch yawPitch = new YawPitch();

	public void locationUpdated(Quat4f rot, Vector3f trans)
	{
		if (textElementBear != null)
		{
			yawPitch.set(rot);

			// make it a degree and reverse the rotation (from java3D CCW to compass CW)
			int yawDeg = (int) CompassRotation.wrapToMax(-CompassRotation.radToDeg(yawPitch.getYaw()), 360);

			textElementBear.setText("" + yawDeg + " " + bearings[(yawDeg + 22) % 360 / 45]);
			textElementAzi.setText("" + (int) CompassRotation.radToDeg(yawPitch.getPitch()));

			//double yawRad = -yawPitch.getYaw();
			//compassNeedleElement.clear();
			//compassNeedleElement.getGraphics().drawLine(25, 25, (int) (Math.sin(yawRad) * 20) + 25, (int) (Math.cos(yawRad) * -20) + 25);
		}
	}

}
