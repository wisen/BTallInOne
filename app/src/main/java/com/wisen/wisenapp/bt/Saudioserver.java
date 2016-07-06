package com.wisen.wisenapp.bt;

import android.bluetooth.BluetoothSocket;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;

import swordbearer.audio.receiver.AudioDecoder;
import swordbearer.audio.receiver.AudioReceiver;

/**
 * Created by wisen on 2016-06-28.
 */
public class Saudioserver extends Thread {
    // Debugging
    private static final String TAG = "Saudioserver";
    private static final boolean D = true;

    protected AudioTrack m_out_trk;
    protected int m_out_buf_size;
    protected byte[] m_out_bytes;
    protected boolean m_keep_running;
    private BluetoothSocket s;
    private DataInputStream din;
    private AudioReceiver audioReceiver;

    public Saudioserver(BluetoothSocket s) {
        Log.d(TAG, "Saudioserver create");
        this.s = s;
    }

    public void init() {
        Log.d(TAG, "Saudioserver init");
        try {
            // s = new Socket("192.168.1.100", 4331);
            din = new DataInputStream(s.getInputStream());
            m_keep_running = true;
            m_out_buf_size = AudioTrack.getMinBufferSize(8000,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
//			m_out_trk = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
//			AudioFormat.CHANNEL_CONFIGURATION_MONO,
//			AudioFormat.ENCODING_PCM_16BIT,
//			m_out_buf_size,
//			AudioTrack.MODE_STREAM);
            m_out_bytes = new byte[m_out_buf_size];
            // new Thread(R1).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void free() {
        m_keep_running = false;
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            Log.d("sleep exceptions...\n", "");
        }
    }

    public void run() {
        Log.d(TAG, "Saudioserver thread run");
        // 在接收前，要先启动解码器
        AudioDecoder decoder = AudioDecoder.getInstance();
        decoder.setSocket(s);
        decoder.startDecoding();
        byte[] bytes_pkg = null;
//		m_out_trk.play();
        while (m_keep_running) {
            try {
                int realsize = din.read(m_out_bytes);
                bytes_pkg = m_out_bytes.clone();
                decoder.addData(bytes_pkg, 50);
                // m_out_trk.write(bytes_pkg, 0, bytes_pkg.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//		m_out_trk.stop();
//		m_out_trk = null;

        /*
        try {
        //don't close bluetoothsocket here, because we still use it in bluetoothchatservice
            din.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            din = null;
        }*/
        din = null;
        Log.d(TAG, "Saudioserver thread exit");
    }
}
