package mysite.com.dribbbleshow.AppUtils;


import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mysite.com.dribbbleshow.AppController;


public class FileIO {

    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat _dateFormat = new SimpleDateFormat(
            "MM_dd_yyyy_HH_mm_ss");

    public static String mRootSubfolder = AppController.ROOT_SUBFOLDER;

    public static String GetExternalFilesDir() {
        String result = "";
        try {
            File file = Environment.getExternalStorageDirectory();
            result = file.getAbsolutePath();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return (result);
    }

    public static String GetRootSubfolder() {
        String result = "";
        try {
            if ((mRootSubfolder == null) || (mRootSubfolder.isEmpty() == true)) {
                mRootSubfolder = AppController.ROOT_SUBFOLDER;
            }
            result = GetExternalFilesDir() + "/" + mRootSubfolder;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return (result);
    }

    public static String GetShotsSubfolder() {
        String result = "";
        try {
            result = GetRootSubfolder() + "/" + AppController.SHOTS_SUBFOLDER;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return (result);
    }

    @SuppressWarnings("unchecked")
    public static List<File> GetFiles(String subfolder)
    {
        List<File> list = new ArrayList<File>();
        try {
            File file = new File(subfolder);
            if (file != null) {
                File[] files = file.listFiles();
                if ((files != null) && (files.length > 0)) {
                    List<SortedFileModifyDate> tempList = new ArrayList<SortedFileModifyDate>();
                    for (File tempFile : files) {
                        if (tempFile.isDirectory() == false) {
                            tempList.add(new SortedFileModifyDate(tempFile));
                        }
                    }

                    if (tempList.size() >= 1) {
                        Collections.sort(tempList);
                        for(SortedFileModifyDate sfmd : tempList) {
                            list.add(sfmd.GetFile());
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return (list);
    }

    public static File Exists(String folder, String filename) {

        if ((filename != null) && (filename.isEmpty() == false)) {

            try {

                folder = Utils.ValidateString(folder);
                File file = new File(folder, filename);
                if(file.exists()){
                    return file;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public static boolean CreateSubfolder(String folder) {
        boolean result = false;
        try {
            folder = Utils.ValidateString(folder);
            File dir = new File(folder);
            if (dir.isDirectory() == false) {
                dir.mkdirs();
            }
            result = (dir.isDirectory() == true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return (result);
    }

    public static boolean Delete(String folder, String filename) {
        boolean result = false;
        try {
            if (Exists(folder, filename) != null) {
                folder = Utils.ValidateString(folder);
                Log.d("FileIO.Delete", "folder = \"" + folder + "\"\r\n    filename = \"" + filename + "\"");
                File file = new File(folder, filename);
                result = file.delete();
                if (Exists(folder, filename) != null) {
                    Log.d("FileIO.Delete","Unable to delete \"" + filename + "\"");
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return (result);
    }


}
