package com.wisen.wisenapp.btsmart;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wisen.wisenapp.MainActivity;
import com.wisen.wisenapp.R;
import com.wisen.wisenapp.btsmart.xiaomi.HackXiaoMiActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class BTSmartActivity extends AppCompatActivity {

    private final String TAG = "BTSmart";

    public static final String GATTServiceInfo = "GATTServiceInfo";

    private BluetoothDevice mDeviceToConnect = null;
    public static BtSmartService mService = null;
    private TextView mStatusText;
    //private Button btn_read_Characteristic = null;
    private List<BluetoothGattService> mGattServiceList = null;
    private static BluetoothGattService mGattService = null;
    private static BluetoothGatt mGatt = null;

    //display service list
    private static ArrayList<ServiceInfo> mServicelist = new ArrayList<ServiceInfo>();
    private static ServiceListAdapter mServicelistAdapter;
    ListView mServiceListView = null;
    private static HashSet<String> mServiceUUID = new HashSet<String>();
    public static BluetoothGattService getGattService(){
        return mGattService;
    }
    public static BluetoothGatt getGatt() {
        return mGatt;
    }
    public static BtSmartService getBTSmartService(){
        return mService;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btsmart);

        mServiceListView = (ListView)findViewById(R.id.serviceListView);
        mServicelistAdapter = new ServiceListAdapter(this, mServicelist);
        mServiceListView.setAdapter(mServicelistAdapter);
        mServiceListView.setOnItemClickListener(mServicelistClickListener);


        registerReceiver(mGattUpdateReceiver,BTSmartUtil.getmAdapterIntentFilter());

        mStatusText = (TextView) findViewById(R.id.statusText);
        //btn_read_Characteristic = (Button)findViewById(R.id.btn_read_Characteristic);
        //btn_read_Characteristic.setVisibility();
        //btn_read_Characteristic.setEnabled(false);
        /*
        btn_read_Characteristic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mService){
                    mGatt = mService.get_Gatt();
                    if (null != mCharacterList && null != mGattServiceList) {
                        mCharacterList = mService.get_CharacteristicList();
                        mGattServiceList = mService.get_GattServiceList();
                        if (null != mGatt){
                            for (int j = 0; j < mCharacterList.size(); j++) {
                                Log.d(TAG,
                                        "---CharacterName:"
                                                + mCharacterList.get(j).getUuid());
                                Log.d(TAG,
                                        "---CharacterValue:"
                                                + mGatt.readCharacteristic(mCharacterList.get(j)));
                            }
                        }
                    }
                }
            }
        });*/
        // Show a back button in the action bar.
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the device to connect to that was passed to us by the scan
        // results Activity.
        Intent intent = getIntent();
        mDeviceToConnect = intent.getExtras().getParcelable(BluetoothDevice.EXTRA_DEVICE);
        mStatusText.setText(getString(R.string.connecting_status) + " to " + mDeviceToConnect.getName());

        // Make a connection to BtSmartService to enable us to use its services.
        Intent bindIntent = new Intent(this, BtSmartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((BtSmartService.LocalBinder) rawBinder).getService();
            if (mService != null) {
                // We have a connection to BtSmartService so now we can connect
                // and register the device handler.
                mService.connectAsClient(mDeviceToConnect, mDeviceHandler);
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BtSmartService.ACTION_GATT_CONNECTED.equals(action)) {
                // Update status to show that we are connected.
                String str = mDeviceToConnect.getName();
                str += " ";
                str += getString(R.string.connected_status);
                Log.d(TAG, "str = " + getString(R.string.connected_status));
                mStatusText.setText(str);
                //parentActivity.btn_read_Characteristic.setEnabled(true);
                // TODO: Your code here. Request read/write of
                // characteristics, or register for notifications.
                // Access these functions via the smartService
                // reference.
                // Pass mValueHandler as the handler when calling these
                // request functions.
                mGattServiceList = mService.get_GattServiceList();
                ServiceInfo serviceinfo = null;
                if (null != mGattServiceList){
                    for (int i = 0; i < mGattServiceList.size(); i++) {
                        BluetoothGattService theService = mGattServiceList.get(i);
                        Log.d(TAG, "ServiceName:" + theService.getUuid());
                        Log.d(TAG, "theService:" + theService.toString());
                        serviceinfo = new ServiceInfo(theService.toString(),theService.getUuid().toString()/*, theService*/);
                        if (!mServiceUUID.contains(theService.getUuid().toString())){
                            mServiceUUID.add(theService.getUuid().toString());
                            mServicelist.add(serviceinfo);
                            mServicelistAdapter.notifyDataSetChanged();
                        }
                    }
                }
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
    /**
     * This is the handler for general messages about the connection.
     */
    private final DeviceHandler mDeviceHandler = new DeviceHandler(this);

    private static class DeviceHandler extends Handler {
        private final String TAG = "BTSmart.DeviceHandler";
        private final WeakReference<BTSmartActivity> mActivity;

        public DeviceHandler(BTSmartActivity activity) {
            mActivity = new WeakReference<BTSmartActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BTSmartActivity parentActivity = mActivity.get();
            if (parentActivity != null) {
                BtSmartService smartService = mActivity.get().mService;
                Log.d(TAG, "handleMessage: msg.what=" + msg.what);
                switch (msg.what) {
                    case BtSmartService.MESSAGE_CONNECTED: {
                        // Update status to show that we are connected.
                        String str = parentActivity.mDeviceToConnect.getName();
                        str += " ";
                        str += parentActivity.getString(R.string.connected_status);
                        Log.d(TAG, "str = " + parentActivity.getString(R.string.connected_status));
                        parentActivity.mStatusText.setText(str);
                        //parentActivity.btn_read_Characteristic.setEnabled(true);
                        // TODO: Your code here. Request read/write of
                        // characteristics, or register for notifications.
                        // Access these functions via the smartService
                        // reference.
                        // Pass mValueHandler as the handler when calling these
                        // request functions.
                        parentActivity.mGattServiceList = smartService.get_GattServiceList();
                        ServiceInfo serviceinfo = null;
                        if (null != parentActivity.mGattServiceList){
                            for (int i = 0; i < parentActivity.mGattServiceList.size(); i++) {
                                BluetoothGattService theService = parentActivity.mGattServiceList.get(i);
                                Log.d(TAG, "ServiceName:" + theService.getUuid());
                                Log.d(TAG, "theService:" + theService.toString());
                                serviceinfo = new ServiceInfo(theService.toString(),theService.getUuid().toString()/*, theService*/);
                                if (!mServiceUUID.contains(theService.getUuid().toString())){
                                    mServiceUUID.add(theService.getUuid().toString());
                                    mServicelist.add(serviceinfo);
                                    mServicelistAdapter.notifyDataSetChanged();
                                }
                            }
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
     * This is the handler for characteristic value updates.
     */
    private final Handler mValueHandler = new ValueHandler(this);

    private static class ValueHandler extends Handler {
        private final WeakReference<BTSmartActivity> mActivity;
        private final String TAG = "BTSmart.ValueHandler";

        public ValueHandler(BTSmartActivity activity) {
            mActivity = new WeakReference<BTSmartActivity>(activity);
        }

        public void handleMessage(Message msg) {
            BTSmartActivity parentActivity = mActivity.get();
            Log.d(TAG, "handleMessage: msg.what=" + msg.what);
            if (parentActivity != null) {
                switch (msg.what) {
                    case BtSmartService.MESSAGE_REQUEST_FAILED: {
                        // The request id tells us what failed.
                        int requestId = msg.getData().getInt(BtSmartService.EXTRA_CLIENT_REQUEST_ID);
                        // TODO: Handle failures.
                        break;
                    }
                    case BtSmartService.MESSAGE_CHARACTERISTIC_VALUE: {
                        // This code is executed when a value is received in
                        // response to a direct get or a notification.
                        Bundle msgExtra = msg.getData();
                        UUID serviceUuid = ((ParcelUuid) msgExtra.getParcelable(BtSmartService.EXTRA_SERVICE_UUID))
                                .getUuid();
                        UUID characteristicUuid = ((ParcelUuid) msgExtra
                                .getParcelable(BtSmartService.EXTRA_CHARACTERISTIC_UUID)).getUuid();
                        byte[] value = msgExtra.getByteArray(BtSmartService.EXTRA_VALUE);

                        // TODO: Do something with the value. The serviceUuid
                        // and characteristicUuid tell you which characteristic
                        // the value belongs to.

                        break;
                    }
                }
            }
        }
    };

    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        mService.disconnect();
        unbindService(mServiceConnection);
        Toast.makeText(this, "Disconnected from device:"+mDeviceToConnect.getName(), Toast.LENGTH_LONG).show();
        mServiceUUID.clear();
        mServicelist.clear();
        mDeviceToConnect = null;
        unregisterReceiver(mGattUpdateReceiver);
        super.onDestroy();
    }

    public void onResume(){
        Log.e(TAG, "onResume");
        super.onResume();
    }

    /**
     * The adapter that allows the contents of ServiceInfo objects to be displayed in the ListView. The service name,
     * service UUID specified in servicelist.xml are displayed.
     */
    private class ServiceListAdapter extends BaseAdapter {
        private Activity activity;
        private ArrayList<ServiceInfo> data;
        private LayoutInflater inflater = null;

        public ServiceListAdapter(Activity a, ArrayList<ServiceInfo> object) {
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
                vi = inflater.inflate(R.layout.servicelist, null);

            TextView servicenameText = (TextView) vi.findViewById(R.id.servicename);
            TextView serviceUUIDText = (TextView) vi.findViewById(R.id.serviceUUID);

            ServiceInfo info = (ServiceInfo) data.get(position);
            servicenameText.setText(info.getName());
            serviceUUIDText.setText(info.getUUID());
            return vi;
        }
    }

    /**
     * The on-click listener for selecting a service.
     */
    private AdapterView.OnItemClickListener mServicelistClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ServiceInfo info = (ServiceInfo)mServicelistAdapter.getItem(position);

            if (null != mGattServiceList){
                for (int i = 0; i < mGattServiceList.size(); i++) {
                    BluetoothGattService theService = mGattServiceList.get(i);
                    Log.d(TAG, "ServiceName:" + theService.getUuid());
                    Log.d(TAG, "theService:" + theService.toString());
                    if (theService.getUuid().toString().equals(info.getUUID())){
                        Log.d(TAG, "i'm here!!!");
                        mGattService = theService;
                        mGatt = mService.get_Gatt();
                    }
                }
            }
            startGattServiceActivity(info);
        }
    };

    private void startGattServiceActivity(ServiceInfo info){
        Intent intent;

        if("mi".equals(mDeviceToConnect.getName().toLowerCase())){
            Log.d(TAG, "jump to HackXiaomi Activity");
            intent = new Intent(this, HackXiaoMiActivity.class);
        } else {
            Log.d(TAG, "jump to Default Activity");
            intent = new Intent(this, GattServiceActivity.class);
        }

        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, mDeviceToConnect);
        Bundle bundle = new Bundle();
        bundle.putParcelable(ServiceInfo.ServiceInfoKeyString, info);
        Log.d(TAG, "info=" + info.toString());
        /*
        List<BluetoothGattCharacteristic> mGattCharacterList = info.getService().getCharacteristics();
        for (int j = 0; j < mGattCharacterList.size(); j++) {
            Log.d(TAG,
                    "---CharacterName:"
                            + mGattCharacterList.get(j).getUuid());
        }
        */
        intent.putExtras(bundle);
        this.startActivity(intent);
    }
}
