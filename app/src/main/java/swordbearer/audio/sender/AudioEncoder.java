package swordbearer.audio.sender;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import swordbearer.audio.AudioCodec;
import swordbearer.audio.data.AudioData;

/**
 * Created by wisen on 2016-06-28.
 */
public class AudioEncoder implements Runnable {
    // Debugging
    private static final String TAG = "AudioEncoder";
    private static final boolean D = true;

    private static AudioEncoder encoder;
    private boolean isEncoding = false;

    private List<AudioData> dataList = null;
    protected BluetoothSocket s;
    public static AudioEncoder getInstance() {
        if (encoder == null) {
            encoder = new AudioEncoder();
        }
        return encoder;
    }

    private AudioEncoder() {
        dataList = Collections.synchronizedList(new LinkedList<AudioData>());
    }

    public void addData(byte[] data, int size) {
        if (D) Log.d(TAG, "addData size = " + size);
        AudioData rawData = new AudioData();
        rawData.setSize(size);
        byte[] tempData = new byte[size];
        System.arraycopy(data, 0, tempData, 0, size);
        rawData.setRealData(tempData);
        dataList.add(rawData);
    }

    /*
     * start encoding
     */
    public void startEncoding() {
        if (D) Log.d(TAG, "startEncoding");
        if (isEncoding) {
            if (D) Log.d(TAG, "encoder has been started");
            return;
        }
        new Thread(this).start();
    }

    /*
     * end encoding
     */
    public void stopEncoding() {
        this.isEncoding = false;
    }

    public void run() {
        if (D) Log.d(TAG, "start encode thread");
        // start sender before encoder
        AudioSender sender = new AudioSender(s);
        sender.startSending();

        int encodeSize = 0;
        byte[] encodedData = new byte[256];

        // initialize audio encoder:mode is 30
        AudioCodec.audio_codec_init(30);

        isEncoding = true;
        while (isEncoding) {
            if (dataList.size() == 0) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (isEncoding) {
                AudioData rawData = dataList.remove(0);
                encodedData = new byte[rawData.getSize()];
                //
                encodeSize = AudioCodec.audio_encode(rawData.getRealData(), 0,
                        rawData.getSize(), encodedData, 0);
                System.out.println();
                if (encodeSize > 0) {
                    sender.addData(encodedData, encodeSize);
                    // clear data
                    encodedData = new byte[encodedData.length];
                }
            }
        }
        if (D) Log.d(TAG, "exit encode thread");
        sender.stopSending();
    }

    public void setSocket(BluetoothSocket bs){
        this.s = bs;
    }
}
