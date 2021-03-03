package com.example.myapplication;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;

public class Receive extends Thread{
    private Socket socket;
    private InputStream inputStream;
    private MessageActivity messageActivity;

    private int sizeSoFar;

    private byte[] latestFile;



    public Receive(Socket socket, MessageActivity messageActivity) {
        this.socket = socket;
        this.messageActivity = messageActivity;
        try{
            inputStream = this.socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[4096];
        int count;

        while(socket!=null) {
            try {
                count = inputStream.read(buffer);
                if(count > 0) {
                    byte id = buffer[0];
                    messageActivity.handler.obtainMessage(id, count, -1, buffer).sendToTarget();

                    if(id == messageActivity.FILE_INFO || id == messageActivity.HALF_FILE_INFO) {
                        sizeSoFar = 0;

                        latestFile = new byte[Bytes.byteArrayToInt(Arrays.copyOfRange(buffer, 1, 5))];

                        while(sizeSoFar < latestFile.length) {
                            count = inputStream.read(buffer);
                            if(count > 0) {
                                byte[] actualMessage = Bytes.trimArrayFromEnd(buffer, count);
                                Bytes.addToArray(latestFile, actualMessage, sizeSoFar);
                                sizeSoFar += count;
                            }
                        }

                        Log.e("file ended", "file ended");

                        if(id == messageActivity.FILE_INFO) {
                            messageActivity.handler.obtainMessage(messageActivity.FILE_ENDED, latestFile).sendToTarget();
                        } else {
                            messageActivity.handler.obtainMessage(messageActivity.HALF_FILE_ENDED, latestFile).sendToTarget();
                        }

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
