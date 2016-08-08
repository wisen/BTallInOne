package com.wisen.wisenapp.micphone;

import android.Manifest;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import java.util.List;

public class MicPhoneMainActivity extends AppCompatActivity {

    private final String TAG = "MicPhone";

    private int audioSource = MediaRecorder.AudioSource.MIC;
    private int samplingRate = 22500; /* in Hz*/
    /*
    * 默认的情况下，Android放音的采样率固定为44.1khz，录音的采样率固定为8khz，
    * 因此底层的音频设备驱动只需设置好这两个固定的采样率。如果上层传过来的采样率与其不符的话，
    * 则Android Framework层会对音频流做resample（重采样）处理
    * issue1: 当我把录音采样率设置成8000, 放音采样率设置为44100, 耳机里面听到就是噪音
    * issue2: 下面描述第二个issue, 一开始我连接蓝牙耳机, 接着我连接boombass, 这个时候声音从手机的
    * speaker和boombass同时出来了,这时为什么呢?
    * 先说一下, 因为boombass连接手机后, 蓝牙耳机的A2DP就断了, 然后留上报了HeadsetHalConstants.AUDIO_STATE_DISCONNECTED
    * 然后在HeadsetStateMachine中会对这个消息进行处理:
    * mAudioManager.setBluetoothScoOn(false);
    * 而这句是强制切换audio path到NONE状态,而底层会处理这个NONE的时候会优先切到earpiece->speaker,
    * 因为没有插耳机,所以从speaker出来了.
    * 接着boombass连上, 再加一个A2DP的输出, 这就是2个声音都出来了.
    * 所以我们应该在write之前强制切换, 如果A2DP存在了, 就out到A2DP设备.
    * 其实我怕这样改之后会有另一个issue: boombass连上后, 声音短暂从speaker出来,A2DP建立成功
    * 后, 才从A2DP出来.明天试试看.
    * 今天跟音频的同事讨论了一下,目前Android没不能动态的改Audio policy, 所以我原来的想法是当这个MicPhone
    * 的app起来后, 动态改audio policy为只保留一个A2DP的audio path不能实现了.
    * 后来我想既然不能动态改audio policy, 那么我们就在这里注册reciever, 接收HeadsetHalConstants.AUDIO_STATE_DISCONNECTED
    * 收到后, stop audio track的write.
    * note: 说说issue2, 今天又和audio的同事讨论了下,连上boombass后声音同时从boombass和speaker出来是我测试
    * 的那台手机E56的一个feature, 在E56上专门对这种boombass做了一个双输出处理. 用别的手机,或者别的A2DP设备
    * 就没这样的问题了, 这也是为什么一开始用蓝牙耳机的时候没有声音从speaker出来的原因.
    * */
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize;
    //private int mSystemStreamInSampleRate = 0;
    //private int mSystemStreamOutSampleRate = 0;
    //private int sampleNumBits = 16;
    //private int numChannels = 1;
    private boolean isRecording = false;
    private boolean isA2DPon = false;

    private AudioRecord recorder;
    private AudioTrack audioPlayer;
    private AudioManager mAudioManager;

    private Button btn_start_micphone;
    private Button btn_stop_micphone;
    private BluetoothA2dp mBluetoothA2DP;
    private BluetoothAdapter mBluetoothAdapter;
    List<BluetoothDevice> devices;

    private MicPhoneThread mMicPhoneThread;

    final private int REQUEST_RECORD_AUDIO = 5;

    private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                mBluetoothA2DP = (BluetoothA2dp) proxy;
            }
        }
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.A2DP) {
                mBluetoothA2DP = null;
            }
        }
    };

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

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(null == mBluetoothAdapter){
            Log.e(TAG, "Please enable bluetooth first");
            return;
        }

        //mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        //mAudioManager.setSpeakerphoneOn(false);//默认关掉speaker

        mBluetoothAdapter.getProfileProxy(this, mProfileListener, BluetoothProfile.A2DP);

        btn_start_micphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != mBluetoothA2DP){
                    devices = mBluetoothA2DP.getConnectedDevices();
                    //如果有任何A2DP连接, isA2DPon都为true
                    Log.d(TAG, "get A2DP devices");
                    for ( final BluetoothDevice dev : devices ) {
                        Log.d(TAG, "there have A2DP device connected");
                        isA2DPon = true;
                    }
                } else {
                    Log.d(TAG, "Please confirm that you had connected to A2DP ");
                }

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

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "receive action: " + action);
            if (BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                if(null != mBluetoothA2DP){
                    devices = mBluetoothA2DP.getConnectedDevices();
                    Log.d(TAG, "get A2DP devices");
                    //如果有任何A2DP连接, isA2DPon都为true
                    for ( final BluetoothDevice dev : devices ) {
                        Log.d(TAG, "there have A2DP device connected");
                        isA2DPon = true;
                    }
                } else {
                    Log.d(TAG, "Please confirm that you had connected to A2DP ");
                }
            }
        }
    };

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
        while(isRecording && isA2DPon);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //if(null != mAudioManager)
            //mAudioManager.setSpeakerphoneOn(true);
    }
}
