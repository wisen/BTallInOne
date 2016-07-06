package swordbearer.audio.receiver;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import swordbearer.audio.AudioConfig;
import swordbearer.audio.data.AudioData;

/**
 * Created by wisen on 2016-06-28.
 */
public class AudioPlayer implements Runnable {
    private static final String TAG = "AudioPlayer";
    private static final boolean D = true;
    private static AudioPlayer player;

    private List<AudioData> dataList = null;
    private AudioData playData;
    private boolean isPlaying = false;

    private AudioTrack audioTrack;

    //
    private File file;
    private FileOutputStream fos;

    private AudioPlayer() {
        dataList = Collections.synchronizedList(new LinkedList<AudioData>());

        file = new File("/sdcard/audio/decode.amr");
        try {
            if (!file.exists())
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static AudioPlayer getInstance() {
        if (player == null) {
            player = new AudioPlayer();
        }
        return player;
    }

    public void addData(byte[] rawData, int size) {
        AudioData decodedData = new AudioData();
        decodedData.setSize(size);
        byte[] tempData = new byte[size];
        System.arraycopy(rawData, 0, tempData, 0, size);
        decodedData.setRealData(tempData);
        dataList.add(decodedData);
        Log.e(TAG, "dataList.size = " + dataList.size());
    }

    /*
     * init Player parameters
     */
    private boolean initAudioTrack() {
        Log.d(TAG, "initAudioTrack start");
        int bufferSize = AudioRecord.getMinBufferSize(AudioConfig.SAMPLERATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioConfig.AUDIO_FORMAT);
        if (bufferSize < 0) {
            Log.e(TAG, "initialize error!");
            return false;
        }
        Log.d(TAG, "bufferSize = " + bufferSize);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                AudioConfig.SAMPLERATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioConfig.AUDIO_FORMAT, bufferSize, AudioTrack.MODE_STREAM);
        // set volume
        audioTrack.setStereoVolume(1.0f, 1.0f);
        audioTrack.play();
        Log.d(TAG, "initAudioTrack end");
        return true;
    }

    private void playFromList() throws IOException {
        Log.d(TAG, "playFromList");
        while (isPlaying) {
            while (dataList.size() > 0) {
                playData = dataList.remove(0);
                Log.d(TAG, "playFromList dataList.size = " + dataList.size());
                audioTrack.write(playData.getRealData(), 0, playData.getSize());
                // fos.write(playData.getRealData(), 0, playData.getSize());
                // fos.flush();
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
    }

    public void startPlaying() {
        Log.d(TAG, "startPlaying");
        if (isPlaying) {
            Log.d(TAG, "isPlaying = " + isPlaying);
            return;
        }
        new Thread(this).start();
    }

    public void run() {
        this.isPlaying = true;
        if (!initAudioTrack()) {
            Log.d(TAG, "播放器初始化失败");
            return;
        }
        Log.d(TAG, "开始播放");
        try {
            playFromList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // while (isPlaying) {
        // if (dataList.size() > 0) {
        // playFromList();
        // } else {
        //
        // }
        // }
        if (this.audioTrack != null) {
            if (this.audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                this.audioTrack.stop();
                this.audioTrack.release();
            }
        }
        Log.d(TAG, "end playing");
    }

    public void stopPlaying() {
        this.isPlaying = false;
    }
}

