package com.ingenieur.andyelderscrolls.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import static android.R.attr.path;

public class FileChooser implements DialogInterface.OnDismissListener
{
	private static final String PARENT_DIR = "..";

	private final Activity activity;
	private ListView list;
	private Dialog dialog;
	private File currentPath;

	// filter on file extension
	private String extension = null;

	public FileChooser setExtension(String extension)
	{
		this.extension = (extension == null) ? null :
				extension.toLowerCase();
		refresh(currentPath);
		return this;
	}

	// file selection event handling
	public interface FileSelectedListener
	{
		void fileSelected(File file);

		void folderSelected(File file);
	}

	public FileChooser setFileListener(FileSelectedListener fileListener)
	{
		this.fileListener = fileListener;
		return this;
	}

	private FileSelectedListener fileListener;

	public FileChooser(Activity activity)
	{
		this(activity, null);
	}

	public FileChooser(Activity activity, File startFolder)
	{
		// is the start folder a extant file/folder? if not see if a parent can be found that is
		if( !startFolder.exists())
		{
			startFolder = startFolder.getParentFile();
			while(!startFolder.exists())
				startFolder = startFolder.getParentFile();
		}


		this.activity = activity;
		dialog = new Dialog(activity);

		dialog.setOnDismissListener(this);

		list = new ListView(activity);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int which, long id)
			{
				String fileChosen = (String) list.getItemAtPosition(which);
				File chosenFile = getChosenFile(fileChosen);
				if (chosenFile.isDirectory())
				{
					refresh(chosenFile);
					fileListener.folderSelected(chosenFile);
					//TODO: must make a button for "select" but for now just click away will do
				}
				else
				{
					if (fileListener != null)
					{
						fileListener.fileSelected(chosenFile);
					}
					dialog.dismiss();
				}
			}
		});
		dialog.setContentView(list);
		dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		if (startFolder == null)
		{
			//this doesn't work no navigate from here
			//startFolder = Environment.getExternalStorageDirectory();

			File chooserStartFolder = new File( System.getenv("EXTERNAL_STORAGE"));
		}
		else if (startFolder.isFile())
		{
			startFolder = startFolder.getParentFile();
		}
		currentPath = startFolder;
	}


	public void showDialog()
	{
		refresh(currentPath);
		dialog.show();
	}

	/**
	 * Override to listen for dismissal
	 *
	 * @param dialogInterface
	 */
	@Override
	public void onDismiss(DialogInterface dialogInterface)
	{

	}

	/**
	 * Sort, filter and display the files for the given path.
	 */
	private void refresh(File path)
	{
		this.currentPath = path;
		if (path.exists())
		{
			File[] dirs = path.listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File file)
				{
					return (file.isDirectory() && file.canRead());
				}
			});
			File[] files = path.listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File file)
				{
					if (!file.isDirectory())
					{
						if (!file.canRead())
						{
							return false;
						}
						else if (extension == null)
						{
							return true;
						}
						else
						{
							return file.getName().toLowerCase().endsWith(extension);
						}
					}
					else
					{
						return false;
					}
				}
			});

			// convert to an array
			int i = 0;
			String[] fileList;
			if (path.getParentFile() == null)
			{
				fileList = new String[dirs.length + files.length];
			}
			else
			{
				fileList = new String[dirs.length + files.length + 1];
				fileList[i++] = PARENT_DIR;
			}
			Arrays.sort(dirs);
			Arrays.sort(files);
			for (File dir : dirs)
			{
				fileList[i++] = dir.getName();
			}
			for (File file : files)
			{
				fileList[i++] = file.getName();
			}

			// refresh the user interface
			dialog.setTitle(currentPath.getPath());
			list.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, fileList)
			{
				@Override
				public View getView(int pos, View view, ViewGroup parent)
				{
					view = super.getView(pos, view, parent);
					((TextView) view).setSingleLine(true);
					return view;
				}
			});
		}
	}


	/**
	 * Convert a relative filename into an actual File object.
	 */
	private File getChosenFile(String fileChosen)
	{
		if (fileChosen.equals(PARENT_DIR))
		{
			return currentPath.getParentFile();
		}
		else
		{
			return new File(currentPath, fileChosen);
		}
	}
}