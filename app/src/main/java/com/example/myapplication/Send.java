package com.example.myapplication;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Send extends Thread {
    private Socket socket;
    private OutputStream outputStream;
    private MessageActivity messageActivity;

    private byte[] message;

    public Send(Socket socket, MessageActivity messageActivity) {
        this.socket = socket;
        try {
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        message = new byte[0];
    }

    @Override
    public void run() {
        while (socket != null) {
            if(message.length != 0) {
                try {
                    outputStream.write(message);
                    message = new byte[0];
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }


}
