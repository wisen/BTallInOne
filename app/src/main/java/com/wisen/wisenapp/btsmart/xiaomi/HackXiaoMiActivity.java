package com.wisen.wisenapp.btsmart.xiaomi;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
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
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BtSmartService.ACTION_GATT_CONNECTED.equals(action)) {

            } else if (BtSmartService.ACTION_GATT_DISCONNECTED.equals(action)) {
                finish();
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

        final byte[] green = {0x0e,0x04,0x05,0x00,0x01};
        final byte[] red = {0x0e,0x06,0x01,0x02,0x01};
        final byte[] blue = {0x0e,0x00,0x06,0x06,0x01};
        final byte[] orange = {0x0e,0x06,0x02,0x00,0x01};

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
}
