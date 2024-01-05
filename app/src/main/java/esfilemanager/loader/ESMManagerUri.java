package esfilemanager.loader;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.DataFormatException;

import esfilemanager.common.data.plugin.MasterFC;
import esfilemanager.common.PluginException;
import esfilemanager.common.data.plugin.Master;
import esfilemanager.loader.ESMManager;
import esfilemanager.loader.IESMManager;
import esfilemanager.tes3.ESMManagerTes3Uri;

/** For desktop to swap File path strings into ByteBuffer or randomaccessfile or io streams
 Android will have the same for SAF Uri starting points
*/
public class ESMManagerUri extends ESMManager {
	public ESMManagerUri(FileChannel fileChannel, String fileName) {
		try {
			Master master = new MasterFC(fileChannel, fileName);
			master.load();
			addMaster(master);
		} catch (PluginException e1) {
			e1.printStackTrace();
		} catch (DataFormatException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public synchronized void addMaster(String fileNameToAdd) {
		throw new UnsupportedOperationException();
	}

	public static IESMManager getESMManager(Context context, Uri fileUri) {
		try {
			ParcelFileDescriptor pfdInput = context.getContentResolver().openFileDescriptor(fileUri, "r");
			//https://stackoverflow.com/questions/44530136/read-failed-ebadf-bad-file-descriptor-while-reading-from-inputstream-nougat
			//The problem is with ParcelFileDescriptor. It closes the input stream.
			//https://developer.android.com/reference/android/os/ParcelFileDescriptor.AutoCloseInputStream
			FileInputStream fis = new ParcelFileDescriptor.AutoCloseInputStream(pfdInput);
			//FileInputStream fis = new FileInputStream(pfdInput.getFileDescriptor());
			FileChannel fileChannel = fis.getChannel();
			try {
				byte[] prefix = new byte[16];
				int count = fileChannel.read(ByteBuffer.wrap(prefix));
				fileChannel.position(fileChannel.position()-16);
				if (count == 16) {
					String recordType = new String(prefix, 0, 4);
					if (recordType.equals("TES3")) {
						return new ESMManagerTes3Uri(fileChannel, fileUri.getLastPathSegment());
					}
				}
			}
			catch (IOException e) {
				//fall through, try tes4
			}

			//assume tes4
			return new ESMManagerUri(fileChannel, fileUri.getLastPathSegment());

		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}
