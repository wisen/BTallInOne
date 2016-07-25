package com.wisen.wisenapp.btsmart.xiaomi;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.wisen.wisenapp.R;
import com.wisen.wisenapp.btsmart.CharacteristicInfo;
import com.wisen.wisenapp.btsmart.ScanResultsActivity;

import java.util.ArrayList;

public class HackXMMainActivity extends AppCompatActivity {
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBondedDevicesList.clear();
    }

    final String TAG = "HackXMMainActivity";

    ListView bonded_devices_ListView = null;
    private static BondedDevicesAdapter mBondedDeviceslistAdapter;
    private ArrayList<BondedDevice> mBondedDevicesList = new ArrayList<BondedDevice>();

    private BluetoothAdapter mBtAdapter = null;
    Button btn_scanxiaomi = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hack_xmmain);

        final BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = btManager.getAdapter();

        bonded_devices_ListView = (ListView)findViewById(R.id.bonded_devices_ListView);
        mBondedDeviceslistAdapter = new BondedDevicesAdapter(this, mBondedDevicesList);
        bonded_devices_ListView.setAdapter(mBondedDeviceslistAdapter);
        bonded_devices_ListView.setOnItemClickListener(mBondedDevicesClickListener);

        if(XiaoMiUtil.isBondedFileExist()){
            XiaoMiUtil.do_parser(mBondedDevicesList);
        }

        btn_scanxiaomi = (Button)findViewById(R.id.btn_scan_xiaomi);
        btn_scanxiaomi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HackXMMainActivity.this, ScanResultsActivity.class);
                startActivity(intent);
            }
        });
    }

    private class BondedDevicesAdapter extends BaseAdapter {
        private Activity activity;
        private ArrayList<BondedDevice> data;
        private LayoutInflater inflater = null;

        public BondedDevicesAdapter(Activity a, ArrayList<BondedDevice> object) {
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
                vi = inflater.inflate(R.layout.bondeddevices, null);

            TextView bondeddevicename = (TextView) vi.findViewById(R.id.bonded_device_name);
            TextView bondeddeviceaddress = (TextView) vi.findViewById(R.id.bonded_device_address);
            //TextView characteristicvaluesText = (TextView) vi.findViewById(R.id.characteristic_values);
            //ImageView read_icon = (ImageView) vi.findViewById(R.id.read_action);
            // ImageView write_icon = (ImageView) vi.findViewById(R.id.write_action);

            BondedDevice device = (BondedDevice) data.get(position);
            bondeddevicename.setText(device.getName());
            bondeddeviceaddress.setText(device.getAddress());
            return vi;
        }
    }

    private AdapterView.OnItemClickListener mBondedDevicesClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final BondedDevice device = (BondedDevice)mBondedDeviceslistAdapter.getItem(position);
            Log.d(TAG, "device name: " + device.getName());
            Log.d(TAG, "device address: " + device.getAddress());

            startHackXiaoMiActivity(device);
        }
    };

    private void startHackXiaoMiActivity(BondedDevice device){
        BluetoothDevice deviceToConnect = mBtAdapter.getRemoteDevice(device.getAddress());
        Intent intent = new Intent(this, HackXiaoMiActivity.class);
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, deviceToConnect);
        intent.putExtra(XiaoMiUtil.BONDED_FLAG, XiaoMiUtil.XIAOMI_BONDED);
        this.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBondedDeviceslistAdapter.notifyDataSetChanged();
    }
}
