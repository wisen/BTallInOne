package com.wisen.wisenapp.btsmart;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class BtSmartService extends Service {

    private List<BluetoothGattService> serviceList;
    private List<BluetoothGattCharacteristic> characterList;

    private final String TAG = "BtSmartService";
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    private void broadcastBtSmartStatus(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    // // Enumerated type for Bluetooth Smart UUIDs.
    public enum BtSmartUuid {
        HRP_SERVICE("0000180d-0000-1000-8000-00805f9b34fb"),
        HEART_RATE_MEASUREMENT("00002a37-0000-1000-8000-00805f9b34fb"),
        CCC("00002902-0000-1000-8000-00805f9b34fb"),
        IMMEDIATE_ALERT("00001802-0000-1000-8000-00805f9b34fb"),
        ALERT_LEVEL("00002a06-0000-1000-8000-00805f9b34fb"),
        ALERT_NOTIFICATION_SERVICE("00001811-0000-1000-8000-00805f9b34fb"),
        ALERT_NOTIFICATION_CONTROL_POINT("00002a44-0000-1000-8000-00805f9b34fb"),
        UNREAD_ALERT_STATUS("00002a45-0000-1000-8000-00805f9b34fb"),
        NEW_ALERT("00002a46-0000-1000-8000-00805f9b34fb"),
        NEW_ALERT_CATEGORY("00002a47-0000-1000-8000-00805f9b34fb"),
        UNREAD_ALERT_CATEGORY("00002a48-0000-1000-8000-00805f9b34fb"),
        DEVICE_INFORMATION_SERVICE("0000180A-0000-1000-8000-00805f9b34fb"),
        MANUFACTURER_NAME("00002A29-0000-1000-8000-00805f9b34fb"),
        BATTERY_SERVICE("0000180f-0000-1000-8000-00805f9b34fb"),
        BATTERY_LEVEL("00002a19-0000-1000-8000-00805f9b34fb"),
        CSC_SERVICE("00001816-0000-1000-8000-00805f9b34fb"),
        CSC_MEASUREMENT("0002a5b-0000-1000-8000-00805f9b34fb"),
        CSC_FEATURE("00002a5c-0000-1000-8000-00805f9b34fb"),
        SENSOR_LOCATION("00002a5d-0000-1000-8000-00805f9b34fb"),
        CSC_CONTROL_POINT("00002a55-0000-1000-8000-00805f9b34fb");

        private UUID value;

        private BtSmartUuid(String value) {
            this.value = UUID.fromString(value);
        }

        public UUID getUuid() {
            return value;
        }

        public ParcelUuid getParcelable() {
            return new ParcelUuid(this.value);
        }

        // Lookup table to allow reverse lookup.
        private static final HashMap<UUID, BtSmartUuid> lookup = new HashMap<UUID, BtSmartUuid>();

        // Populate the lookup table at load time
        static {
            for (BtSmartUuid s : EnumSet.allOf(BtSmartUuid.class))
                lookup.put(s.value, s);
        }

        /**
         * Reverse look up UUID -> BtSmartUuid
         *
         * @param uuid
         *            The UUID to get a enumerated value for.
         * @return Enumerated value of type BtSmartUuid.
         */
        public static BtSmartUuid get(UUID uuid) {
            return lookup.get(uuid);
        }
    }

    // // Messages to send to registered handlers.
    public static final int MESSAGE_SCAN_RESULT = 1;
    public static final int MESSAGE_CONNECTED = 2;
    public static final int MESSAGE_CHARACTERISTIC_VALUE = 3;
    public static final int MESSAGE_DISCONNECTED = 4;
    public static final int MESSAGE_REQUEST_FAILED = 5;
    public static final int MESSAGE_CHARACTERISTIC_READ = 6;
    public static final int MESSAGE_CHARACTERISTIC_WRITE = 7;
    public static final int MESSAGE_DESCRIPTOR_READ = 8;
    public static final int MESSAGE_DESCRIPTOR_WRITE = 9;
    public static final int MESSAGE_DESCRIPTOR_VALUE = 10;

    // // Keys to use for sending extra data with above messages.
    public static final String EXTRA_SCAN_RECORD = "SCANRECORD";
    public static final String EXTRA_VALUE = "CVALUE";
    public static final String EXTRA_RSSI = "RSSI";
    public static final String EXTRA_PROPERTIES = "CPROPERTIES";
    public static final String EXTRA_APPEARANCE_KEY = "APPEARKEY";
    public static final String EXTRA_APPEARANCE_NAME = "APPEARNAME";
    public static final String EXTRA_APPEARANCE_ICON = "APPEARICON";
    public static final String EXTRA_SERVICE_UUID = "SERVUUID";
    public static final String EXTRA_CHARACTERISTIC_UUID = "CHARUUID";
    public static final String EXTRA_DESCRIPTOR_UUID = "DESCUUID";
    public static final String EXTRA_REQUEST_ID = "REQUESTID";
    public static final String EXTRA_CLIENT_REQUEST_ID = "CLIENTREQUESTID";

    public static final int APPEARANCE_UNKNOWN = 0;

    private static final int RESPONSE_STATUS_OK = 0;

    private final IBinder mBinder = new LocalBinder();
    private Handler mClientDeviceHandler = null;
    private Handler mServerDeviceHandler = null;
    private Handler mGattServiceHandler = null;
    private BluetoothManager mBtManager = null;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothGatt mGattClient = null;
    private BluetoothGattServer mGattServer = null;
    private BluetoothDevice mConnectedDevice = null;

    private final HashMap<Integer, BluetoothGattCharacteristic> pendingCharacteristicWrites = new HashMap<Integer, BluetoothGattCharacteristic>();
    private final HashMap<Integer, BluetoothGattDescriptor> pendingDescriptorWrites = new HashMap<Integer, BluetoothGattDescriptor>();

    private static final HashMap<String, Integer> characteristicProperties = new HashMap<String, Integer>();
    private static final HashMap<String, Integer> characteristicPermissions = new HashMap<String, Integer>();
    private static final HashMap<String, Integer> descriptorPermissions = new HashMap<String, Integer>();

    private static final HashMap<String, byte[]> descriptorValues = new HashMap<String, byte[]>();
    private static final HashMap<String, byte[]> characteristicValues = new HashMap<String, byte[]>();

    private CharacteristicHandlersContainer mNotificationHandlers = new CharacteristicHandlersContainer();

    // Characteristic currently waiting to have a notification value written to
    // it.
    private BluetoothGattCharacteristic mPendingCharacteristic = null;

    Queue<BtSmartRequest> requestQueue = new LinkedList<BtSmartRequest>();

    BtSmartRequest currentRequest = null;

    // Static hash maps for converting string values to BLE library constants.
    static {
        characteristicProperties.put("BROADCAST", BluetoothGattCharacteristic.PROPERTY_BROADCAST);
        characteristicProperties.put("EXTENDED_PROPS", BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS);
        characteristicProperties.put("INDICATE", BluetoothGattCharacteristic.PROPERTY_INDICATE);
        characteristicProperties.put("NOTIFY", BluetoothGattCharacteristic.PROPERTY_NOTIFY);
        characteristicProperties.put("READ", BluetoothGattCharacteristic.PROPERTY_READ);
        characteristicProperties.put("SIGNED_WRITE", BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE);
        characteristicProperties.put("WRITE", BluetoothGattCharacteristic.PROPERTY_WRITE);
        characteristicProperties.put("WRITE_NO_RESPONSE", BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE);

        characteristicPermissions.put("READ", BluetoothGattCharacteristic.PERMISSION_READ);
        characteristicPermissions.put("READ_ENCRYPTED", BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED);
        characteristicPermissions.put("READ_ENCRYPTED_MITM", BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM);
        characteristicPermissions.put("WRITE", BluetoothGattCharacteristic.PERMISSION_WRITE);
        characteristicPermissions.put("WRITE_ENCRYPTED", BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED);
        characteristicPermissions.put("WRITE_ENCRYPTED_MITM", BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM);
        characteristicPermissions.put("WRITE_SIGNED", BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED);
        characteristicPermissions.put("WRITE_SIGNED_MITM", BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM);

        descriptorPermissions.put("READ", BluetoothGattDescriptor.PERMISSION_READ);
        descriptorPermissions.put("READ_ENCRYPTED", BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED);
        descriptorPermissions.put("READ_ENCRYPTED_MITM", BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED_MITM);
        descriptorPermissions.put("WRITE", BluetoothGattDescriptor.PERMISSION_WRITE);
        descriptorPermissions.put("WRITE_ENCRYPTED", BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED);
        descriptorPermissions.put("WRITE_ENCRYPTED_MITM", BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED_MITM);
        descriptorPermissions.put("WRITE_SIGNED", BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED);
        descriptorPermissions.put("WRITE_SIGNED_MITM", BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED_MITM);

        descriptorValues.put("ENABLE_NOTIFICATION", BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        descriptorValues.put("ENABLE_INDICATION", BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        descriptorValues.put("DISABLE", BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
    }

    /**
     * Class used for the client Binder. Because we know this service always runs in the same process as its clients, we
     * don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public BtSmartService getService() {
            // Return this instance of BtSmartService so clients can call public
            // methods.
            return BtSmartService.this;
        }
    }

    /**
     * Return the interface to this service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Initialise the service.
     */
    @Override
    public void onCreate() {
        if (mBtAdapter == null) {
            mBtManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBtAdapter = mBtManager.getAdapter();
        }
    }

    /**
     * When the service is destroyed, make sure to close the Bluetooth connection.
     */
    @Override
    public void onDestroy() {
        if (mGattClient != null)
            mGattClient.close();
        if (mGattServer != null)
            mGattServer.close();
        super.onDestroy();
    }

    /**
     * Parse a scan record and retrieve the appearance value.
     *
     * @param scanRecord
     *            The scan record to search.
     * @return Appearance value as defined in assigned numbers document.
     */
    public static int getAppearanceFromScanRecord(byte[] scanRecord) {
        final int STATE_LENGTH = 0;
        final int STATE_AD_TYPE = 1;
        final int STATE_APPEARANCE_DATA = 2;

        final int AD_TYPE_APPEARANCE = 0x19;

        int state = STATE_LENGTH;

        byte length = 0;

        for (int i = 0; i < scanRecord.length; i++) {
            switch (state) {
                case STATE_LENGTH:
                    length = scanRecord[i];
                    state++;
                    break;
                case STATE_AD_TYPE:
                    // Found what we're looking for. Set the next state to read
                    // the
                    // value.
                    if (scanRecord[i] == AD_TYPE_APPEARANCE) {
                        state = STATE_APPEARANCE_DATA;
                    } else {
                        // Skip the rest of this data as it's not an appearance.
                        i += (length - 1);
                        state = STATE_LENGTH;
                    }
                    break;
                case STATE_APPEARANCE_DATA:
                    // This is a 16-bit value in little-endian format.
                    int MSB = (int) scanRecord[i + 1] << 8;
                    int LSB = (int) scanRecord[i];
                    return (MSB + LSB);
            }
        }
        // Appearance data was not found in the scan data.
        return APPEARANCE_UNKNOWN;
    }

    /**
     * Connect to a remote Bluetooth Smart device.
     *
     * @param device
     *            The BluetothDevice to connect to.
     * @return Boolean success value.
     */
    public void connectAsClient(BluetoothDevice device, Handler deviceHandler) {
        mClientDeviceHandler = deviceHandler;
        mGattClient = device.connectGatt(this, false, mGattCallbacks);
        mConnectedDevice = device;
    }

    public void registerGattServiceHandler(Handler handler){
        mGattServiceHandler = handler;
    }

    public BluetoothGatt get_Gatt(){
        return mGattClient;
    }

    public List<BluetoothGattCharacteristic> get_CharacteristicList(){
        return characterList;
    }

    public List<BluetoothGattService> get_GattServiceList(){
        return serviceList;
    }
    /**
     * Connect to remote Bluetooth Smart device and start a server. The deviceHandler will receive MESSAGE_CONNECTED on
     * connection success.
     *
     * @param device
     *            The remote Bluetooth device to connect to.
     * @param deviceHandler
     *            The handler that will receive messages about the connection.
     * @param serviceXmlResourceId
     *            A resource ID of an XML file used to register services for the server. Can be zero to indicate no
     *            services.
     * @throws ServiceRegistrationFailedException
     *             If registration of the service from the provided XML fails for some reason.
     */
    public void connectAsServer(BluetoothDevice device, Handler deviceHandler, int serviceXmlResourceId)
            throws ServiceRegistrationFailedException {
        mServerDeviceHandler = deviceHandler;
        mGattServer = mBtManager.openGattServer(this, mGattServerCallbacks);
        if (serviceXmlResourceId != 0) {
            registerServiceFromXml(serviceXmlResourceId);
        }
        mGattServer.connect(device, false);
        mConnectedDevice = device;
    }

    /**
     * Disconnect from the currently connected Bluetooth Smart device.
     */
    public void disconnect() {
        Log.d(TAG,"disconnect");
        if (mGattClient != null) {
            mGattClient.disconnect();
            mGattClient.close();
        }
        if (mGattServer != null) {
            mGattServer.cancelConnection(mConnectedDevice);
            mGattServer.close();
        }
    }

    /**
     * Enable notifications for a particular characteristic and register a handler for those notifications. If a request
     * is currently in progress then queue it.
     *
     * @param requestId
     *            An id provided by the caller that will be included in messages to the handler.
     * @param serviceUuid
     *            The service that contains the characteristic of interest.
     * @param characteristicUuid
     *            The characteristic to register for.
     * @param notifyHandler
     *            The handler that will receive MESSAGE_CHARACTERISTIC_VALUE messages containing a byte array named
     *            EXTRA_VALUE.
     */
    public void requestCharacteristicNotification(int requestId, UUID serviceUuid, UUID characteristicUuid,
                                                  Handler notifyHandler) {
        if (currentRequest == null) {
            performNotificationRequest(requestId, serviceUuid, characteristicUuid, notifyHandler);
        } else {
            requestQueue.add(new BtSmartRequest(BtSmartRequest.RequestType.CHARACTERISTIC_NOTIFICATION, requestId, serviceUuid,
                    characteristicUuid, null, notifyHandler));
        }
    }

    /**
     * Request the current value of a characteristic. This will return the value once only in a
     * MESSAGE_CHARACTERISTIC_VALUE. If a request is currently in progress then queue it.
     *
     * @param requestId
     *            An id provided by the caller that will be included in messages to the handler.
     * @param service
     *            The UUID of the service that contains the characteristic of interest.
     * @param characteristic
     *            The UUID of the characteristic.
     * @param valueHandler
     *            The handler that will receive MESSAGE_CHARACTERISTIC_VALUE messages containing a byte array named
     *            EXTRA_VALUE.
     */
    public void requestCharacteristicValue(int requestId, UUID service, UUID characteristic, Handler valueHandler) {
        if (currentRequest == null) {
            performCharacValueRequest(requestId, service, characteristic, valueHandler);
        } else {
            requestQueue.add(new BtSmartRequest(BtSmartRequest.RequestType.READ_CHARACTERISTIC, requestId, service, characteristic,
                    null, valueHandler));
        }
    }

    /**
     * Request the current value of a descriptor. This will return the value once only in a MESSAGE_DESCRIPTOR_VALUE. If
     * a request is currently in progress then queue it. Use requestCharacteristicNotification() for constant updates
     * when a characteristic value changes.
     *
     * @param requestId
     *            An id provided by the caller that will be included in messages to the handler.
     * @param service
     *            The UUID of the service that contains the characteristic and descriptor of interest.
     * @param characteristic
     *            The UUID of the characteristic.
     * @param descriptor
     *            The UUID of the descriptor.
     * @param valueHandler
     *            The handler that will receive MESSAGE_DESCRIPTOR_VALUE messages containing a byte array named
     *            EXTRA_VALUE.
     */
    public void requestDescriptorValue(int requestId, UUID service, UUID characteristic, UUID descriptor,
                                       Handler valueHandler) {
        if (currentRequest == null) {
            performDescValueRequest(requestId, service, characteristic, descriptor, valueHandler);
        } else {
            requestQueue.add(new BtSmartRequest(BtSmartRequest.RequestType.READ_DESCRIPTOR, requestId, service, characteristic,
                    descriptor, valueHandler));
        }
    }

    /**
     * For a GATT server, get a characteristic from the local database by UUID.
     *
     * @param serviceUuid
     *            The UUID of the service that contains the characteristic.
     * @param characteristicUuid
     *            The UUID of the characteristic to find.
     * @return The characteristic if found, otherwise null.
     */
    public BluetoothGattCharacteristic getCharacteristic(UUID serviceUuid, UUID characteristicUuid) {
        if (mGattServer != null) {
            BluetoothGattService service = mGattServer.getService(serviceUuid);
            if (service == null)
                return null;
            return service.getCharacteristic(characteristicUuid);
        } else {
            throw new NullPointerException("GATT server not started.");
        }
    }

    /**
     * For a GATT server, used to send a notification or indication to the client that a characteristic has changed.
     *
     * @param characteristic
     *            The local characteristic that has been updated.
     * @param confirm
     *            True to request confirmation from the client (indication), false to send a notification.
     */
    public void notifyCharacteristicChanged(BluetoothGattCharacteristic characteristic, boolean confirm) {
        if (mGattServer != null) {
            mGattServer.notifyCharacteristicChanged(mConnectedDevice, characteristic, confirm);
        } else {
            throw new NullPointerException("GATT server not started.");
        }
    }

    /**
     * For a GATT server, write the specified characteristic or descriptor value to the local database and send a
     * response to the client. It calls the more specific version of handleWrite(). This should be called by the Handler
     * after it receives a MESSAGE_CHARACTERISTIC_WRITE or MESSAGE_DESCRIPTOR_WRITE
     *
     * @param requestId
     *            Request ID that was received from the client.
     * @param value
     *            The value to write.
     */
    public void handleWrite(int requestId, byte[] value) throws IllegalArgumentException {

        if (pendingCharacteristicWrites.containsKey(requestId)) {
            handleWrite(requestId, pendingCharacteristicWrites.get(requestId), value);
            pendingCharacteristicWrites.remove(requestId);
        } else if (pendingDescriptorWrites.containsKey(requestId)) {
            handleWrite(requestId, pendingDescriptorWrites.get(requestId), value);
            pendingDescriptorWrites.remove(requestId);
        } else {
            throw new IllegalArgumentException("Request ID is not valid. No pending write was found with that ID.");
        }
    }

    /**
     * For a GATT server, send a response including the value to the client for a read. This must be called by the
     * Handler after it receives a MESSAGE_CHARACTERISTIC_READ or MESSAGE_DESCRIPTOR_READ
     *
     * @param requestId
     *            Request ID that was received from the client.
     * @param value
     *            The value to send.
     */
    public void handleRead(int requestId, byte[] value) {
        if (mGattServer != null) {
            mGattServer.sendResponse(mConnectedDevice, requestId, RESPONSE_STATUS_OK, 0, value);
        } else {
            throw new NullPointerException("GATT server not started.");
        }
    }

    /**
     * For a GATT server, write the specified characteristic value to the local database and send a response to the
     * client.
     *
     * @param requestId
     *            Request ID that was received from the client.
     * @param characteristic
     *            The local characteristic that has been updated.
     * @param value
     *            The value to write.
     */
    private void handleWrite(int requestId, BluetoothGattCharacteristic characteristic, byte[] value) {
        if (mGattServer != null) {
            if (value != null) {
                characteristic.setValue(value);
            }
            if (mGattServer != null) {
                mGattServer.sendResponse(mConnectedDevice, requestId, RESPONSE_STATUS_OK, 0, value);
            }
        } else {
            throw new NullPointerException("GATT server not started.");
        }
    }

    /**
     * For a GATT server, write the specified descriptor value to the local database and send a response to the client.
     *
     * @param requestId
     *            Request ID that was received from the client.
     * @param descriptor
     *            The local descriptor that has been updated.
     * @param value
     *            The value to write.
     */
    private void handleWrite(int requestId, BluetoothGattDescriptor descriptor, byte[] value) {
        if (value != null) {
            descriptor.setValue(value);
        }
        if (mGattServer != null) {
            mGattServer.sendResponse(mConnectedDevice, requestId, RESPONSE_STATUS_OK, 0, value);
        } else {
            throw new NullPointerException("GATT server not started.");
        }
    }

    /**
     * Helper function to send a message to a handler with bundle.
     *
     * @param h
     *            The Handler to send the message to.
     * @param msgId
     *            The message identifier to send.
     * @param bundle
     *            The data which need to send to handler
     */
    private void sendMessage(Handler h, int msgId, Bundle bundle) {
        if (h != null) {
            Message msg = Message.obtain(h, msgId);
            msg.setData(bundle);
            msg.sendToTarget();
        }
    }

    /**
     * Helper function to send a message to a handler with no parameters.
     *
     * @param h
     *            The Handler to send the message to.
     * @param msgId
     *            The message identifier to send. Use one of the defined constants.
     */
    private void sendMessage(Handler h, int msgId) {
        if (h != null) {
            Message.obtain(h, msgId).sendToTarget();
        }
    }

    /**
     * Helper function to send a message to a handler with no parameters except the request ID.
     *
     * @param h
     *            The Handler to send the message to.
     * @param msgId
     *            The message identifier to send. Use one of the defined constants.
     * @param requestId
     *            The request ID provided by the client of this Service.
     */
    private void sendMessage(Handler h, int requestId, int msgId) {
        if (h != null) {
            Bundle messageBundle = new Bundle();
            Message msg = Message.obtain(h, msgId);
            messageBundle.putInt(EXTRA_CLIENT_REQUEST_ID, requestId);
            msg.setData(messageBundle);
            msg.sendToTarget();
        }
    }

    /**
     * This is where most of the interesting stuff happens in response to changes in BLE state for a client.
     */
    private BluetoothGattCallback mGattCallbacks = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED && mGattClient != null) {
                // Get all the available services. This allows us to query them
                // later. The result of this being
                // successful will be a call to onServicesDiscovered(). Don't
                // tell the handler that we are connected
                // until the services have been discovered.
                Log.d(TAG, "Connected to GATT server.");
                mGattClient.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //sendMessage(mClientDeviceHandler, MESSAGE_DISCONNECTED);
                broadcastBtSmartStatus(ACTION_GATT_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered received: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Services are discovered, so now we are ready for
                // characteristic handlers to register themselves,
                serviceList = gatt.getServices();

                for (BluetoothGattService theService:serviceList) {
                    Log.d(TAG, "ServiceName:" + theService.getUuid());

                    characterList = theService.getCharacteristics();
                    for (BluetoothGattCharacteristic gattCharacteristic:characterList) {
                        mGattClient.readCharacteristic(gattCharacteristic);
                        Log.d(TAG,
                                "---CharacterName:"
                                        + gattCharacteristic.getUuid());
                    }
                }

                //sendMessage(mClientDeviceHandler, MESSAGE_CONNECTED);
                broadcastBtSmartStatus(ACTION_GATT_CONNECTED);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // A notification for a characteristic has been received, so notify
            // the registered Handler.
            Handler notificationHandler = mNotificationHandlers.getHandler(characteristic.getService().getUuid(),
                    characteristic.getUuid());
            if (notificationHandler != null) {
                Bundle messageBundle = new Bundle();
                Message msg = Message.obtain(notificationHandler, MESSAGE_CHARACTERISTIC_VALUE);
                messageBundle.putByteArray(EXTRA_VALUE, characteristic.getValue());
                messageBundle.putParcelable(EXTRA_SERVICE_UUID, BtSmartUuid.get(characteristic.getService().getUuid())
                        .getParcelable());
                messageBundle.putParcelable(EXTRA_CHARACTERISTIC_UUID, BtSmartUuid.get(characteristic.getUuid())
                        .getParcelable());
                msg.setData(messageBundle);
                msg.sendToTarget();
            }
        }

        /**
         * After calling registerForNotification this callback should trigger, and then we can perform the actual
         * enable. It could also be called when a descriptor was requested directly, so that case is handled too.
         */
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
            if (currentRequest.type == BtSmartRequest.RequestType.CHARACTERISTIC_NOTIFICATION) {
                // Descriptor was not requested indirectly as part of
                // registration for notifications.
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    sendMessage(currentRequest.notifyHandler, currentRequest.requestId, MESSAGE_REQUEST_FAILED);
                    mNotificationHandlers
                            .removeHandler(characteristic.getService().getUuid(), characteristic.getUuid());
                }
                if (characteristic.getService().getUuid() == mPendingCharacteristic.getService().getUuid()
                        && characteristic.getUuid() == mPendingCharacteristic.getUuid()) {
                    mNotificationHandlers.addHandler(characteristic.getService().getUuid(), characteristic.getUuid(),
                            currentRequest.notifyHandler);
                    if (!enableNotification(true, characteristic)) {
                        sendMessage(currentRequest.notifyHandler, currentRequest.requestId, MESSAGE_REQUEST_FAILED);
                        mNotificationHandlers.removeHandler(characteristic.getService().getUuid(),
                                characteristic.getUuid());
                    }
                    // Don't call processNextRequest yet as this request isn't
                    // complete until onDescriptorWrite() triggers.
                }
            } else if (currentRequest.type == BtSmartRequest.RequestType.READ_DESCRIPTOR) {
                // Descriptor was requested directly.
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Bundle messageBundle = new Bundle();
                    Message msg = Message.obtain(currentRequest.notifyHandler, MESSAGE_DESCRIPTOR_VALUE);
                    messageBundle.putByteArray(EXTRA_VALUE, characteristic.getValue());
                    messageBundle.putParcelable(EXTRA_SERVICE_UUID,
                            BtSmartUuid.get(characteristic.getService().getUuid()).getParcelable());
                    messageBundle.putParcelable(EXTRA_CHARACTERISTIC_UUID, BtSmartUuid.get(characteristic.getUuid())
                            .getParcelable());
                    messageBundle.putParcelable(EXTRA_DESCRIPTOR_UUID, BtSmartUuid.get(descriptor.getUuid())
                            .getParcelable());
                    msg.setData(messageBundle);
                    msg.sendToTarget();
                } else {
                    sendMessage(currentRequest.notifyHandler, currentRequest.requestId, MESSAGE_REQUEST_FAILED);
                }
                // This request is now complete, so see if there is another.
                processNextRequest();
            }
        }

        /**
         * After writing the CCC for a notification this callback should trigger. It could also be called when a
         * descriptor write was requested directly, so that case is handled too.
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
            Log.d(TAG, "onDescriptorWrite:"+descriptor.getUuid());
            Log.d(TAG, "characteristic:"+characteristic.getUuid().toString());
            /*
            if (currentRequest.type == BtSmartRequest.RequestType.CHARACTERISTIC_NOTIFICATION) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    sendMessage(currentRequest.notifyHandler, currentRequest.requestId, MESSAGE_REQUEST_FAILED);
                    mNotificationHandlers
                            .removeHandler(characteristic.getService().getUuid(), characteristic.getUuid());
                }
            } else if (currentRequest.type == BtSmartRequest.RequestType.WRITE_DESCRIPTOR) {
                // TODO: If descriptor writing is implemented, add code here to
                // send message to handler.
            }
            processNextRequest();
            */
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            // This can only be in response to the current request as there
            // can't be more than one in progress.
            // So check this is what we were expecting.
            Log.d(TAG, "onCharacteristicRead");
            byte[] value=characteristic.getValue();
            if (value!=null) {
                String v = new String(value);
                Log.d(TAG, "Value=" + v);
            }

            Bundle bundle = new Bundle();
            bundle.putParcelable(EXTRA_CHARACTERISTIC_UUID, new ParcelUuid(characteristic.getUuid()));
            bundle.putByteArray(EXTRA_VALUE, characteristic.getValue());
            bundle.putInt(EXTRA_PROPERTIES, characteristic.getProperties());
            sendMessage(mGattServiceHandler, MESSAGE_CHARACTERISTIC_VALUE, bundle);

            /*
            if (currentRequest.type == BtSmartRequest.RequestType.READ_CHARACTERISTIC) {
                if (currentRequest.notifyHandler != null) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Bundle messageBundle = new Bundle();
                        Message msg = Message.obtain(currentRequest.notifyHandler, MESSAGE_CHARACTERISTIC_VALUE);
                        messageBundle.putByteArray(EXTRA_VALUE, characteristic.getValue());
                        messageBundle.putParcelable(EXTRA_SERVICE_UUID,
                                BtSmartUuid.get(characteristic.getService().getUuid()).getParcelable());
                        messageBundle.putParcelable(EXTRA_CHARACTERISTIC_UUID, BtSmartUuid
                                .get(characteristic.getUuid()).getParcelable());
                        msg.setData(messageBundle);
                        msg.sendToTarget();
                    } else {
                        sendMessage(currentRequest.notifyHandler, currentRequest.requestId, MESSAGE_REQUEST_FAILED);
                    }
                }
                processNextRequest();
            }*/
        }
    };

    /**
     * This is where most of the interesting stuff happens in response to changes in BLE state for a server.
     */
    private BluetoothGattServerCallback mGattServerCallbacks = new BluetoothGattServerCallback() {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED && mGattServer != null) {
                Bundle messageBundle = new Bundle();
                Message msg = Message.obtain(mServerDeviceHandler, MESSAGE_CONNECTED);
                messageBundle.putParcelable(BluetoothDevice.EXTRA_DEVICE, device);
                msg.setData(messageBundle);
                msg.sendToTarget();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                sendMessage(mServerDeviceHandler, MESSAGE_DISCONNECTED);
            }
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset,
                                            BluetoothGattDescriptor descriptor) {
            byte[] value = descriptor.getValue();
            if (mGattServer != null) {
                // Notify the Handler that there was a descriptor read request.
                // The Handler should call handleRead() after receiving this
                // message.
                Bundle messageBundle = new Bundle();
                Message msg = Message.obtain(mServerDeviceHandler, MESSAGE_DESCRIPTOR_READ);
                messageBundle.putParcelable(EXTRA_SERVICE_UUID,
                        BtSmartUuid.get(descriptor.getCharacteristic().getService().getUuid()).getParcelable());
                messageBundle.putParcelable(EXTRA_CHARACTERISTIC_UUID,
                        BtSmartUuid.get(descriptor.getCharacteristic().getUuid()).getParcelable());
                messageBundle.putParcelable(EXTRA_DESCRIPTOR_UUID, BtSmartUuid.get(descriptor.getUuid())
                        .getParcelable());
                messageBundle.putByteArray(EXTRA_VALUE, value);
                messageBundle.putInt(EXTRA_REQUEST_ID, requestId);
                msg.setData(messageBundle);
                msg.sendToTarget();
            }
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor,
                                             boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            if (mGattServer != null) {
                // Notify the Handler that there was a descriptor write request.
                // The Handler should call handleWrite() after receiving this
                // message.
                pendingDescriptorWrites.put(requestId, descriptor);
                Bundle messageBundle = new Bundle();
                Message msg = Message.obtain(mServerDeviceHandler, MESSAGE_DESCRIPTOR_WRITE);
                messageBundle.putParcelable(EXTRA_SERVICE_UUID,
                        BtSmartUuid.get(descriptor.getCharacteristic().getService().getUuid()).getParcelable());
                messageBundle.putParcelable(EXTRA_CHARACTERISTIC_UUID,
                        BtSmartUuid.get(descriptor.getCharacteristic().getUuid()).getParcelable());
                messageBundle.putParcelable(EXTRA_DESCRIPTOR_UUID, BtSmartUuid.get(descriptor.getUuid())
                        .getParcelable());
                messageBundle.putByteArray(EXTRA_VALUE, value);
                messageBundle.putInt(EXTRA_REQUEST_ID, requestId);
                msg.setData(messageBundle);
                msg.sendToTarget();
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            if (mGattServer != null) {
                // Notify the Handler that there was a characteristic read
                // request. The Handler should call handleRead() after receiving
                // this message.
                byte[] value = characteristic.getValue();
                Bundle messageBundle = new Bundle();
                Message msg = Message.obtain(mServerDeviceHandler, MESSAGE_CHARACTERISTIC_READ);
                messageBundle.putParcelable(EXTRA_SERVICE_UUID, BtSmartUuid.get(characteristic.getService().getUuid())
                        .getParcelable());
                messageBundle.putParcelable(EXTRA_CHARACTERISTIC_UUID, BtSmartUuid.get(characteristic.getUuid())
                        .getParcelable());
                messageBundle.putByteArray(EXTRA_VALUE, value);
                messageBundle.putInt(EXTRA_REQUEST_ID, requestId);
                msg.setData(messageBundle);
                msg.sendToTarget();
            }
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset,
                                                 byte[] value) {
            if (mGattServer != null) {
                // Notify the Handler that there was a characteristic write
                // request. The Handler should call handleWrite() after
                // receiving this message.
                pendingCharacteristicWrites.put(requestId, characteristic);
                Bundle messageBundle = new Bundle();
                Message msg = Message.obtain(mServerDeviceHandler, MESSAGE_CHARACTERISTIC_WRITE);
                messageBundle.putParcelable(EXTRA_SERVICE_UUID, BtSmartUuid.get(characteristic.getService().getUuid())
                        .getParcelable());
                messageBundle.putParcelable(EXTRA_CHARACTERISTIC_UUID, BtSmartUuid.get(characteristic.getUuid())
                        .getParcelable());
                messageBundle.putByteArray(EXTRA_VALUE, value);
                messageBundle.putInt(EXTRA_REQUEST_ID, requestId);
                msg.setData(messageBundle);
                msg.sendToTarget();
            }
        }
    };

    /**
     * Process the next request in the queue for some BLE action (such as characteristic read). This is required because
     * the Android 4.3 BLE stack only allows one active request at a time.
     */
    private void processNextRequest() {
        if (requestQueue.isEmpty()) {
            currentRequest = null;
            return;
        }
        BtSmartRequest request = requestQueue.remove();
        switch (request.type) {
            case CHARACTERISTIC_NOTIFICATION:
                performNotificationRequest(request.requestId, request.serviceUuid, request.characteristicUuid,
                        request.notifyHandler);
                break;
            case READ_CHARACTERISTIC:
                performCharacValueRequest(request.requestId, request.serviceUuid, request.characteristicUuid,
                        request.notifyHandler);
                break;
            case READ_DESCRIPTOR:
                performDescValueRequest(request.requestId, request.serviceUuid, request.characteristicUuid,
                        request.descriptorUuid, request.notifyHandler);
                break;
            default:
                break;
        }
    }

    /**
     * Perform the notification request now.
     *
     * @param requestId
     *            An id provided by the caller that will be included in messages to the handler.
     * @param service
     *            The service that contains the characteristic of interest.
     * @param characteristic
     *            The characteristic to register for.
     * @param notifyHandler
     *            The handler that will receive MESSAGE_CHARACTERISTIC_VALUE messages containing a byte array named
     *            EXTRA_VALUE.
     */
    private void performNotificationRequest(int requestId, UUID service, UUID characteristic, Handler notifyHandler) {
        // This currentRequest object will be used when we get the value back
        // asynchronously in the callback.
        currentRequest = new BtSmartRequest(BtSmartRequest.RequestType.CHARACTERISTIC_NOTIFICATION, requestId, service,
                characteristic, null, notifyHandler);
        BluetoothGattService serviceObject = mGattClient.getService(service);
        if (serviceObject != null) {
            mPendingCharacteristic = serviceObject.getCharacteristic(characteristic);
            if (mPendingCharacteristic != null) {
                BluetoothGattDescriptor clientCharacteristicConfig = mPendingCharacteristic
                        .getDescriptor(BtSmartUuid.CCC.value);
                // If the CCC exists then attempt to read it.
                if (clientCharacteristicConfig == null || !mGattClient.readDescriptor(clientCharacteristicConfig)) {
                    // CCC didn't exist or the read failed early. Send the
                    // failed message and move onto the next request.
                    sendMessage(notifyHandler, currentRequest.requestId, MESSAGE_REQUEST_FAILED);
                    processNextRequest();
                }
            }
        }
    }

    /**
     * Perform the characteristic value request now.
     *
     * @param requestId
     *            An id provided by the caller that will be included in messages to the handler.
     * @param service
     *            The service that contains the characteristic of interest.
     * @param characteristic
     *            The characteristic to get the value of.
     * @param valueHandler
     *            The handler that will receive MESSAGE_CHARACTERISTIC_VALUE messages containing a byte array named
     *            EXTRA_VALUE.
     */
    private void performCharacValueRequest(int requestId, UUID service, UUID characteristic, Handler valueHandler) {
        // This currentRequest object will be used when we get the value back
        // asynchronously in the callback.
        currentRequest = new BtSmartRequest(BtSmartRequest.RequestType.READ_CHARACTERISTIC, requestId, service, characteristic, null,
                valueHandler);
        BluetoothGattService serviceObject = mGattClient.getService(service);
        if (serviceObject != null) {
            BluetoothGattCharacteristic characteristicObject = serviceObject.getCharacteristic(characteristic);
            if (characteristicObject != null) {
                if (!mGattClient.readCharacteristic(characteristicObject)) {
                    sendMessage(valueHandler, currentRequest.requestId, MESSAGE_REQUEST_FAILED);
                    processNextRequest();
                }
            }
        }
    }

    /**
     * Perform the descriptor value request now.
     *
     * @param requestId
     *            An id provided by the caller that will be included in messages to the handler.
     * @param service
     *            The service that contains the characteristic of interest.
     * @param characteristic
     *            The characteristic that contains the descriptor of interest.
     * @param descriptor
     *            The descriptor to get the value of.
     * @param valueHandler
     *            The handler that will receive MESSAGE_CHARACTERISTIC_VALUE messages containing a byte array named
     *            EXTRA_VALUE.
     */
    private void performDescValueRequest(int requestId, UUID service, UUID characteristic, UUID descriptor,
                                         Handler valueHandler) {
        // This currentRequest object will be used when we get the value back
        // asynchronously in the callback.
        currentRequest = new BtSmartRequest(BtSmartRequest.RequestType.READ_CHARACTERISTIC, requestId, service, characteristic,
                descriptor, valueHandler);
        BluetoothGattService serviceObject = mGattClient.getService(service);
        if (serviceObject != null) {
            BluetoothGattCharacteristic characteristicObject = serviceObject.getCharacteristic(characteristic);
            if (characteristicObject != null) {
                BluetoothGattDescriptor descriptorObject = characteristicObject.getDescriptor(descriptor);
                if (descriptorObject != null) {
                    if (!mGattClient.readDescriptor(descriptorObject)) {
                        sendMessage(valueHandler, currentRequest.requestId, MESSAGE_REQUEST_FAILED);
                        processNextRequest();
                    }
                }
            }
        }
    }

    /**
     * Write to the CCC to enable or disable notifications.
     *
     * @param enable
     *            Boolean indicating whether the notification should be enabled or disabled.
     * @param characteristic
     *            The CCC to write to.
     * @return Boolean result of operation.
     */
    private boolean enableNotification(boolean enable, BluetoothGattCharacteristic characteristic) {
        if (mGattClient == null) {
            throw new NullPointerException("GATT client not started.");
        }
        if (!mGattClient.setCharacteristicNotification(characteristic, enable)) {
            return false;
        }
        BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(BtSmartUuid.CCC.value);
        if (clientConfig == null) {
            return false;
        }
        if (enable) {
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }

        return mGattClient.writeDescriptor(clientConfig);
    }

    /**
     * Register a service described in an XML file with the GATT Server instance.
     *
     * @param xmlResourceId
     *            Resource ID of the XML file.
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void registerServiceFromXml(int xmlResourceId) throws ServiceRegistrationFailedException {
        XmlResourceParser xpp;
        try {
            xpp = getResources().getXml(xmlResourceId);
        } catch (Resources.NotFoundException e) {
            throw new ServiceRegistrationFailedException("Resource ID not found");
        }

        int eventType = 0;
        try {
            xpp.next();
            eventType = xpp.getEventType();
        } catch (XmlPullParserException e) {
            throw new ServiceRegistrationFailedException("Error when parsing XML.");
        } catch (IOException e) {
            throw new ServiceRegistrationFailedException("IO error when reading from XML.");
        }

        BluetoothGattService currentService = null;
        BluetoothGattCharacteristic currentCharacteristic = null;
        BluetoothGattDescriptor currentDescriptor = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tagName = xpp.getName();
                if (tagName.equals("service")) {
                    if (currentService != null) {
                        mGattServer.addService(currentService);
                        currentService = null;
                    }
                    UUID serviceUuid = getUUIDfromString(xpp.getAttributeValue(null, "uuid"));

                    String serviceType = xpp.getAttributeValue(null, "type");
                    if (serviceType.equalsIgnoreCase("primary")) {
                        currentService = new BluetoothGattService(serviceUuid,
                                BluetoothGattService.SERVICE_TYPE_PRIMARY);
                    } else if (serviceType.equalsIgnoreCase("secondary")) {
                        currentService = new BluetoothGattService(serviceUuid,
                                BluetoothGattService.SERVICE_TYPE_SECONDARY);
                    } else {
                        throw new ServiceRegistrationFailedException(
                                "Service type should be one of 'primary' or 'secondary'.");
                    }
                } else if (tagName.equals("characteristic")) {
                    if (currentService == null) {
                        throw new ServiceRegistrationFailedException("Characteristic specified with no parent service.");
                    }
                    UUID characteristicUuid = getUUIDfromString(xpp.getAttributeValue(null, "uuid"));
                    int propertyValue = parseBooleanOrExpression(xpp.getAttributeValue(null, "property"),
                            characteristicProperties);
                    int permissionValue = parseBooleanOrExpression(xpp.getAttributeValue(null, "permission"),
                            characteristicPermissions);
                    currentCharacteristic = new BluetoothGattCharacteristic(characteristicUuid, propertyValue,
                            permissionValue);

                    // Check for a value.
                    String characteristicValue = xpp.getAttributeValue(null, "value");
                    byte[] value = null;
                    if (characteristicValue != null) {
                        if (characteristicValues.containsKey(characteristicValue)) {
                            value = characteristicValues.get(characteristicValue);
                        }
                        // Check if we got a comma separated list of literal
                        // byte values.
                        else if (characteristicValue.matches("[(0x[0-9a-f]+)[,]*]+")) {
                            value = getHexValues(characteristicValue);
                        } else {
                            throw new ServiceRegistrationFailedException("Invalid characteristic value.");
                        }

                        if (value != null) {
                            currentCharacteristic.setValue(value);
                        }
                    }

                    currentService.addCharacteristic(currentCharacteristic);
                } else if (tagName.equals("descriptor")) {
                    if (currentCharacteristic == null) {
                        throw new ServiceRegistrationFailedException(
                                "Descriptor specified with no parent characteristic.");
                    }
                    UUID descriptorUuid = getUUIDfromString(xpp.getAttributeValue(null, "uuid"));
                    int permissionValue = parseBooleanOrExpression(xpp.getAttributeValue(null, "permission"),
                            descriptorPermissions);
                    currentDescriptor = new BluetoothGattDescriptor(descriptorUuid, permissionValue);

                    // Check for a value.
                    String descriptorValue = xpp.getAttributeValue(null, "value");
                    byte[] value = null;
                    if (descriptorValue != null) {
                        if (descriptorValues.containsKey(descriptorValue)) {
                            value = descriptorValues.get(descriptorValue);
                        }
                        // Check if we got a comma separated list of literal
                        // byte values.
                        else if (descriptorValue.matches("[(0x[0-9a-f]+)[,]*]+")) {
                            value = getHexValues(descriptorValue);
                        } else {
                            throw new ServiceRegistrationFailedException("Invalid descriptor value.");
                        }

                        if (value != null) {
                            currentDescriptor.setValue(value);
                        }
                    }

                    currentCharacteristic.addDescriptor(currentDescriptor);
                }
            }
            try {
                eventType = xpp.next();
            } catch (XmlPullParserException e) {
                throw new ServiceRegistrationFailedException("Error when parsing XML.");
            } catch (IOException e) {
                throw new ServiceRegistrationFailedException("IO error when reading from XML.");
            }
        }
        if (currentService != null) {
            mGattServer.addService(currentService);
        }
    }

    /**
     * Helper function used by registerServiceFromXml(). Converts a string containing either a numerical UUID or a
     * textual identifier matching a member of the UUID enumerated type into a UUID.
     *
     * @param uuidString
     *            String containing the UUID or a textual identifier.
     * @return UUID
     */
    private UUID getUUIDfromString(String uuidString) throws IllegalArgumentException {
        UUID result = null;
        try {
            result = BtSmartUuid.valueOf(uuidString).value;
        } catch (IllegalArgumentException e) {
            // The UUID wasn't found in the enum, maybe it is a numerical UUID?
            try {
                result = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e2) {
                // Add some extra information.
                throw new IllegalArgumentException("Invalid UUID in XML.");
            }
        }
        return result;
    }

    /**
     * Helper function used by registerServiceFromXml(). Parse a string of the form "X|Y|Z" and return an integer formed
     * by a bitwise OR of the values of X, Y and Z. The values of X, Y and Z are looked up in the provided HashMap.
     *
     * @param expression
     *            String of the form "X|Y|Z" (an arbitrary number of identifiers is allowed).
     * @param idMap
     *            HashMap mapping String identifiers to integer values.
     * @return Bitwise OR of values specified or zero if the expression is null.
     */
    private int parseBooleanOrExpression(String expression, HashMap<String, Integer> idMap) {
        int result = 0;
        if (expression != null) {
            String[] identifiers = expression.split("\\|");
            for (String identifier : identifiers) {
                if (idMap.containsKey(identifier)) {
                    result |= idMap.get(identifier);
                }
            }
        }
        return result;
    }

    /**
     * Helper function used by registerServiceFromXml(). Return an array of byte values from a string of comma separated
     * hex values.
     *
     * @param values
     *            Hex values in the format 0xXX, 0xXX, etc.
     * @return byte array of values found in the string.
     */
    private byte[] getHexValues(String values) {
        byte[] result = null;

        String[] hexValues = values.split(",");
        result = new byte[hexValues.length];

        for (int i = 0; i < hexValues.length; i++) {
            // Parse the hex but first take off the '0x' with substring.
            result[i] = Byte.parseByte(hexValues[i].substring(2), 16);
        }

        return result;
    }

}

