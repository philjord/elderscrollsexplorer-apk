package com.ingenieur.andyelderscrolls.utils;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static android.R.attr.text;

/**
 * Created by phil on 3/7/2016.
 */
public class SopInterceptor extends PrintStream
{
	private String tag;

	private File logFile = null;

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
					Log.e("LogwritingNew", e.toString());
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
				Log.e("Logwriting", e.toString());
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

	public void setLogFile(File logFile)
	{
		this.logFile = logFile;
	}
}
