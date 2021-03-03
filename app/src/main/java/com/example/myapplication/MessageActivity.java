package com.example.myapplication;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Half;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

public class MessageActivity extends AppCompatActivity {

    private TextView txtMsg, txtStatus, connStatus;
    private Button btnSend, btnDownload, loadImageButton, loadFile, sendFile, saveFile;
    private EditText writeMsgText;
    private ImageView imageView;

    private ListView messagesListView;
    private ArrayAdapter<String> messageAdapter;
    private ArrayList<String> messages;

    private String myName;
    private String friendName;

    private byte[] myHalf;
    private boolean isFirstHalf;

    private byte[] latestFile;
    private String latestFileName;

    private int bytesDownloadedMe = -1;
    private int bytesDownloadedOther = -1;
    private int fileSize = 0;

    private boolean acknowledgement = false;

    long startTimer = 0;

    static final byte STATUS_MESSAGE = 0;
    static final byte TEXT_MESSAGE = 1;
    static final byte LINK_MESSAGE = 2;
    static final byte FILE_MESSAGE = 3;
    static final byte DOWNLOAD_FROM_END_TO_START = 4;
    static final byte YOUR_BYTES_DOWNLOADED_SO_FAR = 5;
    static final byte MY_BYTES_DOWNLOADED_SO_FAR = 6;

    static final byte HALF_FILE_INFO = 10;
    static final byte HALF_FILE_ENDED = 11;
    static final byte FILE_INFO = 15;
    static final byte FILE_ENDED = 16;
    static final byte FILE_GOT = 17;


    String type;
    InetAddress groupOwnerAddress;
    BroadcastReceiver broadcastReceiver;

    ServerClass serverClass;
    ClientClass clientClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        type = getIntent().getExtras().getString("type");
        groupOwnerAddress = (InetAddress) getIntent().getExtras().get("groupOwnerAddress");
        myName = getIntent().getExtras().getString("myName");
        friendName = getIntent().getExtras().getString("friendName");

        txtMsg = (TextView) findViewById(R.id.txtMsg);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        connStatus = (TextView) findViewById(R.id.connStatusTxt);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnDownload = (Button) findViewById(R.id.btnDownload);
        writeMsgText = (EditText) findViewById(R.id.writeMsgTxt);

        loadFile = (Button) findViewById(R.id.loadFile);
        sendFile = (Button) findViewById(R.id.sendFile);
        saveFile = (Button) findViewById(R.id.saveFile);

        //////

        loadImageButton = (Button) findViewById(R.id.testLoadImageButton);
        imageView = (ImageView) findViewById(R.id.testImageView);

        loadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Pick an image"), 1);
            }
        });


        /////

        messagesListView = (ListView) findViewById(R.id.messagesListView);
        messages = new ArrayList<String>();
        messageAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, messages);
        messagesListView.setAdapter(messageAdapter);

        txtStatus.setText(String.format("%s (%s)", type, myName));

        if(type.equals("host")) {
            serverClass = new ServerClass(this);
            serverClass.start();
        } else if(type.equals("client")) {
            clientClass = new ClientClass(groupOwnerAddress, this);
            clientClass.start();
        }

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = writeMsgText.getText().toString();
                writeMsgText.setText(""); //clear EditText

                sendTextMessage(msg);
            }
        });

        loadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    latestFile = new byte[0];
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + writeMsgText.getText().toString();
                    latestFile = Files.loadFile(path);
                    if(latestFile != null) {
                        Toast.makeText(getApplicationContext(), "File loaded!", Toast.LENGTH_SHORT).show();
                        latestFileName = path.substring(path.lastIndexOf('/') + 1);
                        writeMsgText.setText("");
                    } else {
                        Toast.makeText(getApplicationContext(), "File failed to load", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
            }
        });

        saveFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WifiDirect/" + latestFileName;
                    if(Files.saveFile(latestFile, path)) {
                        Toast.makeText(getApplicationContext(), "File Saved!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to save file", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
        });

        sendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                messages.add(myName + " :" + "Sending a file..Please wait");
                messageAdapter.notifyDataSetChanged();

                byte[] message = Bytes.addIdentifier(FILE_INFO, Bytes.concatenateArrays(Bytes.intToByteArray(latestFile.length), latestFileName.getBytes()));
                sendMessage(message);

                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                startTimer = System.currentTimeMillis();
                sendMessage(latestFile);
            }
        });

        btnDownload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                messages.add("Downloading...");
                messageAdapter.notifyDataSetChanged();

                startTimer = System.currentTimeMillis();

                String link = writeMsgText.getText().toString();
                byte[] message = Bytes.addIdentifier(DOWNLOAD_FROM_END_TO_START, link.getBytes());

                sendMessage(message);
                Downloader downloader = new Downloader(link, true, getApplicationContext());
                downloader.start();

                while (!downloader.isDone());

                if(downloader.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Download was successful", Toast.LENGTH_SHORT).show();

                    isFirstHalf = true;
                    latestFileName = downloader.getFileName();

                    myHalf = Files.loadFile(downloader.getFilePath());

                    message = Bytes.addIdentifier(HALF_FILE_INFO, Bytes.intToByteArray(myHalf.length));
                    sendMessage(message);

                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    sendMessage(myHalf);
                } else {
                    Toast.makeText(getApplicationContext(), "Download wasn't successful", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case TEXT_MESSAGE:
                    byte[] readBuff = Bytes.removeIdentifier((byte[]) msg.obj);
                    String tempMsg = new String(readBuff, 0, msg.arg1 - 1);
                    messages.add(friendName + " :" + tempMsg);
                    messageAdapter.notifyDataSetChanged();
                    break;
                case STATUS_MESSAGE:
                    Log.i("handler", "status");
                    String status = (String) msg.obj;
                    Log.i("handler", status);
                    connStatus.setText(status);
                    break;
                case LINK_MESSAGE:
                    // TODO: 01/03/2021 link message
                    /// start download backwards
                    break;
                case YOUR_BYTES_DOWNLOADED_SO_FAR:
                    sendMyBytes();
                    break;
                case MY_BYTES_DOWNLOADED_SO_FAR:
                    bytesDownloadedOther = Bytes.byteArrayToInt(Bytes.removeIdentifier((byte[]) msg.obj));
                    break;
                case FILE_INFO:
                    byte[] buff = Bytes.removeIdentifier((byte[]) msg.obj);
                    latestFileName = new String(buff, 4, msg.arg1 - 1 - 4);

                    messages.add(friendName + " :" + "Sending a file..Please wait");
                    messageAdapter.notifyDataSetChanged();
                    break;
                case HALF_FILE_INFO:
                    messages.add(friendName + " :" + "Sending his half file..Please wait");
                    messageAdapter.notifyDataSetChanged();
                    break;
                case FILE_ENDED:

                    latestFile = (byte[]) msg.obj;

                    messages.add(myName + " :" + "File sent..Press SV to save it to your phone!");
                    messageAdapter.notifyDataSetChanged();

                    sendFileGot();
                    break;
                case HALF_FILE_ENDED:

                    if(isFirstHalf) {
                        latestFile = Bytes.concatenateArrays(myHalf, (byte[]) msg.obj);
                    } else {
                        latestFile = Bytes.concatenateArrays((byte[]) msg.obj , myHalf);
                    }
                    messages.add(myName + " :" + "Send my half..Press SV to save the whole file it to your phone!");
                    messageAdapter.notifyDataSetChanged();
                    sendFileGot();
                    break;
                case FILE_GOT:
                    double timeToSend = (System.currentTimeMillis() - startTimer) / 1000.0;

                    messages.add(friendName + " :" + "File sent! Time needed: " + timeToSend + " secs");
                    messageAdapter.notifyDataSetChanged();
                    break;
                case DOWNLOAD_FROM_END_TO_START:

                    messages.add("Downloading...");
                    messageAdapter.notifyDataSetChanged();

                    startTimer = System.currentTimeMillis();

                    readBuff = Bytes.removeIdentifier((byte[]) msg.obj);
                    String link = new String(readBuff, 0, msg.arg1 - 1);

                    Downloader downloader = new Downloader(link, false, getApplicationContext());
                    downloader.start();

                    while(!downloader.isDone());

                    if(downloader.isSuccessful()) {

                        Toast.makeText(getApplicationContext(), "Download was successful", Toast.LENGTH_SHORT).show();


                        isFirstHalf = false;
                        latestFileName = downloader.getFileName();

                        myHalf = Files.loadFile(downloader.getFilePath());



                        byte[] message = Bytes.addIdentifier(HALF_FILE_INFO, Bytes.intToByteArray(myHalf.length));
                        sendMessage(message);

                        try {
                            TimeUnit.MILLISECONDS.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        sendMessage(myHalf);

                    } else {
                        Toast.makeText(getApplicationContext(), "Download wasn't successful", Toast.LENGTH_SHORT).show();
                    }

                    break;
            }
            return true;
        }
    });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == 1) {
            try{
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(byte[] message) {
        if(type.equals("host")) {
            serverClass.send.setMessage(message);
        } else if(type.equals("client")) {
            clientClass.send.setMessage(message);
        }
    }

    private void sendTextMessage(String msg) {
        //show message on my screen
        messages.add(myName + ": " + msg);
        messageAdapter.notifyDataSetChanged();

        //send message
        byte[] message = Bytes.addIdentifier(TEXT_MESSAGE, msg.getBytes());
        sendMessage(message);
    }

    private void sendMyBytes() {
        byte[] message = Bytes.addIdentifier(MY_BYTES_DOWNLOADED_SO_FAR, Bytes.intToByteArray(bytesDownloadedMe));

        sendMessage(message);
    }

    private void askBytesDownloaded() {
        byte[] message = new byte[1];
        message[0] = YOUR_BYTES_DOWNLOADED_SO_FAR;

        sendMessage(message);
    }

    private void sendFileGot() {
        byte[] message = new byte[1];

        message[0] = FILE_GOT;
        sendMessage(message);
    }


    public byte[] getLatestFile() {
        return latestFile;
    }

    public void setLatestFile(byte[] latestFile) {
        this.latestFile = latestFile;
    }
}