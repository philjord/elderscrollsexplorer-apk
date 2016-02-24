package com.ingenieur.ese.eseandroid.nifdisplay;

import java.io.File;
import java.io.FilenameFilter;



public class NifKfFileFilter  implements FilenameFilter
{


	public boolean accept(File f)
	{
		return f.getName().endsWith(".nif") || f.getName().endsWith(".kf");
	}


	public String getDescription()
	{
		return "Nif or Kf";
	}

	public boolean accept(File dir, String name)
	{
		return name.endsWith(".nif") || name.endsWith(".kf");
	}

}