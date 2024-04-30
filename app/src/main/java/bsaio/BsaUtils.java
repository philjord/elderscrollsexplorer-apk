package bsaio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.documentfile.provider.DocumentFile;

import org.jogamp.java3d.compressedtexture.CompressedTextureLoader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import bsa.source.BsaTextureSource;
import bsa.source.DDSToKTXBsaConverter;
import etcpack.ETCPack;

public class BsaUtils {

    public static Bitmap getBitmapFromTextureSource(String textureName, BsaTextureSource textureSource) {
        return getBitmapFromTextureSource(textureName, textureSource, false);
    }
    public static Bitmap getBitmapFromTextureSource(String textureName, BsaTextureSource textureSource, boolean invert) {
        if (textureName != null && textureName.length() > 0) {
            InputStream inputStream = textureSource.getInputStream(textureName);
            if(inputStream != null) {
                try {
                    ByteBuffer bb = CompressedTextureLoader.toByteBuffer(inputStream);

                    int[] w = new int[1];
                    int[] h = new int[1];


                    ETCPack ep = new ETCPack();
                    byte[] rawBytes = ep.uncompressImageFromByteBuffer(bb, w, h, true);
                    if (rawBytes != null) {
                        ByteBuffer buffer = ByteBuffer.wrap(rawBytes);
                        int width = w[0];
                        int height = h[0];

                        int[] pixels = new int[width * height];
                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < width; x++) {
                                int y2 = invert ? (height-1) - y : y;
                                //NOTE javadoc on Bitmap.Config.ARGB_8888 says it is actually ABGR!! christ.
                                pixels[(y2 * width) + x] = ((buffer.get() & 0xff) << 24 | (buffer.get() & 0xff) << 0 | (buffer.get() & 0xff) << 8
                                        | (buffer.get() & 0xff) << 16);
                            }
                        }

                        //TODO: handle non A types
                        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(pixels));
                        return bitmap;
                    }
                } catch (IOException e) {
                    System.out.println("" + textureName + " had a  IO problem  : " + e.getMessage());
                }
            }
        }
        return null;
    }


    public static void organiseDDSKTXBSA(Activity parentActivity, DocumentFile rootFolder, BSArchiveSet bsaFileSet, ProgressBar progressBar) {
        //OK time to check that each bsa file that holds dds has a ktx equivalent and drop the dds version
        // or if not to convert the dds to ktx then drop the dds version

        //a list of dds archives that might have a ktx equivalent
        ArrayList<ArchiveFile> ddsBsas = new ArrayList<ArchiveFile>();
        for (ArchiveFile archiveFile : bsaFileSet) {
            if (archiveFile != null && archiveFile.hasDDS()) {
                // we want a archive with the same name but _ktx before the extension holding KTX files
                ddsBsas.add(archiveFile);
            }
        }

        // search for ktx existing and drop the dds if so
        HashMap<String, ArchiveFile> neededBsas = new HashMap<String, ArchiveFile>();
        for (ArchiveFile ddsArchive : ddsBsas) {
            // we want a archive with the same name but _ktx before the extension holding KTX files
            String ddsArchiveName = ddsArchive.getName();
            String ext = ddsArchiveName.substring(ddsArchiveName.lastIndexOf("."));
            String ktxArchiveName = ddsArchiveName.substring(0, ddsArchiveName.lastIndexOf("."));
            ktxArchiveName = ktxArchiveName + "_ktx" + ext;
            boolean found = false;
            for (ArchiveFile ktxArchive : bsaFileSet) {
                //TODO: should see  if it's got ktx in it, but for now let's just prey
                if (ktxArchive != null && ktxArchive.getName().equals(ktxArchiveName)) {
                    found = true;
                    //remove the dds version archive
                    try {
                        ddsArchive.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bsaFileSet.remove(ddsArchive);
                    break;
                }
            }

            if (!found) {
                neededBsas.put(ktxArchiveName, ddsArchive);
            }
        }

        // are there any that might be converted or possibly just run "on the fly"
        if (neededBsas.size() > 0) {

            CountDownLatch waitForAnswer = new CountDownLatch(1);
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        Thread t = new Thread("BSA dds to ktx conversion") {
                            public void run() {
                                for (String ktxArchiveName : neededBsas.keySet()) {
                                    ArchiveFile ddsArchive = neededBsas.get(ktxArchiveName);
                                    String ddsArchiveName = ddsArchive.getName();
                                    //remove the dds version archive either way
                                    try {
                                        ddsArchive.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    bsaFileSet.remove(ddsArchive);
                                    ddsArchive = null;

                                    boolean found = false;
                                    for (ArchiveFile archiveFile : bsaFileSet) {
                                        //TODO: should see  if it's got ktx in it, but for now let's just prey
                                        if (archiveFile != null && archiveFile.getName().equals(ktxArchiveName)) {
                                            found = true;
                                            break;
                                        }
                                    }

                                    if (!found) {
                                        System.out.println("Not found: " + ktxArchiveName + " creating now");

                                        // I need the displayable version to convert so let's load a new copy of ddsArchive
                                        FileInputStream fis;
                                        try {
                                            long tstart2 = System.currentTimeMillis();
                                            DocumentFile ddsDF = rootFolder.findFile(ddsArchiveName);

                                            Uri ddsUri = ddsDF.getUri();
                                            System.out.println("Reloading as in displayable format " + ddsDF.getUri());

                                            ParcelFileDescriptor ddsPFD = parentActivity.getContentResolver().openFileDescriptor(ddsUri, "r");
                                            fis = new ParcelFileDescriptor.AutoCloseInputStream(ddsPFD);
                                            ArchiveFile archiveFile = ArchiveFile.createArchiveFile(fis.getChannel(), ddsArchiveName);
                                            archiveFile.load(true);//blocking call
                                            System.out.println("loaded as displayable " + ddsUri + " in " + (System.currentTimeMillis() - tstart2));
                                            //converting
                                            final long tstart = System.currentTimeMillis();
                                            // find it
                                            DocumentFile ktxDF = rootFolder.findFile(ktxArchiveName);
                                            // or create it (if not found)
                                            if (ktxDF == null) {
                                                ktxDF = rootFolder.createFile("application/octet-stream", ktxArchiveName);
                                            }

                                            ParcelFileDescriptor ktxPFD = parentActivity.getContentResolver().openFileDescriptor(ktxDF.getUri(), "rw");
                                            FileOutputStream fos = new ParcelFileDescriptor.AutoCloseOutputStream(ktxPFD);
                                            FileInputStream fisKtx = new ParcelFileDescriptor.AutoCloseInputStream(ktxPFD);

                                            //DO NOT delete file as it is hopefully a restartable //->fos.getChannel().truncate(0);//in case the file already exists somehow, this is a delete type action
                                            DDSToKTXBsaConverter.StatusUpdateListener sul = new DDSToKTXBsaConverter.StatusUpdateListener() {
                                                public void updateProgress(int currentProgress) {
                                                    parentActivity.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            progressBar.setProgress(currentProgress);
                                                            System.out.println("CurrentProgress " + currentProgress + "%  in " + (System.currentTimeMillis() - tstart) + "ms for " + ktxArchiveName);
                                                        }
                                                    });
                                                }
                                            };


                                            DDSToKTXBsaConverter convert = new DDSToKTXBsaConverter(fos.getChannel(), fisKtx.getChannel(), archiveFile, sul);
                                            System.out.println("Converting " + ddsArchiveName + " to ktx version, this may take ages!");
                                            parentActivity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    //screen still sleeps just after a long time, CPU processing appears to continue anyway
                                                    parentActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                                    progressBar.setIndeterminate(false);
                                                    progressBar.setVisibility(ProgressBar.VISIBLE);
                                                    progressBar.setProgress(0);
                                                    progressBar.setMax(100);
                                                }
                                            });

                                            convert.start();
                                            try {
                                                convert.join();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }

                                            parentActivity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    parentActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                                    progressBar.setVisibility(ProgressBar.GONE);
                                                }
                                            });
                                            System.out.println("" + (System.currentTimeMillis() - tstart) + "ms to compress " + ktxArchiveName);
                                            // have to re locate it for some reason to load it
                                            ktxDF = rootFolder.findFile(ktxArchiveName);
                                            ktxPFD = parentActivity.getContentResolver().openFileDescriptor(ktxDF.getUri(), "r");
                                            // now load that newly created file into the system
                                            fis = new ParcelFileDescriptor.AutoCloseInputStream(ktxPFD);
                                            bsaFileSet.loadFileAndWait(fis.getChannel(), ktxArchiveName);

                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (DBException e1) {
                                            e1.printStackTrace();
                                        } catch (IOException e1) {
                                            e1.printStackTrace();
                                        }

                                    }
                                }
                                waitForAnswer.countDown();
                            }
                        };
                        t.start();
                    } else {
                        waitForAnswer.countDown();
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    builder.setMessage("Convert " + neededBsas.size() + " bsa files from dds to ktx, this may take ages...").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }
            });

            try {
                waitForAnswer.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
