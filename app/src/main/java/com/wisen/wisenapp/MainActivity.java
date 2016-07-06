package com.wisen.wisenapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.wisen.wisenapp.audio.AudioMainActivity;
import com.wisen.wisenapp.bt.BTMainActivity;
import com.wisen.wisenapp.btadv.BTAdvActivity;
import com.wisen.wisenapp.btsmart.ScanResultsActivity;

/**
 * Created by wisen on 2016-06-23.
 */
public class MainActivity extends AppCompatActivity {

    private Button Btn_btMain = null;
    private Button Btn_audioMain = null;
    private Button Btn_btSmart = null;
    private Button Btn_btAdv = null;

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
