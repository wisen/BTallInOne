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
    private ServiceInfo mServiceinfo = null;
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

    private TextView GattServicename = null;
    private TextView GattServiceUUID = null;
    private Button btn_write_xiaomi = null;
    private Button btn_vib_xiaomi = null;
    private Button btn_set_color = null;


    //5 main xiamo services and there are UUIDs
    private BluetoothGattService XiaoMi_S1800 = null;
    private static final UUID XM_UUID_S1800 = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    private BluetoothGattService XiaoMi_S1801 = null;
    private static final UUID XM_UUID_S1801 = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
    private BluetoothGattService XiaoMi_S1802 = null;
    private static final UUID XM_UUID_S1802 = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    private BluetoothGattService XiaoMi_Sfee0 = null;
    private static final UUID XM_UUID_Sfee0 = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb");
    private BluetoothGattService XiaoMi_Sfee1 = null;
    private static final UUID XM_UUID_Sfee1 = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb");
    private BluetoothGattService XiaoMi_Sfee7 = null;
    private static final UUID XM_UUID_Sfee7 = UUID.fromString("0000fee2-0000-1000-8000-00805f9b34fb");

    private static final UUID UUID_DESCRIPTOR_UPDATE_NOTIFICATION	= UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    //characteristic uuid list
    private static final UUID CHARA_UUID_ff03 = UUID.fromString("0000ff03-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARA_UUID_ff07 = UUID.fromString("0000ff07-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARA_UUID_ff01 = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARA_UUID_ff04 = UUID.fromString("0000ff04-0000-1000-8000-00805f9b34fb");
    final byte[] write_into_ff04_1 = {(byte)0xf0, 0x69, (byte)0xf8, 0x3a, 0x00, 0x34, (byte)0xa0, 0x3e, 0x01, 0x00, 0x00,
            0x2d, 0x31, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x59};
    private static final UUID CHARA_UUID_ff0a = UUID.fromString("0000ff0a-0000-1000-8000-00805f9b34fb");
    final byte[] write_into_ff0a_1 = {0x10, 0x06, 0x08, 0x0b, 0x12, 0x0b,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff};
    //private static final UUID CHARA_UUID_ff01 = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARA_UUID_ff0c = UUID.fromString("0000ff0c-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARA_UUID_ff06 = UUID.fromString("0000ff06-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARA_UUID_ff09 = UUID.fromString("0000ff09-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARA_UUID_ff05 = UUID.fromString("0000ff05-0000-1000-8000-00805f9b34fb");
    final byte[] write_into_ff05_1 = {0x05, 0x00, 0x40, 0x1f};
    final byte[] write_into_ff05_2 = {0x04, 0x00, 0x00, 0x0f, 0x0b, 0x1f, 0x08, 0x00, 0x28, 0x00, 0x1f};
    final byte[] write_into_ff05_3 = {0x04, 0x01, 0x00, 0x0f, 0x0b, 0x1f, 0x08, 0x00, 0x28, 0x00, 0x1f};
    final byte[] write_into_ff05_4 = {0x04, 0x02, 0x00, 0x0f, 0x0b, 0x1f, 0x08, 0x00, 0x28, 0x00, 0x1f};
    final byte[] write_into_ff05_5 = {0x0e, 0x00, 0x06, 0x06, 0x00};
    final byte[] write_into_ff05_6 = {0x0f, 0x00};
    final byte[] write_into_ff05_7 = {0x00};
    final byte[] write_into_ff05_8 = {0x06};
    final byte[] write_into_ff05_9 = {0x0a, 0x10, 0x06, 0x08, 0x0b, 0x12, 0x0b, 0x00, 0x00};
    final byte[] write_into_ff05_red = {0x0e, 0x06, 0x01, 0x02, 0x01};
    final byte[] write_into_ff05_blue = {0x0e, 0x00, 0x06, 0x06, 0x01};
    final byte[] write_into_ff05_green = {0x0e, 0x04, 0x05, 0x00, 0x01};
    final byte[] write_into_ff05_orange = {0x0e, 0x06, 0x02, 0x00, 0x01};

    private static final UUID CHARA_UUID_ff0e = UUID.fromString("0000ff0e-0000-1000-8000-00805f9b34fb");
    final byte[] write_into_ff0e_1 = {0x04, 0x13, (byte)0x82, 0x06, 0x03, (byte)0xce, (byte)0x86, 0x43,
            (byte)0x9d, 0x24, 0x6c, 0x35, 0x76, 0x78, (byte)0x8a, 0x18, (byte)0x9c};

    private static final UUID CHARA_UUID_ff0d = UUID.fromString("0000ff0d-0000-1000-8000-00805f9b34fb");

    //service 1802
    private static final UUID CHARA_UUID_2a06 = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
    final byte[] write_into_2a06_1 = {0x03};

    private enum XIAOMI_COLOR
    {
        RED,BLUE,ORANGE,GREEN,
    }

    private void init_hank_sequence(){
        enable_notify(XM_UUID_Sfee0, CHARA_UUID_ff03);
        enable_notify(XM_UUID_Sfee0, CHARA_UUID_ff07);

        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff01);

        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff04, write_into_ff04_1);
        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff0a, write_into_ff0a_1);

        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff01);
        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff0c);

        enable_notify(XM_UUID_Sfee0, CHARA_UUID_ff0c);
        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff06);
        enable_notify(XM_UUID_Sfee0, CHARA_UUID_ff06);
        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff09);

        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_1);
        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_2);
        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_3);
        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_4);

        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff01);

        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_5);
        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_6);
        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_7);

        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff0e, write_into_ff0e_1);

        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff01);

        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_8);
        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_9);

        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff0d);
        read_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff01);

        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_8);
        write_characteristic(XM_UUID_Sfee0, CHARA_UUID_ff05, write_into_ff05_9);
    }

    private void vibration_xiaomi(){
        write_characteristic(XM_UUID_S1802, CHARA_UUID_2a06, write_into_2a06_1);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hack_xiao_mi);

        registerReceiver(mGattUpdateReceiver, BTSmartUtil.getmAdapterIntentFilter());

        Intent intent = getIntent();
        mDeviceToConnect = intent.getExtras().getParcelable(BluetoothDevice.EXTRA_DEVICE);
        Log.d(TAG, "device name=" + mDeviceToConnect.getName());
        mServiceinfo = (ServiceInfo)intent.getParcelableExtra(ServiceInfo.ServiceInfoKeyString);
        //mGattService = mServiceinfo.getService();
        Log.d(TAG,"name="+mServiceinfo.getName());
        Log.d(TAG,"uuid="+mServiceinfo.getUUID());

        // Make a connection to BtSmartService to enable us to use its services.
        Intent bindIntent = new Intent(this, BtSmartService.class);
        bindService(bindIntent, mBTSmartServiceConnection, Context.BIND_AUTO_CREATE);

        mCharacteristiclistView = (ListView)findViewById(R.id.characteristicListView);
        mCharacteristiclistAdapter = new CharacteristicListAdapter(this, mCharacteristiclist);
        mCharacteristiclistView.setAdapter(mCharacteristiclistAdapter);
        mCharacteristiclistView.setOnItemClickListener(mCharacteristiclistClickListener);

        GattServicename = (TextView)findViewById(R.id.GattServicename);
        GattServicename.setText(mServiceinfo.getName());
        GattServiceUUID = (TextView)findViewById(R.id.GattServiceUUID);
        GattServiceUUID.setText(mServiceinfo.getUUID());

        btn_write_xiaomi = (Button)findViewById(R.id.btn_write_sequence);
        btn_write_xiaomi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init_hank_sequence();
            }
        });

        btn_vib_xiaomi = (Button)findViewById(R.id.btn_vib_xiaomi);
        btn_vib_xiaomi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibration_xiaomi();
            }
        });

        btn_set_color = (Button)findViewById(R.id.btn_set_color);
        btn_set_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set_color(XIAOMI_COLOR.BLUE);
                delay_time(2000);
                set_color(XIAOMI_COLOR.RED);
                delay_time(2000);
                set_color(XIAOMI_COLOR.ORANGE);
                delay_time(2000);
                set_color(XIAOMI_COLOR.GREEN);


            }
        });
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BtSmartService.ACTION_GATT_CONNECTED.equals(action)) {

            } else if (BtSmartService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //finish();
                /*
                if (mBTSmartService != null) {
                    // We have a connection to BtSmartService so now we can connect
                    // and register the device handler.
                    if (null != mDeviceToConnect)
                        mBTSmartService.connectAsClient(mDeviceToConnect, null);
                }*/
            } else if (BtSmartService.
                    ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the
                // user interface.
            } else if (BtSmartService.ACTION_DATA_AVAILABLE.equals(action)) {
            }
        }
    };

    public void onDestroy() {
        unregisterReceiver(mGattUpdateReceiver);
        Toast.makeText(this, "Disconnected from device:" + mDeviceToConnect.getName(), Toast.LENGTH_LONG).show();
        mGatt.close();
        if(null != mBTSmartService){
            mBTSmartService.disconnect();
        }
        unbindService(mBTSmartServiceConnection);
        super.onDestroy();
    }

    private ServiceConnection mBTSmartServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mBTSmartService = ((BtSmartService.LocalBinder) rawBinder).getService();
            if (mBTSmartService != null) {
                mGatt = mBTSmartService.get_Gatt();
                mGattServiceList = mBTSmartService.get_GattServiceList();
                mBTSmartService.registerGattServiceHandler(mGattServiceHandler);
                if(null != mGatt && null != mGattServiceList) {
                    for(BluetoothGattService gattService:mGattServiceList){
                        if (gattService.getUuid().toString().equals(mServiceinfo.getUUID())){
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

            }
        }

        public void onServiceDisconnected(ComponentName classname) {
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
                        // This code is executed when a value is received in
                        // response to a direct get or a notification.
                        Bundle msgExtra = msg.getData();
                        //UUID serviceUuid = ((ParcelUuid) msgExtra.getParcelable(BtSmartService.EXTRA_SERVICE_UUID))
                        //        .getUuid();
                        UUID characteristicUuid = ((ParcelUuid) msgExtra
                                .getParcelable(BtSmartService.EXTRA_CHARACTERISTIC_UUID)).getUuid();
                        byte[] value = msgExtra.getByteArray(BtSmartService.EXTRA_VALUE);
                        int properties = msgExtra.getInt(BtSmartService.EXTRA_PROPERTIES, 0);

                        // and characteristicUuid tell you which characteristic
                        // the value belongs to.

                        CharacteristicInfo info = new CharacteristicInfo(characteristicUuid.toString(),
                                BTSmartUtil.getPropertiesStr(properties), new String(value));
                        if (mCharacteristiclist.contains(info)){
                            Log.d(TAG, "UUID: " + characteristicUuid.toString());
                            Log.d(TAG, "index: " + mCharacteristicIndex.get(characteristicUuid.toString()));
                            mCharacteristiclist.set(mCharacteristicIndex.get(characteristicUuid.toString()), info);
                            Log.d(TAG, "size: " + mCharacteristiclist.size());
                            mCharacteristiclistAdapter.notifyDataSetChanged();
                        }

                        break;
                    }
                    case BtSmartService.MESSAGE_DISCONNECTED: {
                        // End this activity and go back to scan results view.
                        parentActivity.finish();
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
        if(null != mGatt) {
            BluetoothGattCharacteristic chara = mGatt.getService(service_uuid).getCharacteristic(characteristic_uuid);
            if(null != chara){
                chara.setValue(value);
                mGatt.writeCharacteristic(chara);
            }
        }
    }

    private void enable_notify(UUID service_uuid, UUID characteristic_uuid){
        if(null != mGatt) {
            BluetoothGattCharacteristic chara = mGatt.getService(service_uuid).getCharacteristic(characteristic_uuid);
            if(null != chara){
                BluetoothGattDescriptor descriptor = chara.getDescriptor(UUID_DESCRIPTOR_UPDATE_NOTIFICATION);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mGatt.writeDescriptor(descriptor);
            }
        }
    }

    private void read_characteristic(UUID service_uuid, UUID characteristic_uuid) {
        if(null != mGatt) {
            BluetoothGattCharacteristic chara = mGatt.getService(service_uuid).getCharacteristic(characteristic_uuid);
            if(null != chara){
                mGatt.readCharacteristic(chara);
                //Log.d(TAG, "value="+chara.getValue());
            }
        }
    }
}
