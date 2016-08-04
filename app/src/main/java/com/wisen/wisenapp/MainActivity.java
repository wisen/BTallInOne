package com.wisen.wisenapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.wisen.wisenapp.audio.AudioMainActivity;
import com.wisen.wisenapp.bt.BTMainActivity;
import com.wisen.wisenapp.btadv.BTAdvActivity;
import com.wisen.wisenapp.btsmart.ScanResultsActivity;
import com.wisen.wisenapp.btsmart.xiaomi.BondedDevice;
import com.wisen.wisenapp.btsmart.xiaomi.HackXMMainActivity;
import com.wisen.wisenapp.btsmart.xiaomi.XiaoMiUtil;
import com.wisen.wisenapp.micphone.MicPhoneMainActivity;
import com.wisen.wisenapp.pbap.PbapMainActivity;
import com.wisen.wisenapp.ui.UIMainActivity;

import java.util.ArrayList;

/**
 * Created by wisen on 2016-06-23.
 */
public class MainActivity extends AppCompatActivity {


    private Button Btn_btMain = null;
    private Button Btn_audioMain = null;
    private Button Btn_btSmart = null;
    private Button Btn_btAdv = null;
    private Button Btn_UIMain = null;
    private Button Btn_XiaoMIMain = null;
    private Button Btn_phone_book = null;
    private Button Btn_micPhone = null;

    private static final String TAG = "WisenApp";
    private static final boolean D = true;

    final private int REQUEST_WRITE_EXTERNAL_STORAGE = 12;

    private TextView mTitle = null;
    private ArrayList<BondedDevice> mList;

    protected boolean hasWRITE_EXTERNAL_STORAGEPermission() {
        if (D) Log.d(TAG, "hasRecordAudioPermission()");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (D) Log.d(TAG, "requestPermissions Manifest.permission.ACCESS_COARSE_LOCATION");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE);
            return false;
        }
        return true;
    }

    private void testxml(){

        if(!hasWRITE_EXTERNAL_STORAGEPermission()){
            Log.e(TAG, "No WRITE_EXTERNAL_STORAGE Permission!!!");
            return;
        }

        if(mList == null){
            mList = new ArrayList<BondedDevice>();
        }

        XiaoMiUtil.do_parser(mList);

        for(BondedDevice device:mList){
            Log.d("WisenApp", "device: " + device.getName() + " " + device.getAddress());
        }

        Log.d("WisenApp", "begin to write");
        BondedDevice device = new BondedDevice("00:00:00:00:00:00", "wisen");
        mList.add(device);

        XiaoMiUtil.saveinfo(mList);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Btn_btMain = (Button)findViewById(R.id.btMainActivity);
        Btn_btMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BTMainActivity.class);
                startActivity(intent);
            }
        });

        Btn_audioMain = (Button)findViewById(R.id.audioMainActivity);
        Btn_audioMain.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AudioMainActivity.class);
                startActivity(intent);
            }
        });

        Btn_btSmart = (Button)findViewById(R.id.btSmartActivity);
        Btn_btSmart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScanResultsActivity.class);
                startActivity(intent);
            }
        });

        Btn_btAdv = (Button)findViewById(R.id.btAdvActivity);
        Btn_btAdv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BTAdvActivity.class);
                startActivity(intent);
            }
        });

        Btn_UIMain = (Button)findViewById(R.id.btn_uimain);
        Btn_UIMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UIMainActivity.class);
                startActivity(intent);
            }
        });

        Btn_XiaoMIMain = (Button)findViewById(R.id.btn_hack_xm);
        Btn_XiaoMIMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HackXMMainActivity.class);
                startActivity(intent);
            }
        });


        Btn_phone_book = (Button)findViewById(R.id.btn_phone_book);
        Btn_phone_book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PbapMainActivity.class);
                startActivity(intent);
            }
        });

        Btn_micPhone = (Button)findViewById(R.id.btn_micphone);
        Btn_micPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MicPhoneMainActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
