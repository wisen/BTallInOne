package com.wisen.wisenapp.pbap;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothUuid;
import android.bluetooth.client.pbap.BluetoothPbapClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vcard.VCardEntry;
import com.wisen.wisenapp.R;
import com.wisen.wisenapp.bt.DeviceListActivity;
import com.wisen.wisenapp.bt.Saudioclient;
import com.wisen.wisenapp.btsmart.xiaomi.BondedDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PbapMainActivity extends AppCompatActivity {

    private static final String TAG = "PbapMainActivity";
    private static final boolean D = true;
    private BluetoothAdapter mBluetoothAdapter = null;

    public static BluetoothPbapClient sPbapClient;
    public BluetoothServiceHandler sHandler = new BluetoothServiceHandler();
    public static BluetoothSocket sBluetoothSocket;

    final private int REQUEST_READ_CONTACTS = 90;
    final private int REQUEST_WRITE_CONTACTS = 91;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private static TextView mTitle;
    private static boolean connect_to_pse_flag = false;
    private static boolean update_contacts_flag = false;
    private static boolean read_contacts_permission = false;
    private static boolean write_contacts_permission = false;

    private ArrayList<VCardEntry> mVCardEntryList = new ArrayList<VCardEntry>();

    public class BluetoothServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "BluetoothServiceHandler msg = " + msg.what);
            Log.d(TAG, msg.toString());
            switch (msg.what) {
                case BluetoothPbapClient.EVENT_PULL_PHONE_BOOK_DONE: {
                    Log.d(TAG, "EVENT_PULL_PHONE_BOOK_DONE");
                    mVCardEntryList = (ArrayList<VCardEntry>)msg.obj;
                    sPbapClient.disconnect();
                    mTitle.setText("Pull PB Done!");
                    if(null != mVCardEntryList){
                        mContactsList = mVCardEntryList;
                        mContactsListAdapter.notifyDataSetChanged();
                        update_contacts_flag = true;
                        invalidateOptionsMenu();
                        for(VCardEntry vCardEntry:mVCardEntryList){
                            Log.d(TAG, "Name: " + vCardEntry.getNameData().displayName);
                            List<VCardEntry.PhoneData> phoneDataList = vCardEntry.getPhoneList();
                            List<VCardEntry.EmailData> emailDataList = vCardEntry.getEmailList();
                            if (null != phoneDataList) {
                                for(VCardEntry.PhoneData phoneData : phoneDataList){
                                    Log.d(TAG, "Tele: " + phoneData.getNumber());
                                }
                            }

                            if (null != emailDataList) {
                                for(VCardEntry.EmailData emailData : emailDataList){
                                    Log.d(TAG, "Email: " + emailData.getAddress());
                                }
                            }
                        }
                    }
                }
                break;
                case BluetoothPbapClient.EVENT_SESSION_CONNECTED: {
                    Log.d(TAG, "EVENT_SESSION_CONNECTED");
                    connect_to_pse_flag = true;
                    mTitle.setText("Connected to PSE!");
                    invalidateOptionsMenu();//fresh the menu items.
                }
                break;
                case BluetoothPbapClient.EVENT_SESSION_DISCONNECTED: {
                    Log.d(TAG, "EVENT_SESSION_DISCONNECTED");
                    connect_to_pse_flag = false;
                    mTitle.setText("DisConnected to PSE!");
                    invalidateOptionsMenu();
                }
                break;
                default: {
                }
            }
        }
    }

    ListView contacts_listview = null;
    private static ContactsListAdapter mContactsListAdapter;
    private ArrayList<VCardEntry> mContactsList = new ArrayList<VCardEntry>();

    private class ContactsListAdapter extends BaseAdapter {
        private Activity activity;
        private ArrayList<VCardEntry> data;
        private LayoutInflater inflater = null;

        public ContactsListAdapter(Activity a, ArrayList<VCardEntry> object) {
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
                vi = inflater.inflate(R.layout.contact_list_item, null);

            TextView contactname = (TextView) vi.findViewById(R.id.contact_name);
            TextView contactphonenum = (TextView) vi.findViewById(R.id.contact_phonenum);

            VCardEntry cardEntry = (VCardEntry) data.get(position);
            contactname.setText(cardEntry.getDisplayName());
            List<VCardEntry.PhoneData> phoneDataList = cardEntry.getPhoneList();
            List<VCardEntry.EmailData> emailDataList = cardEntry.getEmailList();
            if (null != phoneDataList) {
                for(VCardEntry.PhoneData phoneData : phoneDataList){
                    Log.d(TAG, "Tele: " + phoneData.getNumber());
                    contactphonenum.setText(phoneData.getNumber());
                }
            }

            if (null != emailDataList) {
                for(VCardEntry.EmailData emailData : emailDataList){
                    Log.d(TAG, "Email: " + emailData.getAddress());
                    contactphonenum.setText(emailData.getAddress());
                }
            }
            return vi;
        }
    }

    private AdapterView.OnItemClickListener mContactsListClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final VCardEntry cardEntry = (VCardEntry)mContactsListAdapter.getItem(position);
            Log.d(TAG, "name: " + cardEntry.getDisplayName());
            update_contacts_item(cardEntry);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.CustomTheme);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pbap_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);

        contacts_listview = (ListView)findViewById(R.id.recieved_contacts_list);
        mContactsListAdapter = new ContactsListAdapter(this, mContactsList);
        contacts_listview.setAdapter(mContactsListAdapter);
        contacts_listview.setOnItemClickListener(mContactsListClickListener);

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if(hasREAD_CONTACTSPermission()){
            read_contacts_permission = true;
        }

        if(hasWRITE_CONTACTSPermission()){
            write_contacts_permission = true;
        }
    }

    protected void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "+++ onStart +++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {

        }
    }

    public void establishPbap(BluetoothDevice device) {
        Log.d(TAG, "Begin to establishPbap");
        if (null != mBluetoothAdapter){
            if (sPbapClient == null) {
                sPbapClient = new BluetoothPbapClient(device, sHandler);
                sPbapClient.connect();
                Log.d(TAG, "after sPbapClient.connect()");
            } else {
                sPbapClient.connect();
            }
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        if (startPairing(device)){
                            mTitle.setText(device.getName() + " Bonded.");
                            //establishSocket(device);
                            establishPbap(device);
                        } else {
                            Toast.makeText(getApplicationContext(),"Pair failed",Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mTitle.setText(device.getName() + " Bonded.");
                        //establishSocket(device);
                        // Attempt to connect to the device
                        establishPbap(device);
                    }

                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    //do non things
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    public boolean startPairing(BluetoothDevice device) {
        // Pairing is unreliable while scanning, so cancel discovery
        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        if (!device.createBond()) {
            return false;
        }

        //mConnectAfterPairing = true;  // auto-connect after pairing
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pbap_menu, menu);

        MenuItem item = menu.findItem(R.id.pull_phonebook);
        if(connect_to_pse_flag && read_contacts_permission && write_contacts_permission){
            item.setVisible(true);
        } else {
            item.setVisible(false);
        }

        item = menu.findItem(R.id.update_contacts);
        if(update_contacts_flag && read_contacts_permission && write_contacts_permission){
            item.setVisible(true);
        } else {
            item.setVisible(false);
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scann_device:
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            case R.id.discovery:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            case R.id.pull_phonebook:
                getPhoneBook();
                return true;
            case R.id.update_contacts:
                update_all_contacts();
                return true;
        }
        return false;
    }

    private void update_contacts_item(VCardEntry mv){
        List<VCardEntry.PhoneData> phoneDataList = mv.getPhoneList();
        List<VCardEntry.EmailData> emailDataList = mv.getEmailList();

        ContentValues values = new ContentValues();
        //首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
        Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);

        //往data表入姓名数据
        values.clear();
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, mv.getDisplayName());
        getContentResolver().insert(
                android.provider.ContactsContract.Data.CONTENT_URI, values);

        //往data表入电话数据
        values.clear();
        values.put(android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        if (null != phoneDataList) {
            for(VCardEntry.PhoneData phoneData : phoneDataList){
                Log.d(TAG, "Tele: " + phoneData.getNumber());
                values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneData.getNumber());
            }
        }
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        getContentResolver().insert(
                android.provider.ContactsContract.Data.CONTENT_URI, values);

        //往data表入Email数据
        values.clear();
        values.put(android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
        if (null != emailDataList) {
            for(VCardEntry.EmailData emailData : emailDataList){
                Log.d(TAG, "Email: " + emailData.getAddress());
                values.put(ContactsContract.CommonDataKinds.Email.DATA, emailData.getAddress());
            }
        }
        values.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
        getContentResolver().insert(
                android.provider.ContactsContract.Data.CONTENT_URI, values);
    }

    private void update_all_contacts(){
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ArrayList<VCardEntry> list = mVCardEntryList;
        Iterator<VCardEntry> it = null;
        if (list != null) {
            it = list.iterator();
        }
        Log.d(TAG, "--->doInBackground it:" + it);
        int rawContactInsertIndex = 0;
        while(it!= null && it.hasNext()) {
            VCardEntry mv = it.next();
            rawContactInsertIndex = ops.size(); // 有了它才能给真正的实现批量添加
            Log.d(TAG, "--->>>>>>>name:" + mv.getDisplayName());
            Log.d(TAG, "--->>>>>>>getPhoneList:" + mv.getPhoneList());
            if (mv.getPhoneList() != null) {
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, ContactsContract.RawContacts.ACCOUNT_NAME)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, ContactsContract.RawContacts.ACCOUNT_TYPE)
                        .withYieldAllowed(true).build());
                // add name
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, mv.getDisplayName())
                        .withYieldAllowed(true).build());
                // add number
                for(VCardEntry.PhoneData phone : mv.getPhoneList()) {
                    Log.d(TAG, "--->>>>>>>number:" + phone.getNumber());
                    ops.add(ContentProviderOperation
                            .newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone.getNumber())
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.LABEL, "")
                            .withYieldAllowed(true).build());
                }
            }
        }
        ContentProviderResult[] results = null;
        if (ops != null) {
            try {
                results = getContentResolver()
                        .applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (RemoteException e) {
                Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
            } catch (OperationApplicationException e) {
                Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
            }

        }
    }

    public void getPhoneBook() {
        if (sPbapClient != null && sPbapClient.getState() == BluetoothPbapClient.ConnectionState.CONNECTED) {
            Log.d(TAG,"pulling the PhoneBook, it may take a long time ! ");
            sPbapClient.pullPhoneBook(BluetoothPbapClient.PB_PATH);
        } else {
            Log.d(TAG,"-----------------------------");
            Log.d(TAG,"sPbapClient is not ready ! ");
            Log.d(TAG,"sPbapClient:"+ sPbapClient.getState());
            Log.d(TAG,"-----------------------------");

        }
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public void showAllAvailableUuids(BluetoothDevice device) {
        Log.d(TAG, "-------------");

        ParcelUuid[] uuids = device.getUuids();
        if (uuids == null || uuids.length == 0) {
            Log.d(TAG, "get uuids failed");
        } else {
            for (ParcelUuid uuid : uuids) {
                Log.d(TAG, "uuid is : " + uuid);
            }
            Log.d(TAG, "-------------");
        }
    }

    public void showBondedState(BluetoothDevice device){
        // bond state
        switch (device.getBondState()) {
            case BluetoothDevice.BOND_BONDED: {
                Log.d(TAG, "BONDED");
            }
            break;
            case BluetoothDevice.BOND_NONE: {
                Log.d(TAG, "NONE");
            }
            break;
            case BluetoothDevice.BOND_BONDING: {
                Log.d(TAG, "BONDING");
            }
            break;
            default: {
            }
        }
    }

    public void showDiscoveryState(){
        if (mBluetoothAdapter.isDiscovering()) {
            Log.d(TAG, "@@@ it is discovering @@@ ");
            mBluetoothAdapter.cancelDiscovery();
        } else {
            Log.d(TAG, "@@@ it is not discovering @@@");
        }
    }

    public void establishSocket(BluetoothDevice device) {

        ParcelUuid uuid = BluetoothUuid.PBAP_PSE;

        showAllAvailableUuids(device);
        showBondedState(device);
        showDiscoveryState();

        try {
            Log.d(TAG, "try to createRfcommSocketToServiceRecord");
            mBluetoothAdapter.cancelDiscovery();

            sBluetoothSocket = device.createRfcommSocketToServiceRecord(uuid.getUuid());

            int connectTimes = 0;
            int maxConnectTimes = 10;
            boolean isConnected = false;
            while (!isConnected && connectTimes < maxConnectTimes) {
                try {
                    sBluetoothSocket.connect();

                } catch (IOException e) {
                    Log.d(TAG, "sBLuetoothSocket.connect() is failed -------from the IOException");
                    try {
                        sBluetoothSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
                Log.d(TAG, "state of sBluetoothSocket : " + sBluetoothSocket.isConnected());

                if (sBluetoothSocket.isConnected()) {
                    isConnected = true;
                    mTitle.setText("Socket connected.");
                }
                connectTimes = connectTimes + 1;
            }


        } catch (IOException e) {
            Log.d(TAG, "createRfcommSocketToServiceRecord failed");
            e.printStackTrace();
        }

    }

    protected boolean hasREAD_CONTACTSPermission() {
        if (D) Log.d(TAG, "hasRecordAudioPermission()");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            if (D) Log.d(TAG, "requestPermissions Manifest.permission.ACCESS_COARSE_LOCATION");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
            return false;
        }
        return true;
    }

    protected boolean hasWRITE_CONTACTSPermission() {
        if (D) Log.d(TAG, "hasRecordAudioPermission()");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            if (D) Log.d(TAG, "requestPermissions Manifest.permission.ACCESS_COARSE_LOCATION");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_CONTACTS},
                    REQUEST_WRITE_CONTACTS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (D) Log.d(TAG, "onRequestPermissionsResult, requestCode = " + requestCode +
                ", permissions = " + permissions + ", grantResults = " + grantResults[0]);
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // proceedDiscovery(); // --->
                    //hasACCESS_COARSE_LOCATIONPermission = true;
                } else {
                    //TODO re-request
                }
                break;
            }
            case REQUEST_WRITE_CONTACTS: {
                break;
            }
        }

    }
}
