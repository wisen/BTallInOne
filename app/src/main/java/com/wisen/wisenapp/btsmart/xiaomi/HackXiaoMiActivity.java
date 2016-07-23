package com.wisen.wisenapp.btsmart.xiaomi;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wisen.wisenapp.R;
import com.wisen.wisenapp.btsmart.BTSmartUtil;
import com.wisen.wisenapp.btsmart.BtSmartService;
import com.wisen.wisenapp.btsmart.CharacteristicInfo;
import com.wisen.wisenapp.btsmart.ServiceInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HackXiaoMiActivity extends AppCompatActivity {

    private final String TAG = "HackXiaoMi";

    final byte[] green = {0x0e,0x04,0x05,0x00,0x01};
    final byte[] red = {0x0e,0x06,0x01,0x02,0x01};
    final byte[] blue = {0x0e,0x00,0x06,0x06,0x01};
    final byte[] orange = {0x0e,0x06,0x02,0x00,0x01};

    private List<BluetoothGattCharacteristic> mGattCharacterList = null;
    private BluetoothDevice mDeviceToConnect = null;
    //private ServiceInfo mServiceinfo = null;
    private BluetoothGattService mGattService = null;
    private BluetoothGatt mGatt = null;
    private BtSmartService mBTSmartService = null;
    private List<BluetoothGattService> mGattServiceList = null;

    //display charactistic list
    private static ArrayList<CharacteristicInfo> mCharacteristiclist = new ArrayList<CharacteristicInfo>();
    private static int index = 0;
    private static CharacteristicListAdapter mCharacteristiclistAdapter;
    ListView mCharacteristiclistView = null;
    //private static HashSet<String> mCharacteristicUUID = new HashSet<String>();
    private static HashMap<String, Integer> mCharacteristicIndex = new HashMap<String, Integer>();

    //private TextView GattServicename = null;
    //private TextView GattServiceUUID = null;
    private Button btn_write_xiaomi = null;
    private Button btn_vib_xiaomi = null;
    private Button btn_set_color = null;
    private Button btn_read_battery = null;
    private Button btn_vib_test2 = null;
    private Button btn_read_firmware_version = null;
    private static TextView battery_info = null;


    //5 main xiamo services and there are UUIDs
    //private BluetoothGattService XiaoMi_S1800 = null;
    private static final UUID XM_UUID_S1800 = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    //private BluetoothGattService XiaoMi_S1801 = null;
    private static final UUID XM_UUID_S1801 = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
    //private BluetoothGattService XiaoMi_S1802 = null;
    private static final UUID XM_UUID_S1802 = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    //private BluetoothGattService XiaoMi_Sfee0 = null;
    private static final UUID XM_UUID_Sfee0 = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb");
    //private BluetoothGattService XiaoMi_Sfee1 = null;
    private static final UUID XM_UUID_Sfee1 = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb");
    //private BluetoothGattService XiaoMi_Sfee7 = null;
    private static final UUID XM_UUID_Sfee7 = UUID.fromString("0000fee2-0000-1000-8000-00805f9b34fb");

    private static final UUID UUID_DESCRIPTOR_UPDATE_NOTIFICATION	= UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    //characteristic uuid list
    private static final UUID CHARA_UUID_ff03 = UUID.fromString("0000ff03-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARA_UUID_ff07 = UUID.fromString("0000ff07-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARA_UUID_ff01 = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    //ff04 userinfo
    private static final UUID CHARA_UUID_ff04 = UUID.fromString("0000ff04-0000-1000-8000-00805f9b34fb");
    final byte[] write_into_ff04_1 = {(byte)0xf0, 0x69, (byte)0xf8, 0x3a, 0x00, 0x34, (byte)0xa0, 0x3e, 0x01, 0x00, 0x00,
            0x2d, 0x31, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x59};
    //c8:0f:10:00:86:58
    final byte[] write_into_ff04_2 = {(byte)0xf0, 0x69, (byte)0xf8, 0x3a, 0x00, 0x34, (byte)0xa0, 0x3e, 0x01, 0x05, 0x00,
            0x2d, 0x31, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xb2};
    //ff0a read write date time
    private static final UUID CHARA_UUID_ff0a = UUID.fromString("0000ff0a-0000-1000-8000-00805f9b34fb");
    final byte[] write_into_ff0a_1 = {0x10, 0x06, 0x08, 0x0b, 0x12, 0x0b,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff};
    //private static final UUID CHARA_UUID_ff01 = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARA_UUID_ff0c = UUID.fromString("0000ff0c-0000-1000-8000-00805f9b34fb");
    //一般刚连上小米或者重连的时候要读一下ff0c,以获取小米的电池的信息
    //当小米充电完成的时候,小米手环会主动发送ff0c的Value Notification, 其中最后一位是03, 代表充电充满, 这个时候手机会震动一下, 应该是小米APP调用系统函数实现
    //ff0c对应的是手环的电池的各种信息 data format:
    //60 10 06 14 0b 0e 21 22 00 04
    //60-->96 电量96%
    //10-->16+2000=2016年
    //06-->6+1 = 7 月
    //14-->20=20日
    //0b-->11=11点
    //0e-->14=14分
    //21-->33=33秒
    //22-->34=充电循环次数
    //00--> 未知, 是不是循环次数的高位?
    //04--> 未充电的状态 02 充电ing 03 充满

    private static final UUID CHARA_UUID_ff06 = UUID.fromString("0000ff06-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARA_UUID_ff09 = UUID.fromString("0000ff09-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARA_UUID_ff05 = UUID.fromString("0000ff05-0000-1000-8000-00805f9b34fb");
    final byte[] write_into_ff05_1 = {0x05, 0x00, 0x40, 0x1f};
    final byte[] write_into_ff05_2 = {0x04, 0x00, 0x00, 0x0f, 0x0b, 0x1f, 0x08, 0x00, 0x28, 0x00, 0x1f};
    final byte[] write_into_ff05_3 = {0x04, 0x01, 0x00, 0x0f, 0x0b, 0x1f, 0x08, 0x00, 0x28, 0x00, 0x1f};
    final byte[] write_into_ff05_4 = {0x04, 0x02, 0x00, 0x0f, 0x0b, 0x1f, 0x08, 0x00, 0x28, 0x00, 0x1f};
    final byte[] write_into_ff05_5 = {0x0e, 0x00, 0x06, 0x06, 0x00};
    final byte[] write_into_ff05_6 = {0x0f, 0x00};
    //final byte[] write_into_ff05_7 = {0x00};
    final byte[] write_into_ff05_8 = {0x06};
    final byte[] write_into_ff05_9 = {0x0a, 0x10, 0x06, 0x08, 0x0b, 0x12, 0x0b, 0x00, 0x00};
    final byte[] write_into_ff05_red = {0x0e, 0x06, 0x01, 0x02, 0x01};
    final byte[] write_into_ff05_blue = {0x0e, 0x00, 0x06, 0x06, 0x01};
    final byte[] write_into_ff05_green = {0x0e, 0x04, 0x05, 0x00, 0x01};
    final byte[] write_into_ff05_orange = {0x0e, 0x06, 0x02, 0x00, 0x01};
    final byte[] write_into_ff05_vib_with_led = {0x08, 0x00};
    final byte[] write_into_ff05_vib_until_stop = {0x08, 0x01};
    final byte[] write_into_ff05_vib_without_led = {0x08, 0x03};
    final byte[] write_into_ff05_vib_stop = {0x19};

    private static final UUID CHARA_UUID_ff02 = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");
    final byte[] write_into_ff02_1 = {0x00};

    private static final UUID CHARA_UUID_ff0e = UUID.fromString("0000ff0e-0000-1000-8000-00805f9b34fb");

    private static final UUID CHARA_UUID_ff0d = UUID.fromString("0000ff0d-0000-1000-8000-00805f9b34fb");
    final byte[] write_into_ff0d_1 = {0x04, 0x13, (byte)0x82, 0x06, 0x03, (byte)0xce, (byte)0x86, 0x43,
            (byte)0x9d, 0x24, 0x6c, 0x35, 0x76, 0x78, (byte)0x8a, 0x18, (byte)0x9c};
    //service 1802
    private static final UUID CHARA_UUID_2a06 = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
    final byte[] write_into_2a06_1 = {0x03};

    private enum XIAOMI_COLOR
    {
        RED,BLUE,ORANGE,GREEN,
    }

    private void init_hank_sequence(){
        //这里之前没有加delay_time, 出现了大量的错误:writeCharacteristic: mDeviceBusy = true, and return false
        //后来看了sepc, spec上说了每一个request或者command都属于原子操作,同一时间只能完成一次事务,下面是原话:
        /*An attribute protocol request and response or indication-confirmation pair is
        considered a single transaction. A transaction shall always be performed on
        one ATT Bearer, and shall not be split over multiple ATT Bearers.*/
        //所以目前这种加延时的做法还是有问题, 应该做一个queue, 有request就加到queue中
        //当当前的事务完成了,再执行下一个事务的request
        //那么后面的任务就很明确了, 做一个queue, 添加事务完成的确认
        enable_notify(XM_UUID_Sfee0, CHARA_UUID_ff03);
        //delay_time(100);
        enable_notify(XM_UUID_Sfee0, CHARA_UUID_ff07);
        //delay_time(100);
        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff01);
        //delay_time(100);
        UserInfo userInfo = new UserInfo(989358576, 1, 32, 172, 80, "wisen_wang", 1);
        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff04, userInfo.getBytes(mDeviceToConnect.getAddress()));
        //write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff04, write_into_ff04_1);
        //write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff04, write_into_ff04_2);
        //delay_time(100);
        //write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff0a, write_into_ff0a_1);
        //delay_time(100);
        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff01);
        //delay_time(100);
        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff0c);
        //delay_time(100);
        enable_notify(XM_UUID_Sfee0, CHARA_UUID_ff0c);
        //delay_time(100);
        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff06);
        // delay_time(100);
        enable_notify(XM_UUID_Sfee0, CHARA_UUID_ff06);
        //delay_time(100);
        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff09);
        //delay_time(100);
        //ff02 device name???
        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff02);
    }

    //called after recive the ff03 report 0x15
    private void init_hank_sequence2(){

        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_1);
        //delay_time(100);
        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_2);
        //delay_time(100);
        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_3);
        //delay_time(100);
        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_4);
        //delay_time(100);

        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff01);
        //delay_time(100);

        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_5);
        //delay_time(100);
        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_6);
        //delay_time(100);
        //write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff02, write_into_ff02_1);
        //delay_time(100);

        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff0d, write_into_ff0d_1);
        //delay_time(100);

        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff01);
        //delay_time(100);

        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_8);
        //delay_time(100);
        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_9);
        //delay_time(100);

        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff0d);
        //delay_time(100);
        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff01);
        //delay_time(100);

        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_8);
        //delay_time(100);
        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_9);
    }

    private void vibration_xiaomi(){
        Log.d(TAG, "vibration_xiaomi");
        write_characteristic(XM_UUID_S1802, CHARA_UUID_2a06, write_into_2a06_1);
    }


    private boolean vib_xiaomi_test2_flag = true;
    private void vib_xiaomi_test2(){
        if(!vib_xiaomi_test2_flag){
            write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_vib_stop);
            vib_xiaomi_test2_flag = true;
        } else {
            write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_vib_until_stop);
            vib_xiaomi_test2_flag = false;
        }
    }

    private void set_color(XIAOMI_COLOR color){
        switch(color){
            case RED:
                write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_red);
                break;
            case BLUE:
                write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_blue);
                break;
            case ORANGE:
                write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_orange);
                break;
            case GREEN:
                write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_green);
                break;
            default:
                Log.d(TAG, "wrong color!!!");
                break;
        }
    }

    private void delay_time(int time){
        try{
            Thread.currentThread();
            Thread.sleep(time);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private TextView mTitle = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.CustomTheme);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hack_xiao_mi);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(TAG);
        mTitle = (TextView) findViewById(R.id.title_right_text);

        registerReceiver(mGattUpdateReceiver, BTSmartUtil.getmAdapterIntentFilter());

        Intent intent = getIntent();
        mDeviceToConnect = intent.getExtras().getParcelable(BluetoothDevice.EXTRA_DEVICE);
        Log.d(TAG, "device name=" + mDeviceToConnect.getName());
        //mServiceinfo = (ServiceInfo)intent.getParcelableExtra(ServiceInfo.ServiceInfoKeyString);
        //mGattService = mServiceinfo.getService();
        //Log.d(TAG,"name="+mServiceinfo.getName());
        //Log.d(TAG,"uuid="+mServiceinfo.getUUID());

        // Make a connection to BtSmartService to enable us to use its services.
        Intent bindIntent = new Intent(this, BtSmartService.class);
        bindService(bindIntent, mBTSmartServiceConnection, Context.BIND_AUTO_CREATE);

        mCharacteristiclistView = (ListView)findViewById(R.id.characteristicListView);
        mCharacteristiclistAdapter = new CharacteristicListAdapter(this, mCharacteristiclist);
        mCharacteristiclistView.setAdapter(mCharacteristiclistAdapter);
        mCharacteristiclistView.setOnItemClickListener(mCharacteristiclistClickListener);

        //GattServicename = (TextView)findViewById(R.id.GattServicename);
        //GattServicename.setText(mServiceinfo.getName());
        //GattServiceUUID = (TextView)findViewById(R.id.GattServiceUUID);
        //GattServiceUUID.setText(mServiceinfo.getUUID());

        btn_write_xiaomi = (Button)findViewById(R.id.btn_write_sequence);
        btn_write_xiaomi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mBTSmartService){
                    if(mBTSmartService.get_BtSmartState() == BtSmartService.BtSmartStateTYPE.BT_SMART_STATE_DISCONNECTED){
                        if (null != mBTSmartService){
                            mTitle.setText("Begin to reconn Gatt");
                            Log.e(TAG, "GATT service disconnected, state="+mBTSmartService.get_BtSmartState());
                            reconnectGatt();
                        }
                    } else if(mBTSmartService.get_BtSmartState() == BtSmartService.BtSmartStateTYPE.BT_SMART_STATE_CONNECTED){
                        init_hank_sequence();
                    }
                } else {
                    mTitle.setText("SmartSer not Conn");
                }
            }
        });

        btn_vib_test2 = (Button)findViewById(R.id.btn_vib_test2);
        btn_vib_test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mBTSmartService){
                    if(mBTSmartService.get_BtSmartState() == BtSmartService.BtSmartStateTYPE.BT_SMART_STATE_DISCONNECTED){
                        if (null != mBTSmartService){
                            mTitle.setText("Begin to reconn Gatt");
                            Log.e(TAG, "GATT service disconnected, state="+mBTSmartService.get_BtSmartState());
                            reconnectGatt();
                        }
                    } else if(mBTSmartService.get_BtSmartState() == BtSmartService.BtSmartStateTYPE.BT_SMART_STATE_CONNECTED){
                        vib_xiaomi_test2();
                    }
                } else {
                    mTitle.setText("SmartSer not Conn");
                }
            }
        });

        btn_vib_xiaomi = (Button)findViewById(R.id.btn_vib_xiaomi);
        btn_vib_xiaomi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mBTSmartService){
                    if(mBTSmartService.get_BtSmartState() == BtSmartService.BtSmartStateTYPE.BT_SMART_STATE_DISCONNECTED){
                        if (null != mBTSmartService){
                            mTitle.setText("Begin to reconn Gatt");
                            Log.e(TAG, "GATT service disconnected, state="+mBTSmartService.get_BtSmartState());
                            reconnectGatt();
                        }
                    } else if(mBTSmartService.get_BtSmartState() == BtSmartService.BtSmartStateTYPE.BT_SMART_STATE_CONNECTED){
                        vibration_xiaomi();
                    }
                } else {
                    mTitle.setText("SmartSer not Conn");
                }
            }
        });

        battery_info = (TextView)findViewById(R.id.battery_info);

        btn_read_battery = (Button)findViewById(R.id.btn_read_battery_info);
        btn_read_battery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mBTSmartService){
                    if(mBTSmartService.get_BtSmartState() == BtSmartService.BtSmartStateTYPE.BT_SMART_STATE_DISCONNECTED){
                        if (null != mBTSmartService){
                            mTitle.setText("Begin to reconn Gatt");
                            Log.e(TAG, "GATT service disconnected, state="+mBTSmartService.get_BtSmartState());
                            reconnectGatt();
                        }
                    } else if(mBTSmartService.get_BtSmartState() == BtSmartService.BtSmartStateTYPE.BT_SMART_STATE_CONNECTED){
                        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff0c);
                    }
                } else {
                    mTitle.setText("SmartSer not Conn");
                }
            }
        });
        btn_read_firmware_version = (Button)findViewById(R.id.btn_read_firmware_version);
        btn_read_firmware_version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mBTSmartService){
                    if(mBTSmartService.get_BtSmartState() == BtSmartService.BtSmartStateTYPE.BT_SMART_STATE_DISCONNECTED){
                        if (null != mBTSmartService){
                            mTitle.setText("Begin to reconn Gatt");
                            Log.e(TAG, "GATT service disconnected, state="+mBTSmartService.get_BtSmartState());
                            reconnectGatt();
                        }
                    } else if(mBTSmartService.get_BtSmartState() == BtSmartService.BtSmartStateTYPE.BT_SMART_STATE_CONNECTED){
                        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff01);
                    }
                } else {
                    mTitle.setText("SmartSer not Conn");
                }
            }
        });

        btn_set_color = (Button)findViewById(R.id.btn_set_color);
        btn_set_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mBTSmartService){
                    if(mBTSmartService.get_BtSmartState() == BtSmartService.BtSmartStateTYPE.BT_SMART_STATE_DISCONNECTED){
                        if (null != mBTSmartService){
                            mTitle.setText("Begin to reconn Gatt");
                            Log.e(TAG, "GATT service disconnected, state="+mBTSmartService.get_BtSmartState());
                            reconnectGatt();
                        }
                    } else if(mBTSmartService.get_BtSmartState() == BtSmartService.BtSmartStateTYPE.BT_SMART_STATE_CONNECTED){
                        set_color(XIAOMI_COLOR.BLUE);
                        delay_time(2000);
                        set_color(XIAOMI_COLOR.RED);
                        delay_time(2000);
                        set_color(XIAOMI_COLOR.ORANGE);
                        delay_time(2000);
                        set_color(XIAOMI_COLOR.GREEN);
                    }
                } else {
                    mTitle.setText("SmartSer not Conn");
                }
            }
        });
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BtSmartService.ACTION_GATT_CONNECTED.equals(action)) {
                mTitle.setText("Gatt Connected");
                mGatt = mBTSmartService.get_Gatt();
                mGattServiceList = mBTSmartService.get_GattServiceList();
                mBTSmartService.registerGattServiceHandler(mGattServiceHandler);
                if(null != mGatt && null != mGattServiceList) {
                    for(BluetoothGattService gattService:mGattServiceList){
                        if (gattService.getUuid().equals(XM_UUID_Sfee0)){
                            mGattCharacterList = gattService.getCharacteristics();
                            if (null != mGattCharacterList) {
                                //fill data into adapter
                                fill_data_into_adapter(mGattCharacterList);
                            } else {
                                Log.e(TAG, "get BluetoothGattCharacteristic list fail!!");
                            }
                            break;
                        }
                    }
                } else {
                    Log.e(TAG, "get_Gatt or get_GattServiceList fail!!!");
                }
            } else if (BtSmartService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //Log.e(TAG, "GATT service disconnected, reconnect it!!");
                mTitle.setText("Gatt DisConnected");
            } else if (BtSmartService.
                    ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the
                // user interface.
            } else if (BtSmartService.ACTION_DATA_AVAILABLE.equals(action)) {
            }
        }
    };

    private void reconnectGatt(){
        Log.e(TAG, "begin to reconnect Gatt.");
        mGattCharacterList = null;
        mGattServiceList = null;
        mCharacteristiclist.clear();
        mCharacteristicIndex.clear();
        index=0;
        mCharacteristiclistAdapter.notifyDataSetChanged();
        if (null != mBTSmartService){
            if (null != mDeviceToConnect){
                mBTSmartService.connectAsClient(mDeviceToConnect, null);
            }
        }
    }

    public void onDestroy() {
        unregisterReceiver(mGattUpdateReceiver);
        Toast.makeText(this, "Disconnected from device:" + mDeviceToConnect.getName(), Toast.LENGTH_LONG).show();
        if (null != mGatt)
            mGatt.close();
        if(null != mBTSmartService){
            mBTSmartService.disconnect();
        }
        unbindService(mBTSmartServiceConnection);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        if(null != mBTSmartService && mBTSmartService.get_BtSmartState() == BtSmartService.BtSmartStateTYPE.BT_SMART_STATE_DISCONNECTED){
            mTitle.setText("BTSMartService DisConnected");
            if (null != mBTSmartService){
                Log.e(TAG, "GATT service disconnected, state="+mBTSmartService.get_BtSmartState());
                reconnectGatt();
            }
        }
    }

    private ServiceConnection mBTSmartServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mBTSmartService = ((BtSmartService.LocalBinder) rawBinder).getService();
            if (mBTSmartService != null) {
                mBTSmartService.connectAsClient(mDeviceToConnect, null);
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            Log.e(TAG, "onServiceDisconnected");
            mBTSmartService = null;
        }
    };

    private boolean fill_data_into_adapter(List<BluetoothGattCharacteristic> gattCharacterList){
        Log.d(TAG, "fill_data_into_adapter");
        for (BluetoothGattCharacteristic gattCharacteristic:gattCharacterList){
            //CharacteristicInfo info = new CharacteristicInfo(gattCharacteristic.getUuid().toString(),
            //String.valueOf(gattCharacteristic.getProperties()),"Refresh");
            //if (!mCharacteristicUUID.contains(gattCharacteristic.getUuid().toString())){
            if (!mCharacteristicIndex.containsKey(gattCharacteristic.getUuid().toString())){
                CharacteristicInfo info = new CharacteristicInfo(gattCharacteristic.getUuid().toString(),
                        BTSmartUtil.getPropertiesStr(gattCharacteristic.getProperties()),"Refresh");
                Log.d(TAG, "add UUID:" + gattCharacteristic.getUuid().toString());
                Log.d(TAG, "add index:" + index);
                //mCharacteristicUUID.add(gattCharacteristic.getUuid().toString());
                mCharacteristiclist.add(index, info);
                mCharacteristicIndex.put(gattCharacteristic.getUuid().toString(), index);
                Log.d(TAG, "size: " + mCharacteristiclist.size());
                index++;
                mCharacteristiclistAdapter.notifyDataSetChanged();
            }
        }
        return true;
    }


    private final GattServiceHandler mGattServiceHandler = new GattServiceHandler(this);

    private static class GattServiceHandler extends Handler {
        private final String TAG = "BTSmart.GattSHandler";
        private final WeakReference<HackXiaoMiActivity> mActivity;

        public GattServiceHandler(HackXiaoMiActivity activity) {
            mActivity = new WeakReference<HackXiaoMiActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            HackXiaoMiActivity parentActivity = mActivity.get();
            if (parentActivity != null) {

                Log.d(TAG, "handleMessage:" + msg.what);
                switch (msg.what) {
                    case BtSmartService.MESSAGE_CHARACTERISTIC_VALUE: {
                        Bundle msgExtra = msg.getData();
                        UUID characteristicUuid = ((ParcelUuid) msgExtra
                                .getParcelable(BtSmartService.EXTRA_CHARACTERISTIC_UUID)).getUuid();
                        byte[] value = msgExtra.getByteArray(BtSmartService.EXTRA_VALUE);
                        int properties = msgExtra.getInt(BtSmartService.EXTRA_PROPERTIES, 0);

                        if(CHARA_UUID_ff0c.equals(characteristicUuid)){
                            if(value.length == 10){
                                String str = "";
                                int year = value[1] + 2000;
                                int month = value[2] + 1;
                                int day = value[3];
                                int hour = value[4];
                                int mini = value[5];
                                int seconds = value[6];
                                String status = "unknown";
                                switch (value[9]){
                                    case 1:
                                        status = "Low";
                                        break;
                                    case 2:
                                        status = "Charging...";
                                        break;
                                    case 3:
                                        status = "Full";
                                        break;
                                    case 4:
                                        status = "UnCharge";
                                        break;
                                }
                                str += "Battery percent:"+value[0]+"%\n";
                                str += "Last changed time:"+year+"-"+month+"-"+day+" "
                                    +hour+":"+mini+":"+seconds+"\n";
                                str += "Change "+value[7]+" times\n";
                                str += "Current status:"+status;
                                battery_info.setText(str);
                            } else {
                                Log.d(TAG, "battery data format error!");
                            }
                        } else if(CHARA_UUID_ff01.equals(characteristicUuid)){
                            if(value.length == 16){
                                String str = "";
                                str += "XiaoMI firmware version:\n";
                                str += value[15];
                                str += ".";
                                str += value[14];
                                str += ".";
                                str += value[13];
                                str += ".";
                                str += value[12];
                                battery_info.setText(str);
                            } else {
                                Log.d(TAG, "firmware version format error!");
                            }
                        } else {
                            Log.d(TAG, "characteristicUuid:"+characteristicUuid);
                            Log.d(TAG, "value:"+value[0]);
                        }
                        // and characteristicUuid tell you which characteristic
                        // the value belongs to.
/*
                        CharacteristicInfo info = new CharacteristicInfo(characteristicUuid.toString(),
                                BTSmartUtil.getPropertiesStr(properties), new String(value));
                        if (mCharacteristiclist.contains(info)){
                            Log.d(TAG, "UUID: " + characteristicUuid.toString());
                            Log.d(TAG, "index: " + mCharacteristicIndex.get(characteristicUuid.toString()));
                            mCharacteristiclist.set(mCharacteristicIndex.get(characteristicUuid.toString()), info);
                            Log.d(TAG, "size: " + mCharacteristiclist.size());
                            mCharacteristiclistAdapter.notifyDataSetChanged();
                        }
*/
                        break;
                    }
                    case BtSmartService.MESSAGE_DISCONNECTED: {
                        // End this activity and go back to scan results view.
                        //parentActivity.finish();
                        break;
                    }
                    case BtSmartService.MESSAGE_REQUEST_FAILED: {
                        Log.d(TAG, "request failed!!!");
                        break;
                    }
                    case BtSmartService.MESSAGE_CHARACTERISTIC_CHANGE:{
                        Log.d(TAG, "MESSAGE_CHARACTERISTIC_CHANGE");
                        Bundle msgExtra = msg.getData();
                        UUID characteristicUuid = ((ParcelUuid) msgExtra
                                .getParcelable(BtSmartService.EXTRA_CHARACTERISTIC_UUID)).getUuid();
                        byte[] value = msgExtra.getByteArray(BtSmartService.EXTRA_VALUE);
                        Log.d(TAG, "value = " + value[0]);
                        if(characteristicUuid.equals(CHARA_UUID_ff03) && value[0] == 0x15){
                            Log.d(TAG, "now we can start init_hank_sequence2...");
                            parentActivity.init_hank_sequence2();
                        } else if (characteristicUuid.equals(CHARA_UUID_ff06)){
                            battery_info.setText("Steps: " + value[0]);
                        }else {
                            Log.d(TAG, "Notification: " + characteristicUuid + " "+value[0]);
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * The adapter that allows the contents of ServiceInfo objects to be displayed in the ListView. The service name,
     * service UUID specified in servicelist.xml are displayed.
     */
    private class CharacteristicListAdapter extends BaseAdapter {
        private Activity activity;
        private ArrayList<CharacteristicInfo> data;
        private LayoutInflater inflater = null;

        public CharacteristicListAdapter(Activity a, ArrayList<CharacteristicInfo> object) {
            activity = a;
            data = object;
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return data.size();
        }

        public Object getItem(int position) {
            return data.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            if (convertView == null)
                vi = inflater.inflate(R.layout.characteristiclist, null);

            TextView characteristicUUIDText = (TextView) vi.findViewById(R.id.characteristic_UUID);
            TextView characteristicprpertiesText = (TextView) vi.findViewById(R.id.characteristic_prperties);
            TextView characteristicvaluesText = (TextView) vi.findViewById(R.id.characteristic_values);
            //ImageView read_icon = (ImageView) vi.findViewById(R.id.read_action);
            // ImageView write_icon = (ImageView) vi.findViewById(R.id.write_action);

            CharacteristicInfo info = (CharacteristicInfo) data.get(position);
            characteristicUUIDText.setText(info.getUUID());
            characteristicprpertiesText.setText(info.getProperties());
            characteristicvaluesText.setText(info.getValues());
            return vi;
        }
    }

    /**
     * The on-click listener for selecting a characteristic.
     */
    private AdapterView.OnItemClickListener mCharacteristiclistClickListener = new AdapterView.OnItemClickListener() {



        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final CharacteristicInfo info = (CharacteristicInfo)mCharacteristiclistAdapter.getItem(position);
            Log.d(TAG, "position = " + position);
            Log.d(TAG, "CharacteristicInfo UUID = " + info.getUUID());
            Log.d(TAG, "CharacteristicInfo Proper = " + info.getProperties());
            if(info.getProperties().contains("w")){
                AlertDialog.Builder dlg = new AlertDialog.Builder(HackXiaoMiActivity.this);
                final EditText devNameEdit = new EditText(HackXiaoMiActivity.this);
                dlg.setView(devNameEdit);
                dlg.setTitle("请写入新值");
                dlg.setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(devNameEdit.getText().toString().length() != 0) {
                            //byte[] value = null;
                            for(BluetoothGattCharacteristic gattCharacteristic:mGattCharacterList){
                                if(gattCharacteristic.getUuid().toString().equals(info.getUUID())){
                                    if ("green".equals(devNameEdit.getText().toString())){
                                        Log.d(TAG, "write green value to xiaomi");
                                        gattCharacteristic.setValue(green);
                                    } else if ("red".equals(devNameEdit.getText().toString())){
                                        Log.d(TAG, "write red value to xiaomi");
                                        gattCharacteristic.setValue(green);
                                    } else if ("blue".equals(devNameEdit.getText().toString())){
                                        Log.d(TAG, "write blue value to xiaomi");
                                        gattCharacteristic.setValue(green);
                                    } else if ("orange".equals(devNameEdit.getText().toString())){
                                        Log.d(TAG, "write orange value to xiaomi");
                                        gattCharacteristic.setValue(green);
                                    }else {
                                        Log.d(TAG, "write default value to xiaomi");
                                        gattCharacteristic.setValue(green);
                                    }
                                    mGatt.writeCharacteristic(gattCharacteristic);
                                }
                            }
                        } else {
                            Log.d(TAG, "Please enter a value!!");
                        }

                    }
                });
                dlg.create();
                dlg.show();
            } else {
                for(BluetoothGattCharacteristic gattCharacteristic:mGattCharacterList){
                    if(gattCharacteristic.getUuid().toString().equals(info.getUUID())){
                        Log.d(TAG, "start to read Characteristic");
                        mGatt.readCharacteristic(gattCharacteristic);
                    }
                }
            }
        }
    };

    //you must ensure the characteristic_uuid include into the gatt service
    //你必须确定gatt service包含了这个characteristic
    private void write_characteristic(UUID service_uuid, UUID characteristic_uuid, byte[] value) {
        if(null != mGatt && null != mBTSmartService) {
            Log.d(TAG, "write_characteristic:"+" "+ service_uuid + " "+characteristic_uuid);
            mBTSmartService.requestCharacteristicWrite(1, service_uuid, characteristic_uuid, value, mGattServiceHandler);
            /*
            BluetoothGattCharacteristic chara = mGatt.getService(service_uuid).getCharacteristic(characteristic_uuid);
            if(null != chara){
                chara.setValue(value);
                mGatt.writeCharacteristic(chara);
            }
            */
        }
    }

    private void enable_notify(UUID service_uuid, UUID characteristic_uuid){
        if(null != mGatt && null != mBTSmartService) {
            Log.d(TAG, "enable_notify:"+" "+service_uuid+" "+characteristic_uuid+" "+UUID_DESCRIPTOR_UPDATE_NOTIFICATION);
            mBTSmartService.requestDescriptionWrite(2, service_uuid, characteristic_uuid, UUID_DESCRIPTOR_UPDATE_NOTIFICATION, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, mGattServiceHandler);
            /*
            BluetoothGattCharacteristic chara = mGatt.getService(service_uuid).getCharacteristic(characteristic_uuid);
            if(null != chara){
                BluetoothGattDescriptor descriptor = chara.getDescriptor(UUID_DESCRIPTOR_UPDATE_NOTIFICATION);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mGatt.writeDescriptor(descriptor);
            }
            */
        }
    }

    private void read_characteristic(UUID service_uuid, UUID characteristic_uuid) {
        if(null != mGatt && null != mBTSmartService) {
            Log.d(TAG, "write_characteristic:"+characteristic_uuid+" " + characteristic_uuid);
            mBTSmartService.requestCharacteristicValue(0, service_uuid, characteristic_uuid, mGattServiceHandler);

            /*BluetoothGattCharacteristic chara = mGatt.getService(service_uuid).getCharacteristic(characteristic_uuid);
            if(null != chara){
                mGatt.readCharacteristic(chara);
                //Log.d(TAG, "value="+chara.getValue());
            }*/
        }
    }
}
