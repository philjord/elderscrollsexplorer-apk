package bsaio;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.documentfile.provider.DocumentFile;

import com.ingenieur.andyelderscrolls.andyesexplorer.ScrollsExplorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import esmio.loader.ESMManagerUri;

public class BSArchiveSetUri extends BSArchiveSet {
    /**
     * If the root file is not a folder, it is assumed to be the esm file and so it's parent folder is used as a folder
     * of resources will load all bsa files and check for resource sub folders
     *
     * @param rootFilename
     * @param folderOfResources
     */
    public BSArchiveSetUri(Context context, String rootFilename, boolean folderOfResources, boolean isForDisplay) {
        this(context, new String[]{rootFilename}, folderOfResources, isForDisplay);
    }

    public BSArchiveSetUri(Context context, String[] rootFilenames, boolean folderOfResources, boolean isForDisplay) {
        super(isForDisplay);
        long start = System.currentTimeMillis();
        for (String rootFilename : rootFilenames) {
            try {
                if (folderOfResources) {
                    DocumentFile rootFolder = DocumentFile.fromTreeUri(context, Uri.parse(rootFilename));

                    for (DocumentFile bsaDF : rootFolder.listFiles()) {
                        ParcelFileDescriptor pfdInput = context.getContentResolver().openFileDescriptor(bsaDF.getUri(), "r");
                        //https://stackoverflow.com/questions/44530136/read-failed-ebadf-bad-file-descriptor-while-reading-from-inputstream-nougat
                        //The problem is with ParcelFileDescriptor. It closes the input stream.
                        //https://developer.android.com/reference/android/os/ParcelFileDescriptor.AutoCloseInputStream
                        FileInputStream fis = new ParcelFileDescriptor.AutoCloseInputStream(pfdInput);
                        //FileInputStream fis = new FileInputStream(pfdInput.getFileDescriptor());
                        FileChannel fileChannel = fis.getChannel();
                        if (bsaDF.getName().toLowerCase().endsWith(".bsa") //
                                || bsaDF.getName().toLowerCase().endsWith(".ba2")//
                                || bsaDF.getName().toLowerCase().endsWith(".obb")) //android expansion file name
                        {
                            loadFile(fileChannel, bsaDF.getName());
                        }
                    }
                } else {
                    DocumentFile bsaDF = DocumentFile.fromSingleUri(context, Uri.parse(rootFilename));
                    //ParcelFileDescriptor pfdInput = context.getContentResolver().openFileDescriptor(bsaDF.getUri(), "r");

                    FileInputStream fis = (FileInputStream) context.getContentResolver().openInputStream(Uri.parse(rootFilename));//new FileInputStream(pfdInput.getFileDescriptor());
                    FileChannel fileChannel = fis.getChannel();
                    if (bsaDF.getName().toLowerCase().endsWith(".bsa") //
                            || bsaDF.getName().toLowerCase().endsWith(".ba2")//
                            || bsaDF.getName().toLowerCase().endsWith(".obb")//
                    ) {
                        loadFile(fileChannel, bsaDF.getName());
                    } else {
                        System.out.println("BSAFileSet bad non sibling load of " + rootFilename);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (Thread loadTask : loadThreads) {
            try {
                loadTask.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(
                "BSAFileSet (" + loadThreads.size() + ") completely loaded in " + (System.currentTimeMillis() - start));

        loadThreads.clear();

        if (this.size() == 0) {
            System.out.println("BSAFileSet loaded no files using root: " + rootFilenames[0]);
        }
    }


}
