package com.wisen.wisenapp.audio;

import android.media.AudioFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wisen.wisenapp.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AudioMainActivity extends AppCompatActivity {

    private Button btn_audio_play = null;
    private Button btn_audio_pause = null;
    private Button btn_audio_stop = null;
    private TextView tv_audio_status = null;

    private AudioPlayer2 mAudioPlayer = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_main);

        btn_audio_play = (Button)findViewById(R.id.btn_audio_play);
        btn_audio_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioPlayer.play();
            }
        });

        btn_audio_pause = (Button)findViewById(R.id.btn_audio_pause);
        btn_audio_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioPlayer.pause();
            }
        });

        btn_audio_stop = (Button)findViewById(R.id.btn_audio_stop);
        btn_audio_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioPlayer.stop();
            }
        });

        tv_audio_status = (TextView)findViewById(R.id.audioPlayStatus);

        mAudioPlayer = new AudioPlayer2(mHandler);

        // 获取音频参数
        AudioParam audioParam = getAudioParam();
        mAudioPlayer.setAudioParam(audioParam);

        // 获取音频数据
        byte[] data = getPCMData();
        mAudioPlayer.setDataSource(data);

        // 音频源就绪
        mAudioPlayer.prepare();
        if (data == null)
        {
            tv_audio_status.setText(filePath + "：该路径下不存在文件！");
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case AudioPlayer2.STATE_MSG_ID:
                    showState((Integer)msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        mAudioPlayer.release();
    }

    public void showState(int state)
    {
        String showString = "";

        switch(state)
        {
            case PlayState.MPS_UNINIT:
                showString = "MPS_UNINIT";
                break;
            case PlayState.MPS_PREPARE:
                showString = "MPS_PREPARE";
                break;
            case PlayState.MPS_PLAYING:
                showString = "MPS_PLAYING";
                break;
            case PlayState.MPS_PAUSE:
                showString = "MPS_PAUSE";
                break;
        }

        showStateStr(showString);
    }

    public void showStateStr(String str)
    {
        tv_audio_status.setText(str);
    }

    /*
* 获得PCM音频数据参数
*/
    public AudioParam getAudioParam()
    {
        AudioParam audioParam = new AudioParam();
        audioParam.mFrequency = 44100;
        audioParam.mChannel = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
        audioParam.mSampBit = AudioFormat.ENCODING_PCM_16BIT;

        return audioParam;
    }

    String  filePath  = "/sdcard/audio/testmusic.pcm";
    /*
     * 获得PCM音频数据
     */
    public byte[] getPCMData()
    {

        File file = new File(filePath);
        if (file == null){
            return null;
        }

        FileInputStream inStream;
        try {
            inStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        byte[] data_pack = null;
        if (inStream != null){
            long size = file.length();

            data_pack = new byte[(int) size];
            try {
                inStream.read(data_pack);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }

        }

        return data_pack;
    }
}
