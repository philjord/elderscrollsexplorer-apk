package com.ingenieur.andyelderscrolls.utils;

import android.util.Log;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by phil on 3/7/2016.
 */
public class SopInterceptor extends PrintStream
{
	String tag;

	public SopInterceptor(OutputStream out, String tag)
	{
		super(out, true);
		this.tag = tag;
	}

	@Override
	public void print(String s)
	{//do what ever you like
		//super.print(s);
		Log.w(tag, s);
	}
}
