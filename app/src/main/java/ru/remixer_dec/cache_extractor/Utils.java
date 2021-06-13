package ru.remixer_dec.cache_extractor;

import android.text.TextUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

class ObbInfo {
    public String packageName;
    public String fileName;
    public String path;
    public ObbInfo(String obbPath) {
        String[] splits = obbPath.split("/");
        fileName = splits[splits.length-1];
        path = obbPath;
        packageName = getPackageName(obbPath);
    }
    private String getPackageName(String packagePath) {
        String[] pathSplits = packagePath.split("/");
        String[] splits = pathSplits[pathSplits.length - 1].split("[.]");
        String pkgname;
        try {
            pkgname = TextUtils.join(".", Arrays.copyOfRange(splits, 2, splits.length-1));
        } catch (Exception e) {
            pkgname = pathSplits[pathSplits.length - 2].split(".").length > 0 ?
                    pathSplits[pathSplits.length - 2] : "com.unknown.package";
        }
        return pkgname;
    }
}

class Utils {
    public static int getObbFileType(String path) {
        try {
            RandomAccessFile obbFile = new RandomAccessFile(path,"r");
            byte[] jobbImageHeader = {(byte)0xeb,(byte)0x3c};
            byte[] zipArchiveHeader= {(byte)0x50,(byte)0x4b};
            byte[] freshByteBuffer = new byte[2];
            obbFile.read(freshByteBuffer);
            if (Arrays.equals(jobbImageHeader, freshByteBuffer)) {
                return R.string.jobbImage;
            } else if (Arrays.equals(zipArchiveHeader, freshByteBuffer)) {
                return R.string.zipFolder;
            }
        } catch (IOException ignored) { }
        return R.string.unknown;

    }
    public static ObbInfo getObbInfo(String path) {
        return new ObbInfo(path);
    }
    public static String safeSpace(String src) {
        //doesn't really help, looking for alternative
        return src.replaceAll("[ ]","\\ ");
    }
}
