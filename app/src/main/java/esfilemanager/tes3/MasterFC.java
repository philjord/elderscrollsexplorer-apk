package esfilemanager.tes3;

import java.io.IOException;
import java.nio.channels.FileChannel;

import esmio.tes3.Master;
import tools.io.FileChannelRAF;

/**
 * This is a copy of the master file in data package, however it holds onto a copy of all loaded data for everything
 * other than the WRLD and CELL values, which is simply indexes down to the subblock level
 *
 * @author Administrator
 *
 */
public class MasterFC extends Master {
	public MasterFC(FileChannel masterFile, String fileName) throws IOException {
		super(new FileChannelRAF(masterFile, "r"), fileName);
	}

}
