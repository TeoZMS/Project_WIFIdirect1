package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private Button btnFindDevices, downloadTest;
    private ListView peerListView;
    TextView connectionStatus;
    String myName;
    String friendName;

    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel wifiChannel;
    BroadcastReceiver broadcastReceiver;
    IntentFilter intentFilter;

    ArrayList<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNames;
    WifiP2pDevice[] devices;

    ServerClass serverClass;
    ClientClass clientClass;
    Receive receive;

    byte[] bytes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityComponents();//initialise components on main activity from activity-main.xml.

        WifiInitialize();//initialize wifi components

        btnListeners();

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    //initialise components on main activity from activity-main.xml.
    private void ActivityComponents() {
        btnFindDevices = (Button) findViewById(R.id.findDevices);
        peerListView = (ListView) findViewById(R.id.peerListView);
        connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        myName = "";
        friendName = "";

        downloadTest = (Button) findViewById(R.id.downloadTest);

    }

    private void WifiInitialize() {
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        wifiChannel = wifiP2pManager.initialize(this, getMainLooper(), null);
        broadcastReceiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, wifiChannel, this);
        intentFilter = new IntentFilter();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void btnListeners() {
        btnFindDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check if permission granted
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // You can use the API that requires the permission.
                    wifiP2pManager.discoverPeers(wifiChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            connectionStatus.setText("Discovery Started");
                        }

                        @Override
                        public void onFailure(int reason) {
                            connectionStatus.setText("Discovery Starting Failed");

                            //TODO
                            //explain fail reason
                            Toast.makeText(getApplicationContext(), String.valueOf(reason), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    // Directly ask for the permission.
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        });

        peerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final WifiP2pDevice device = devices[position];

                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                //check if permission granted
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // You can use the API that requires the permission.
                    wifiP2pManager.connect(wifiChannel, config, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getApplicationContext(), "Connecting to " + device.deviceName, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int reason) {
                            Toast.makeText(getApplicationContext(), "Not Connected", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    // Directly ask for the permission.
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        });

        downloadTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Downloader downloader = new Downloader("https://prod-accessonline-be.s3-us-west-2.amazonaws.com/uploads/2019/01/190108_3859266_Gal_Gadot_Shows_Off_Hot_Winter_Bod_On_Tropic.jpg", true, getApplicationContext());
                downloader.start();

                while(!downloader.isDone());

                String path = downloader.getFilePath();

                byte[] firstPart;
                byte[] secondPart;

                firstPart = Files.loadFile(path);

                downloader = new Downloader("https://prod-accessonline-be.s3-us-west-2.amazonaws.com/uploads/2019/01/190108_3859266_Gal_Gadot_Shows_Off_Hot_Winter_Bod_On_Tropic.jpg", false, getApplicationContext());
                downloader.start();

                while(!downloader.isDone());

                secondPart = Files.loadFile(path);

                byte[] finalFile = Bytes.concatenateArrays(firstPart, secondPart);
                Files.saveFile(finalFile, path);
            }
        });



    }




    //refresh device list if a new device is available
    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if(!peerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNames = new String[peerList.getDeviceList().size()];
                devices = new WifiP2pDevice[peerList.getDeviceList().size()];

                int i=0;

                for(WifiP2pDevice d : peerList.getDeviceList()) {
                    deviceNames[i] = d.deviceName;
                    devices[i] = d;
                    i++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNames);
                peerListView.setAdapter(adapter);
            }

            if(peers.size() == 0) {
                Toast.makeText(getApplicationContext(), "No Devices Found", Toast.LENGTH_SHORT).show();
            }
        }
    };



    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupOwnerAddress = info.groupOwnerAddress;

            if(info.groupFormed) {
                Intent i = new Intent(getApplicationContext(), MessageActivity.class);

                if(info.isGroupOwner) {
                    i.putExtra("type", "host");
                } else if (info.groupFormed) {
                    i.putExtra("type", "client");
                }

                i.putExtra("groupOwnerAddress", groupOwnerAddress);
                i.putExtra("myName", myName);
                i.putExtra("friendName", friendName);
                startActivity(i);
            }
        }
    };

    WifiP2pManager.GroupInfoListener groupInfoListener = new WifiP2pManager.GroupInfoListener() {
        @Override
        public void onGroupInfoAvailable(WifiP2pGroup group) {
            ArrayList<WifiP2pDevice> clients = new ArrayList<WifiP2pDevice>(group.getClientList());
            if(!clients.isEmpty()) {
                friendName = clients.get(0).deviceName;
            } else {
                friendName = group.getOwner().deviceName;
            }
        }
    };


}









