package com.ingenieur.andyelderscolls.utils;

import android.graphics.Point;
import android.util.Log;

import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;

/**
 * Created by phil on 3/7/2016.
 */
public class DragMouseAdapter extends MouseAdapter
{
	public enum DRAG_TYPE
	{
		DOWN, UP, LEFT, RIGHT
	}

	private boolean draggingDown = false;
	private boolean draggingUp = false;
	private boolean draggingLeft = false;
	private boolean draggingRight = false;

	private Point startMousePosition = null;

	private Listener listener;

	public Listener getListener()
	{
		return listener;
	}

	public void setListener(Listener listener)
	{
		this.listener = listener;
	}


	public interface Listener
	{
		public void dragComplete(final MouseEvent e, DRAG_TYPE dragType);
	}


	@Override
	public void mousePressed(final MouseEvent e)
	{
		//clear everything
		draggingDown = false;
		draggingUp = false;
		draggingLeft = false;
		draggingRight = false;

		startMousePosition = new Point(e.getX(), e.getY());
	}

	@Override
	public void mouseReleased(final MouseEvent e)
	{
		if (listener != null && startMousePosition != null)
		{

			int x = e.getX();
			int y = e.getY();

			int dx = startMousePosition.x - x;
			int dy = startMousePosition.y - y;

			if (draggingDown && dy < -350)
			{
				listener.dragComplete(e, DRAG_TYPE.DOWN);
			}
			else if (draggingUp && dy > 350)
			{
				listener.dragComplete(e, DRAG_TYPE.UP);
			}
			else if (draggingLeft && dx < -300)
			{
				listener.dragComplete(e, DRAG_TYPE.LEFT);
			}
			else if (draggingRight && dx > 300)
			{
				listener.dragComplete(e, DRAG_TYPE.RIGHT);
			}
		}
		//clear everything
		draggingDown = false;
		draggingUp = false;
		draggingLeft = false;
		draggingRight = false;
		startMousePosition = null;
	}

	@Override
	public void mouseDragged(final MouseEvent e)
	{
		// are if still under way with no cancel?
		if (startMousePosition != null)
		{
			int x = e.getX();
			int y = e.getY();

			int dx = startMousePosition.x - x;
			int dy = startMousePosition.y - y;

			// are we kicking off now
			if (draggingDown == false &&
					draggingUp == false &&
					draggingLeft == false &&
					draggingRight == false)
			{
				// which way have we gone?
				if (dy < 0 && dy < dx) draggingDown = true;
				else if (dy > 0 && dy > dx) draggingUp = true;
				else if (dx < 0) draggingLeft = true;
				else if (dx > 0) draggingRight = true;
			}
			else
			{
				// have we stayed on course?
				if ((draggingDown || draggingUp) && (dx < -80 || dx > 80))
				{
					// cancelled
					startMousePosition = null;
				}
				else if ((draggingLeft || draggingRight) && (dy < -80 || dy > 80))
				{
					// cancelled
					startMousePosition = null;
				}
			}
		}

	}
}
