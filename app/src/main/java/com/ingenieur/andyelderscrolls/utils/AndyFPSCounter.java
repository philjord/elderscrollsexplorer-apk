package com.ingenieur.andyelderscrolls.utils;

import com.jogamp.graph.font.FontFactory;
import com.jogamp.graph.ui.shapes.Label;

import org.jogamp.java3d.Behavior;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.WakeupCriterion;
import org.jogamp.java3d.WakeupOnElapsedFrames;
import org.jogamp.java3d.WakeupOnElapsedTime;

import java.io.IOException;
import java.util.Iterator;

import tools3d.mixed3d2d.Canvas3D2D;
import tools3d.utils.Utils3D;

public class AndyFPSCounter
{
	public static int TIME_SAMPLE = 500;
	public static float HEIGHT = 0.00003F;
	private long currtime;
	private long lasttime;
	private long deltatime;
	private BranchGroup behaviorBranchGroup;
	private AndyFPSCounter.FramesBehavior framesBehavior;
	private AndyFPSCounter.TimeBehavior timeBehavior;
	private Label fpsLabel;
	private int numOfFrames;
	private long timeOfFrames;

	public AndyFPSCounter()
	{
		this.currtime = 0L;
		this.lasttime = 0L;
		this.behaviorBranchGroup = new BranchGroup();
		this.framesBehavior = new AndyFPSCounter.FramesBehavior();
		this.timeBehavior = new AndyFPSCounter.TimeBehavior();
		this.numOfFrames = 0;
		this.timeOfFrames = 0L;
		this.framesBehavior.setSchedulingBounds(Utils3D.defaultBounds);
		this.behaviorBranchGroup.addChild(this.framesBehavior);
		this.timeBehavior.setSchedulingBounds(Utils3D.defaultBounds);
		this.behaviorBranchGroup.addChild(this.timeBehavior);
	}

	public void addToCanvas(Canvas3D2D canvas3d2d)
	{
		float pixelSizeFPS = HEIGHT * (float) canvas3d2d.getGLWindow().getSurfaceHeight();
		try
		{
			this.fpsLabel = new Label(0, FontFactory.get(0).getDefault(), pixelSizeFPS, "");
			canvas3d2d.addUIShape(fpsLabel);
			this.fpsLabel.setEnabled(true);
			this.fpsLabel.moveTo(-0.88F, 0.95F, 0.0F);
			this.fpsLabel.setColor(1.0F, 1.0F, 0.0F, 1.0F);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public BranchGroup getBehaviorBranchGroup()
	{
		return this.behaviorBranchGroup;
	}

	private class FramesBehavior extends Behavior
	{
		private WakeupOnElapsedFrames wakeUp;

		private FramesBehavior()
		{
			this.wakeUp = new WakeupOnElapsedFrames(0);
		}

		public void initialize()
		{
			this.wakeupOn(this.wakeUp);
		}

		public void processStimulus(Iterator<WakeupCriterion> critera)
		{
			AndyFPSCounter.this.currtime = System.currentTimeMillis();
			AndyFPSCounter.this.deltatime = AndyFPSCounter.this.currtime - AndyFPSCounter.this.lasttime;
			AndyFPSCounter.this.lasttime = System.currentTimeMillis();
			AndyFPSCounter.this.numOfFrames = AndyFPSCounter.this.numOfFrames + 1;
			AndyFPSCounter.this.timeOfFrames = AndyFPSCounter.this.timeOfFrames + AndyFPSCounter.this.deltatime;

			this.wakeupOn(this.wakeUp);
		}
	}

	private class TimeBehavior extends Behavior
	{
		private WakeupOnElapsedTime wakeUp;

		private TimeBehavior()
		{
			this.wakeUp = new WakeupOnElapsedTime((long) AndyFPSCounter.TIME_SAMPLE);
		}

		public void initialize()
		{
			this.wakeupOn(this.wakeUp);
		}

		public void processStimulus(Iterator<WakeupCriterion> critera)
		{
			double fps = (double) AndyFPSCounter.this.numOfFrames / ((double) AndyFPSCounter.this.timeOfFrames / 1000.0D);
			String newText = "" + (int) Math.rint(fps * 10.0D) / 10;
			if (!newText.equals(fpsLabel.getText()))
				AndyFPSCounter.this.fpsLabel.setText(newText);

			AndyFPSCounter.this.numOfFrames = 0;
			AndyFPSCounter.this.timeOfFrames = 0L;

			count++;// each 500 milli so 10 = 5sec
			if (count > 10)
			{
				//System.out.println("FPS" + ((int) Math.rint(fps * 10) / 10));
				count = 0;
			}


			this.wakeupOn(this.wakeUp);
		}
		private int count = 0;
	}
}


