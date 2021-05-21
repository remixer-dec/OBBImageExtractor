package ru.remixer_dec.cache_extractor;

import android.text.TextUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

class ObbPackageInfoReplacer {
    private static long calculateOffset(String packageName) {
        return packageName.length() + 12;
    }

    public static void replaceData(String obbFile) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(obbFile, "rw");
        String packageName = Utils.getObbInfo(obbFile).packageName;
        long seekTo = raf.length() - calculateOffset(packageName);
        raf.seek(seekTo);
        ByteBuffer bbuf = ByteBuffer.allocate(4);
        packageName = "ru.remixer_dec.cache_extractor";
        bbuf.order(ByteOrder.LITTLE_ENDIAN);
        bbuf.putInt(packageName.length());
        raf.write(bbuf.array());
        bbuf.rewind();
        raf.writeBytes(packageName);
        bbuf.rewind();
        bbuf.putInt(packageName.length() + 24);
        raf.write(bbuf.array());
        bbuf.rewind();
        bbuf.putInt(17144195);
        raf.write(bbuf.array());
        if (raf.getFilePointer() < raf.length()) {
            raf.setLength(raf.getFilePointer());
            //raf.skipBytes((int) (raf.length() - raf.getFilePointer()));
        }
        raf.close();
    }
    public static void replaceDataInMultipleOBBs(ArrayList<String> obbFiles) {
        for(int i=0, l=obbFiles.size(); i<l; i++) {
            try {
                replaceData(obbFiles.get(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
