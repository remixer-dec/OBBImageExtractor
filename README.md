## OBB Image Extractor
This android app can help you extract data from OBB images right on your phone.
Both [JOBB](https://developer.android.com/studio/command-line/jobb "JOBB")-generated images and ZIP archives are supported.  
![Screenshot](https://i.imgur.com/tnyoDm8.png)  
Originally created to help to run apps with OBB-mounting, which doesn't work on most Android VM/Container apps. Use this app to extract cache and then, modify your target app to use extracted cache, instead of mounting OBB image, to run it on a VM/Container.  

#### How it works for (for non-zip images):
1) Target obb image is modified. App package name is replaced with extractor package name.  
2) Target obb is placed in /Android/obb/... (this app's obb folder)  
3) Target obb is mounted with Android's [StorageManager.mountObb](https://developer.android.com/reference/android/os/storage/StorageManager#mountObb(java.lang.String,%20java.lang.String,%20android.os.storage.OnObbStateChangeListener) "StorageManager.mountObb")  
4) The app copies files from mounted image to selected output folder.  