package com.example.myapplication;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import javax.net.ssl.HttpsURLConnection;

public class Downloader extends Thread {

    private String link;
    private String fileName;
    private String filePath;
    private boolean firstHalf;
    private Context context;
    private boolean done;
    private boolean successful;

    public Downloader(String link, boolean firstHalf, Context context) {
        this.link = link;
        this.firstHalf = firstHalf;
        this.context = context;
        fileName = link.substring(link.lastIndexOf("/") + 1);
        filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WifiDirect/" + fileName;
        done = false;
        successful = false;
    }

    @Override
    public void run() {

        if(firstHalf) {
            downloadFirstHalf();
        } else {
            downloadSecondHalf();
        }

        done = true;

    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isDone() {
        return done;
    }

    public boolean isSuccessful() {
        return successful;
    }

    private void downloadFirstHalf() {
        boolean isFile = false;
        if(link.startsWith("https://")){
            if(fileName.contains(".")){
                isFile = true;
            }
        }else{
            Toast.makeText(context, "not Valid Url", Toast.LENGTH_SHORT).show();
        }

        if(isFile){
            try {
                URL url = new URL(link);
                File downloadedFile = new File(filePath);
                FileOutputStream fout = new FileOutputStream(downloadedFile);
                byte[] chunk = new byte[1024];

                if(link.startsWith("https")){
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    long  totalFileLength = connection.getContentLength();
                    InputStream stream = url.openStream();


                    long chunksToDownload = 0;

                    if(totalFileLength % chunk.length > 0){
                        chunksToDownload++;
                        if((totalFileLength/chunk.length)%2 == 1){
                            chunksToDownload += ((totalFileLength / chunk.length) / 2) + 1;
                        }
                    }
                    else chunksToDownload = ((totalFileLength / chunk.length) / 2);

                    long bytesRemaining = chunksToDownload*chunk.length;

                    while(bytesRemaining > 0) {
                        int n = stream.read(chunk);
                        if(n > 0) {

                            if(bytesRemaining < n) {
                                fout.write(Bytes.trimArrayFromEnd(chunk, (int) bytesRemaining));
                            } else {
                                fout.write(Bytes.trimArrayFromEnd(chunk, n));
                            }
                            bytesRemaining -= n;
                        }
                    }

                    Log.e("Download Completed", "Good");
                    successful = true;
                    fout.close();
                    stream.close();
                }else{
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    // TODO: 02/03/2021 ksaana ta idia
                    // TODO: 02/03/2021 pezi na mporoume na to genikefsoume me URLConnection
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Log.e("URL","Not a file to download.");
        }
    }

    private void downloadSecondHalf() {
        boolean isFile = false;
        if(link.startsWith("https://")){
            if(fileName.contains(".")){
                isFile = true;
            }
        }else{
            Toast.makeText(context, "not Valid Url", Toast.LENGTH_SHORT).show();
        }

        if (isFile){
            try {
                URL url = new URL(link);
                File downloadedFile = new File(filePath);
                Log.e("FileName", downloadedFile.getName());
                FileOutputStream fout = new FileOutputStream(downloadedFile);

                byte[] chunk = new byte[1024];

                if(link.startsWith("https")) {
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                    long totalFileLength = connection.getContentLength();
                    connection.disconnect();

                    long chunksToDownload = ((totalFileLength / chunk.length) / 2);
                    long lastChunkSize = totalFileLength % chunk.length;
                    long bytesRemaining = (chunksToDownload - 1) *chunk.length + lastChunkSize;

                    connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestProperty("Range", "bytes=" + (totalFileLength - bytesRemaining) + "-" + totalFileLength);

                    InputStream stream = new BufferedInputStream(connection.getInputStream());

                    while(bytesRemaining > 0) {
                        int n = stream.read(chunk);
                        if(n > 0) {
                            fout.write(Bytes.trimArrayFromEnd(chunk, n));
                            bytesRemaining -= n;
                        }
                    }
                    Log.e("Download Completed", "Good");

                    successful = true;
                    stream.close();
                    connection.disconnect();
                }else{
                    // TODO: 02/03/2021 http
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
