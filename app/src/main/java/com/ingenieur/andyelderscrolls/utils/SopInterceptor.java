package com.ingenieur.andyelderscrolls.utils;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Calendar;

/**
 * Created by phil on 3/7/2016.
 */
public class SopInterceptor extends PrintStream
{
	private String tag;

	// note static anyone can set this guy and it is used by all
	private static File logFile = null;

	public SopInterceptor(OutputStream out, String tag)
	{
		super(out, true);
		this.tag = tag;
	}

	@Override
	public void print(String s)
	{
		Log.w(tag, s);

		if (logFile != null && s.trim().length() > 0)
		{
			if (!logFile.exists())
			{
				try
				{
					logFile.createNewFile();
				}
				catch (IOException e)
				{
					// could use String st = Log.getStackTraceString(e)
					Log.e("LogWritingNew", e.toString());
				}
			}
			BufferedWriter buf = null;
			try
			{
				//BufferedWriter for performance, true to set append to file flag
				buf = new BufferedWriter(new FileWriter(logFile, true));
				buf.append(header()+tag+ " " + s);
				buf.newLine();
				buf.flush();
			}
			catch (IOException e)
			{
				Log.e("LogWriting", e.toString());
			}
			finally
			{
				try
				{
					if (buf != null) buf.close();
				}
				catch (IOException e)
				{//ignore
				}
			}
		}
	}

	private String header()
	{
		return "TIME: "+java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()) +
				 " PID: "+
				android.os.Process.myPid()+" ";
	}

	public static void setLogFile(File f)
	{
		logFile = f;
	}
}
