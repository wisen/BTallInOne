package com.wisen.wisenapp.micphone;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.wisen.wisenapp.R;

public class MicPhoneMainActivity extends AppCompatActivity {

    private final String TAG = "MicPhone";

    private int audioSource = MediaRecorder.AudioSource.MIC;
    private int samplingRate = 22500; /* in Hz*/
    /*
    * 默认的情况下，Android放音的采样率固定为44.1khz，录音的采样率固定为8khz，
    * 因此底层的音频设备驱动只需设置好这两个固定的采样率。如果上层传过来的采样率与其不符的话，
    * 则Android Framework层会对音频流做resample（重采样）处理
    * issue1: 当我把录音采样率设置成8000, 放音采样率设置为44100, 耳机里面听到就是噪音
    * */
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize;
    private int mSystemStreamInSampleRate = 0;
    private int mSystemStreamOutSampleRate = 0;
    //private int sampleNumBits = 16;
    //private int numChannels = 1;
    private boolean isRecording = false;

    private AudioRecord recorder;
    private AudioTrack audioPlayer;
    private AudioManager mAudioManager = null;

    private Button btn_start_micphone;
    private Button btn_stop_micphone;

    private MicPhoneThread mMicPhoneThread;

    final private int REQUEST_RECORD_AUDIO = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mic_phone_main);

        Log.d(TAG, "onCreate");

        btn_start_micphone = (Button)findViewById(R.id.btn_start_micphone);
        btn_stop_micphone = (Button)findViewById(R.id.btn_stop_micphone);

        if(!hasRecordAudioPermission()){
            Log.e(TAG, "have no record audio permission");
        } else {
            init_recorder_and_player();
        }

        btn_start_micphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = true;
                mMicPhoneThread = new MicPhoneThread();
                mMicPhoneThread.start();
            }
        });

        btn_stop_micphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = false;
                if(mMicPhoneThread != null)
                    mMicPhoneThread = null;

                if(null != recorder){
                    recorder.stop();
                }

                if(null != audioPlayer){
                    audioPlayer.stop();
                }
            }
        });
    }

    private void start_record_and_play(){

        recorder.startRecording();

        if(audioPlayer.getPlayState() != AudioTrack.PLAYSTATE_PLAYING)
            audioPlayer.play();

        int readBytes=0, writtenBytes=0;
        byte[] data = new byte[2048];
        do{
            readBytes = recorder.read(data, 0, bufferSize);

            if(AudioRecord.ERROR_INVALID_OPERATION != readBytes){
                writtenBytes += audioPlayer.write(data, 0, readBytes);
            }
        }
        while(isRecording);
    }

    protected boolean hasRecordAudioPermission() {
        Log.d(TAG, "hasRecordAudioPermission()");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "requestPermissions Manifest.permission.ACCESS_COARSE_LOCATION");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO);
            return false;
        }
        return true;
    }

    private void init_recorder_and_player(){
        bufferSize = AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat);
        Log.d(TAG, "bufferSize = " + bufferSize);
        //bufferSize += 2048;
        Log.d(TAG, "bufferSize = " + bufferSize);
        recorder = new AudioRecord(audioSource, samplingRate, channelConfig, audioFormat, bufferSize);
        if(null != recorder){
            Log.d(TAG, "AudioRecord create success!");
        } else {
            Log.d(TAG, "AudioRecord create failed!");
        }
        audioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, 22500, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);

        if(null != audioPlayer){
            Log.d(TAG, "AudioTrack create success!");
        } else {
            Log.d(TAG, "AudioTrack create failed!");
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult, requestCode = " + requestCode +
                ", permissions = " + permissions + ", grantResults = " + grantResults[0]);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init_recorder_and_player();
                } else {
                    //TODO re-request
                }
                break;
            }
        }
    }

    class MicPhoneThread extends Thread
    {
        @Override
        public void run() {
            start_record_and_play();
        }
    }

}
