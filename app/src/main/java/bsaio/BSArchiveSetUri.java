package bsaio;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.documentfile.provider.DocumentFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class BSArchiveSetUri extends BSArchiveSet {
    /**
     * This is a BLOCKING load constructor, so it will take a long time potentially, like 10 seconds or more
     * If the root file is not a folder,it will be loaded as a single ton otherwise all bsa files in it are loaded
     *
     * @param context
     * @param rootFilename
     * @param isForDisplay
     */
    public BSArchiveSetUri(Context context, String rootFilename, boolean isForDisplay) {
        this(context, new String[]{rootFilename}, isForDisplay);
    }

    /**
     * This is a BLOCKING load constructor, so it will take a long time potentially, like 10 seconds or more
     *
     * @param context
     * @param rootFilenames
     * @param isForDisplay
     */
    public BSArchiveSetUri(Context context, String[] rootFilenames, boolean isForDisplay) {
        super(isForDisplay);
        long start = System.currentTimeMillis();
        for (String rootFilename : rootFilenames) {
            try {
                //TODO: how do I know what sort of uri Ive got? folder or file
                boolean guessFolder = true;
                if (rootFilename.indexOf(".bsa") > 0
                        || rootFilename.indexOf(".ba2") > 0)
                    guessFolder = false;

                if (guessFolder) {
                    DocumentFile rootFolder = DocumentFile.fromTreeUri(context, Uri.parse(rootFilename));

                    for (DocumentFile bsaDF : rootFolder.listFiles()) {
                        if (bsaDF.getName().toLowerCase().endsWith(".bsa") //
                                || bsaDF.getName().toLowerCase().endsWith(".ba2")) {
                            ParcelFileDescriptor pfdInput = context.getContentResolver().openFileDescriptor(bsaDF.getUri(), "r");
                            //https://stackoverflow.com/questions/44530136/read-failed-ebadf-bad-file-descriptor-while-reading-from-inputstream-nougat
                            //The problem is with ParcelFileDescriptor. It closes the input stream. so instead we use
                            //https://developer.android.com/reference/android/os/ParcelFileDescriptor.AutoCloseInputStream
                            FileInputStream fis = new ParcelFileDescriptor.AutoCloseInputStream(pfdInput);
                            FileChannel fileChannel = fis.getChannel();
                            loadFile(fileChannel, bsaDF.getName());
                        }
                    }

                } else {
                    DocumentFile bsaDF = DocumentFile.fromSingleUri(context, Uri.parse(rootFilename));
                    if (bsaDF.getName().toLowerCase().endsWith(".bsa") //
                            || bsaDF.getName().toLowerCase().endsWith(".ba2")) {
                        ParcelFileDescriptor pfdInput = context.getContentResolver().openFileDescriptor(bsaDF.getUri(), "r");
                        FileInputStream fis = new ParcelFileDescriptor.AutoCloseInputStream(pfdInput);
                        FileChannel fileChannel = fis.getChannel();
                        loadFile(fileChannel, bsaDF.getName());
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //this load call is a blocking call so now we wait for all threads to finish up
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
