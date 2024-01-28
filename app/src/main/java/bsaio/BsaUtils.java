package bsaio;

import android.graphics.Bitmap;

import org.jogamp.java3d.compressedtexture.CompressedTextureLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import bsa.source.BsaTextureSource;
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
}
