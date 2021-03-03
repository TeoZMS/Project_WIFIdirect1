package com.example.myapplication;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerClass extends Thread {
    Socket socket;
    ServerSocket serverSocket;
    MessageActivity messageActivity;
    Receive receive;
    Send send;

    public ServerClass(MessageActivity messageActivity) {
        this.messageActivity = messageActivity;
    }

    @Override
    public void run(){
        Log.i("server class","Server thread started");
        messageActivity.handler.obtainMessage(MessageActivity.STATUS_MESSAGE, "Waiting for client to connect...").sendToTarget();
        try{

            serverSocket = new ServerSocket(8888);
            socket = serverSocket.accept();
            receive = new Receive(socket, messageActivity);
            receive.start();

            send = new Send(socket, messageActivity);
            send.start();
            Log.i("server class","Server thread initialized");
            messageActivity.handler.obtainMessage(MessageActivity.STATUS_MESSAGE, "Clint connected...").sendToTarget();


        } catch (IOException e) {
            e.printStackTrace();
            Log.i("server class","Server thread exception");
        }
    }

}
