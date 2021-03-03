 package com.example.myapplication;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

 public class ClientClass extends Thread {
    Socket socket;
    String hostAddress;
    MessageActivity messageActivity;
    Receive receive;
    Send send;

    public ClientClass(InetAddress hostAddress, MessageActivity messageActivity) {
        this.hostAddress = hostAddress.getHostAddress();
        this.messageActivity = messageActivity;
        socket = new Socket();
    }

    @Override
    public void run() {
        Log.i("client thread", "Client thread started");
        messageActivity.handler.obtainMessage(MessageActivity.STATUS_MESSAGE, "Trying to connect to host...").sendToTarget();

        for(int i=0; i<5; i ++) {
            try {
                socket.connect(new InetSocketAddress(hostAddress, 8888));
                receive = new Receive(socket, messageActivity);
                receive.start();

                send = new Send(socket, messageActivity);
                send.start();

                Log.i("client thread", "Client thread initialized, total retries : " + i);
                messageActivity.handler.obtainMessage(MessageActivity.STATUS_MESSAGE, "Connected to host...").sendToTarget();
                break;
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }

            }
        }
    }

}
