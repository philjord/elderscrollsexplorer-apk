package esmio.tes3;

import java.io.IOException;
import java.nio.channels.FileChannel;

import esmio.common.PluginException;

public class ESMManagerTes3Uri extends ESMManagerTes3 {
	public ESMManagerTes3Uri(FileChannel fileChannel, String fileName) {
		try {
			MasterFC master = new MasterFC(fileChannel, fileName);
			master.load();
			addMaster(master);
		} catch (PluginException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void addMaster(String fileNameToAdd) {
		throw new UnsupportedOperationException();
	}

}
