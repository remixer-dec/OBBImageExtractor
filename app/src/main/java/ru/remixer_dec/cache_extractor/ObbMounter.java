package ru.remixer_dec.cache_extractor;

import android.content.Context;
import android.os.Environment;
import android.os.storage.OnObbStateChangeListener;
import android.os.storage.StorageManager;

import java.io.IOException;
import java.util.ArrayList;

interface OnAllMountedListener {
    void onSuccess(ArrayList<String> mountedPaths);
    void onError(int errors, int errorCode);
}

class ObbMounter {
    private StorageManager smanager;
    private OnAllMountedListener allMounted;
    private int totalFiles = 2;
    private int totalErrors = 0;
    private int lastError = 0;
    private ArrayList<String> mountedPaths = new ArrayList<>();
    public void init(Context ctx, OnAllMountedListener allMounted) {
        this.smanager = (StorageManager) ctx.getSystemService(Context.STORAGE_SERVICE);
        this.allMounted = allMounted;
        this.totalErrors = 0;
        this.lastError = 0;
    }
    public String getMountedObbPath(String rawPath) {
        return smanager.getMountedObbPath(rawPath);
    }
    private String moveObb(String path, String targetPath) throws IOException, InterruptedException {
        String[] splits = path.split("/");
        String filename = splits[splits.length-1];
        Runtime.getRuntime().exec("mkdir " + targetPath).waitFor();
        Runtime.getRuntime().exec("mv " + path + " " + targetPath).waitFor();
        return targetPath + filename;
    }
    public void unmountObbFile(String path) {
        smanager.unmountObb(path, true, new OnObbStateChangeListener(){});
    }
    public void mountObbFile(String path) {
        try {
            path = moveObb(path, Environment.getExternalStorageDirectory() + "/" +
                    "Android/obb/" + BuildConfig.APPLICATION_ID + "/");
            Thread.sleep(120);
        } catch (Exception ignored) { }
        this.smanager.mountObb(path, null, new OnObbStateChangeListener() {
            @Override
            public void onObbStateChange(String path, int state) {
               if (state == 1 || state == 24) {
                   mountedPaths.add(path);
                   if (totalErrors != 0) {
                       allMounted.onError(totalErrors, lastError);
                       return;
                   }
                   if (mountedPaths.size() == totalFiles) {
                       allMounted.onSuccess(mountedPaths);
                   }
               } else {
                   //error
                   lastError = state;
                   totalErrors++;
                   if (totalErrors != 0 && totalErrors + mountedPaths.size() == totalFiles) {
                       allMounted.onError(totalErrors, lastError);
                   }
               }
            }
        });
    }
    public void mountMultipleOBBs(ArrayList<String> paths) {
        this.totalFiles = paths.size();
        for (int i=0; i<paths.size(); i++) {
            mountObbFile(paths.get(i));
        }
    }
}
