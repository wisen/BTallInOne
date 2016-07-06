package swordbearer.audio.sender;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import swordbearer.audio.NetConfig;
import swordbearer.audio.data.AudioData;

/**
 * Created by wisen on 2016-06-28.
 */
public class AudioSender implements Runnable {
    private static final String TAG = "AudioSender";
    private static final boolean D = true;

    private boolean isSendering = false;
    private List<AudioData> dataList;

    DatagramSocket socket;
    DatagramPacket dataPacket;
    private InetAddress ip;
    private int port;
    private BluetoothSocket s;
    private DataOutputStream dout;
    public AudioSender(BluetoothSocket bs) {
        this.s = bs;
        try {
            if(null!= s){
                dout = new DataOutputStream(s.getOutputStream());
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        dataList = Collections.synchronizedList(new LinkedList<AudioData>());
        /*
        try {
            try {
                ip = InetAddress.getByName(NetConfig.SERVER_HOST);
                if (D) Log.d(TAG, "Server IP: " + ip.toString());
                port = NetConfig.SERVER_PORT;
                socket = new DatagramSocket();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }*/
    }

    public void addData(byte[] data, int size) {
        if (D) Log.d(TAG, "addData size = " + size);
        AudioData encodedData = new AudioData();
        encodedData.setSize(size);
        byte[] tempData = new byte[size];
        System.arraycopy(data, 0, tempData, 0, size);
        encodedData.setRealData(tempData);
        dataList.add(encodedData);
    }

    /*
     * send data to server
     */
    private void sendData(byte[] data, int size) {
        if (D) Log.d(TAG, "sendData size = " + size);
        try {
            dout.write(data, 0,
                    data.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * start sending data
     */
    public void startSending() {
        new Thread(this).start();
    }

    /*
     * stop sending data
     */
    public void stopSending() {
        this.isSendering = false;
    }

    // run
    public void run() {
        this.isSendering = true;
        if (D) Log.d(TAG, "start AudioSender thread...");
        while (isSendering) {
            if (dataList.size() > 0) {
                AudioData encodedData = dataList.remove(0);
                sendData(encodedData.getRealData(), encodedData.getSize());
            }
        }
        if (D) Log.d(TAG, "exit AudioSender thread...");
    }
}