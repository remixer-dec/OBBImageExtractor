package ru.remixer_dec.cache_extractor;

import android.text.TextUtils;

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
        return TextUtils.join(".", Arrays.copyOfRange(splits, 2, splits.length-1));
    }
}

class Utils {
    public static ObbInfo getObbInfo(String path) {
        return new ObbInfo(path);
    }
}
