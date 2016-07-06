package com.wisen.wisenapp.bt;

import android.Manifest;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;

import swordbearer.audio.sender.AudioEncoder;

/**
 * Created by wisen on 2016-06-28.
 */
public class Saudioclient extends Thread
{
    // Debugging
    private static final String TAG = "Saudioclient";
    private static final boolean D = true;

    protected AudioRecord m_in_rec;
    protected int m_in_buf_size;
    protected byte[] m_in_bytes;
    protected boolean m_keep_running;
    protected BluetoothSocket s;
    protected DataOutputStream dout;
    protected LinkedList<byte[]> m_in_q;
    private int bufferSize;

    private int bufferRead;
    private static final int BUFFER_FRAME_SIZE = 480;
    public Saudioclient(BluetoothSocket s) {
        if (D) Log.d(TAG, "Saudioclient create");
        this.s = s;
    }

    public void init() {
        if (D) Log.d(TAG, "Saudioclient init");
        bufferSize = BUFFER_FRAME_SIZE;
        m_in_buf_size = AudioRecord.getMinBufferSize(8000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        m_in_rec = new AudioRecord(MediaRecorder.AudioSource.MIC,
                8000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                m_in_buf_size);
        m_in_bytes = new byte[m_in_buf_size];
        m_keep_running = true;
        m_in_q = new LinkedList<byte[]>();

        try {
            // s=new Socket("192.168.1.100",4332);
            dout = new DataOutputStream(s.getOutputStream());
            // new Thread(R1).start();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void run() {
        if (D) Log.d(TAG, "Saudioclient thread run");
        try {
            AudioEncoder encoder = AudioEncoder.getInstance();
            encoder.setSocket(s);
            encoder.startEncoding();
            byte[] bytes_pkg;
            m_in_rec.startRecording();

            while (m_keep_running) {
                bufferRead = m_in_rec.read(m_in_bytes, 0, bufferSize);
                bytes_pkg = m_in_bytes.clone();
                if (bytes_pkg.length > 0) {
                    // add data to encoder
                    encoder.addData(bytes_pkg, bufferRead);
                }
                // if (m_in_q.size() >= 2)
                //
                // {
                //
                // dout.write(m_in_q.removeFirst(), 0,
                // m_in_q.removeFirst().length);
                //
                // }
                m_in_q.add(bytes_pkg);
            }

            m_in_rec.stop();
            m_in_rec = null;
            m_in_bytes = null;
            //don't close bluetoothsocket here, because we still use it in bluetoothchatservice
            //dout.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dout = null;
        }
        if (D) Log.d(TAG, "Saudioclient thread exit");
    }

    public void free() {
        m_keep_running = false;
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            if (D) Log.d("sleep exceptions...", "");
        }

    }
}
