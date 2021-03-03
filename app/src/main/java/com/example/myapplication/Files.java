package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Files {

    public static byte[] loadFile(String path) {

        File file = new File(path);
        int size = (int) file.length();
        byte[] fileArray = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(fileArray, 0, fileArray.length);
            buf.close();
            return fileArray;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean saveFile(byte[] fileArray, String path) {
        try {
            File folder = new File(Environment.getExternalStorageDirectory(), "WifiDirect");
            if(!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(path);
            stream.write(fileArray);
            stream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
