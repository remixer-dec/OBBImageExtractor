package ru.remixer_dec.cache_extractor;

import android.view.View;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


class UnzipBGTask implements Runnable {
    private ArrayList<String> sourceList;
    private String destination;

    public UnzipBGTask(ArrayList src, String dest) {
        sourceList = src;
        destination = dest;
    }

    public void run() {
        for (int i=0, l=sourceList.size(); i<l; i++) {
            try {
                String path = sourceList.get(i);
                String outputPath = destination + "/" + Utils.getObbInfo(path).fileName + "/";
                Runtime.getRuntime().exec("mkdir " + Utils.safeSpace(outputPath)).waitFor();
                if (FirstFragment.compatibility.isChecked()) {
                    String errorLog = ZipUnpacker.unzip(new File(path), outputPath);
                    if (!errorLog.equals("")) {
                        throw new IOException(errorLog);
                    }
                } else {
                    File unzip = new File("/system/bin/unzip");
                    Boolean unzipAvailable = unzip.exists() && unzip.canExecute();
                    File busybox = new File("/system/bin/busybox");
                    Boolean busyboxAvailable = busybox.exists() && busybox.canExecute();
                    String binary = unzipAvailable ? "unzip " :  busyboxAvailable ? "busybox unzip " : "NO";
                    if (binary.equals("NO")) {
                        throw new IOException("unzip binary not found");
                    }
                    String cmd = binary + Utils.safeSpace(path) + " -d " + Utils.safeSpace(outputPath);
                    Runtime.getRuntime().exec(cmd).waitFor();
                }
                int completed = i + 1;

                FirstFragment.activity.runOnUiThread(() -> {
                    FirstFragment.status.setText(FirstFragment.activity.getApplicationContext().getString(
                            R.string.unpackSuccess,
                            completed, l)
                    );
                });
                Runtime.getRuntime().exec("rm " + path).waitFor();

            } catch (IOException | InterruptedException e) {
                FirstFragment.activity.runOnUiThread(() -> {
                    FirstFragment.status.setText(
                            FirstFragment.activity.getText(R.string.unpackError) + "\n" + e.toString()
                    );
                });
            }
        }

        FirstFragment.activity.runOnUiThread(() -> {
            FirstFragment.pbar.setVisibility(View.INVISIBLE);
        });
    }
}

class ZipUnpacker {
    public static void unzipMultiple(ArrayList<String> files, String targetDir) {
        FirstFragment.status.setText(R.string.backgroundWorkerActive);
        FirstFragment.pbar.setVisibility(View.VISIBLE);
        Thread thread = new Thread(new UnzipBGTask(files, targetDir));
        thread.start();
    }

    //https://stackoverflow.com/a/6728732 <- the best unzipping algorithm, really hard to find
    public static String unzip(File file, String destination) throws IOException {
            ZipFile zipfile = new ZipFile(file);
            String errorLog = "";
            for (Enumeration e = zipfile.entries(); e.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                try {
                        unzipEntry(zipfile, entry, destination);
                } catch (IOException err) {
                        errorLog += err.toString() + "\n";
                }
            }
            return errorLog;
    }

    private static void unzipEntry(ZipFile zipfile, ZipEntry entry,
                            String outputDir) throws IOException {

        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }


        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            copyStream(inputStream, outputStream);
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }

    private static void createDir(File dir) {
        if (dir.exists()) {
            return;
        }

        if (!dir.mkdirs()) {
            throw new RuntimeException("Can not create dir " + dir);
        }
    }

    private static void copyStream(BufferedInputStream input, BufferedOutputStream output)
            throws IOException
    {
        byte[] buffer = new byte[1024]; // Adjust if you want
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, bytesRead);
        }
    }
}
