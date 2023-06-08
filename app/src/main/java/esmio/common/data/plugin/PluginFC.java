package esmio.common.data.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.zip.DataFormatException;

import esmio.common.PluginException;
import tools.io.FileChannelRAF;

/**
 * For desktop to swap File path strings into ByteBuffer or randomaccessfile or io streams Android will have the same
 * for SAF Uri starting points
 */

public class PluginFC extends Plugin {
	private FileChannel pluginFile;

	public PluginFC(FileChannel masterFile, String fileName) {
		super(fileName);
		this.pluginFile = pluginFile;
	}

	/**
	 * This method assumes an index only version is required
	 * @throws PluginException
	 * @throws DataFormatException
	 * @throws IOException
	 */
	@Override
	public void load() throws PluginException, DataFormatException, IOException {
		load(true);
	}

	@Override
	public void load(boolean indexCellsOnly) throws PluginException, DataFormatException, IOException {
		//in = new RandomAccessFile(pluginFile, "r");
		in = new FileChannelRAF(pluginFile, "r");
		super.load(indexCellsOnly, in);
	}
}
