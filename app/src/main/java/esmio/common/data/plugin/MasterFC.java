package esmio.common.data.plugin;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.zip.DataFormatException;

import esmio.common.PluginException;
import esmio.loader.ESMManager;
import tools.io.FileChannelRAF;

/**
 * For desktop to swap File path strings into ByteBuffer or randomaccessfile or io streams Android will have the same
 * for SAF Uri starting points
 */
public class MasterFC extends Master {

	private FileChannel masterFile;

	public MasterFC(FileChannel masterFile, String fileName) {
		super(fileName);
		this.masterFile = masterFile;
	}

	@Override
	public boolean load() throws PluginException, DataFormatException, IOException {

		FileChannelRAF in;
		if (masterFile.size() > Integer.MAX_VALUE || !ESMManager.USE_FILE_MAPS)
			in = new FileChannelRAF(masterFile, "r");
		else
			in = new FileChannelRAF(masterFile, "r");

		return super.load(in);

	}
}
