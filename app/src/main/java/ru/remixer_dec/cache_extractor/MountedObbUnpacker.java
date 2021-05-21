package ru.remixer_dec.cache_extractor;

import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

class BGTask implements Runnable {
    private String mObbPath;
    private String destPath;
    private String obbFolderName;
    private String folder;
    private String rawObbPath;
    public BGTask(String aObbPath, String aDestPath, String aObbFolderName, String aFolder, String aRawObbPath){
        mObbPath = aObbPath;
        rawObbPath = aRawObbPath;
        destPath = aDestPath;
        obbFolderName = aObbFolderName;
        folder = aFolder;
    }
    public void run() {
        try {
            if (FirstFragment.compatibility.isChecked()) {
                MountedObbUnpacker.copyDirectory(new File(mObbPath), new File(destPath + "/" + folder));
            } else {
                Runtime.getRuntime().exec("cp -r -R " + mObbPath + "/ " + destPath + "/").waitFor();
            }
            Runtime.getRuntime().exec("mv " + destPath + "/" +
                    obbFolderName + "/ " + destPath + "/" + folder + "/").waitFor();
            MountedObbUnpacker.om.unmountObbFile(rawObbPath);
            Thread.sleep(250);
            Runtime.getRuntime().exec("rm " + rawObbPath).waitFor();
                FirstFragment.activity.runOnUiThread(() -> {
                    if (MountedObbUnpacker.errorCount == 0) {
                        MountedObbUnpacker.completeCount++;
                    FirstFragment.status.setText(FirstFragment.activity.getApplicationContext().getString(R.string.unpackSuccess,
                            MountedObbUnpacker.completeCount, MountedObbUnpacker.fileCount));
                        if (MountedObbUnpacker.completeCount == MountedObbUnpacker.fileCount) {
                            FirstFragment.pbar.setVisibility(View.INVISIBLE);
                        }
                    }
                    else {
                        FirstFragment.status.setText(R.string.unpackError);
                        FirstFragment.pbar.setVisibility(View.INVISIBLE);
                    }
                });
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            MountedObbUnpacker.errorCount++;
        }
    }
}

class MountedObbUnpacker {
    public static int errorCount = 0;
    public static int fileCount;
    public static int completeCount;
    public static ObbMounter om;
    public static boolean unpack(String mObbPath, String destPath, String folder, String rawObbPath) {
        FirstFragment.status.setText(R.string.unpackingInProgress + mObbPath + ")");
        String[] splits = mObbPath.split("/");
        String obbFolderName = splits[splits.length-1];
        Thread thread = new Thread(new BGTask(mObbPath, destPath, obbFolderName, folder, rawObbPath));
        thread.start();
        return true;
    }

    public static void unpackMultiple(ArrayList<String> mountedPaths, ObbMounter mounter, String destPath) {
        om = mounter;
        fileCount = mountedPaths.size();
        completeCount = 0;
        for (int i=0; i<fileCount; i++) {
            String rawObbPath = mountedPaths.get(i);
            String mountedObbPath = mounter.getMountedObbPath(rawObbPath);
            String fileName = Utils.getObbInfo(rawObbPath).fileName;
            String firstName = fileName.split("[.]")[0];
            unpack(mountedObbPath, destPath, firstName, rawObbPath);
        }

        FirstFragment.status.setText(R.string.backgroundWorkerActive);
        FirstFragment.pbar.setVisibility(View.VISIBLE);

    }
    public static void copyDirectory(File sourceLocation , File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
            }

            String[] children = sourceLocation.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            // make sure the directory we plan to store the recording in exists
            File directory = targetLocation.getParentFile();
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw new IOException("Cannot create dir " + directory.getAbsolutePath());
            }

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }
}
