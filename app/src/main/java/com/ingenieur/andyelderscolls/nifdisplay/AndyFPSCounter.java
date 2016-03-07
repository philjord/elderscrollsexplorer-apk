package com.ingenieur.andyelderscolls.nifdisplay;

import com.jogamp.graph.font.FontFactory;
import com.jogamp.opengl.hudbasics.graph.demos.ui.Label;

import java.io.IOException;
import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.media.j3d.WakeupOnElapsedTime;

import tools3d.mixed3d2d.Canvas3D2D;
import tools3d.utils.Utils3D;


public class AndyFPSCounter
{
	public static int FRAME_SAMPLE = 5;
	public static int TIME_SAMPLE = 500;
	public static int HEIGHT = 50;
	private long currtime;
	private long lasttime;
	private long deltatime;
	private BranchGroup behaviorBranchGroup;
	private AndyFPSCounter.FramesBehavior framesBehavior;
	private AndyFPSCounter.TimeBehavior timeBehavior;
	private Label fpsLabel;
	private int numOfFrames;
	private long timeOfFrames;

	public AndyFPSCounter(Canvas3D2D canvas3d2d)
	{
		this();
		this.addToCanvas(canvas3d2d);
	}

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
		float pixelSizeFPS = 0.00015F * (float)canvas3d2d.getGLWindow().getSurfaceHeight();
		try
		{
			this.fpsLabel = new Label(canvas3d2d.getVertexFactory(), 0, FontFactory.get(0).getDefault(), pixelSizeFPS, "");
			canvas3d2d.addUIShape(fpsLabel);
			this.fpsLabel.setEnabled(true);
			this.fpsLabel.translate(-0.88F, 0.75F, 0.0F);
			this.fpsLabel.setColor(1.0F, 1.0F, 0.0F, 1.0F);
		}
		catch(IOException e)
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

		public void processStimulus(Enumeration critera)
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

		public void processStimulus(Enumeration critera)
		{
			double fps = (double) AndyFPSCounter.this.numOfFrames / ((double) AndyFPSCounter.this.timeOfFrames / 1000.0D);
			AndyFPSCounter.this.fpsLabel.setText("" + (int) Math.rint(fps * 10.0D) / 10);
			AndyFPSCounter.this.numOfFrames = 0;
			AndyFPSCounter.this.timeOfFrames = 0L;
			this.wakeupOn(this.wakeUp);
		}
	}
}


