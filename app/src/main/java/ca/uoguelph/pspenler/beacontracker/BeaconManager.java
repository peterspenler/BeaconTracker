package ca.uoguelph.pspenler.beacontracker;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.SparseArray;
import android.util.SparseIntArray;

import java.util.Random;

public final class BeaconManager{

    private static float locX = 1;
    private static float locY = 1;

    private static BluetoothAdapter mBluetoothAdapter;
    private static boolean mScanning = true;
    private static SparseArray<BluetoothDevice> mDevices;
    private static SparseIntArray mRssis;
    private static Random rand = new Random();

    public static float getX(){
        locX += (2*rand.nextFloat()) - 1;
        return locX;
    }

    public static float getY(){
        locY += (2*rand.nextFloat()) - 1;
        return locY;
    }

    public static SparseArray<BluetoothDevice> getmDevices() {
        return mDevices;
    }

    public static int getRSSI(int device){
        return mRssis.get(device);
    }

    public static int indexFromHash(int hash){
        return mDevices.indexOfKey(hash);
    }

    public static void initialize(Context c, Activity a){
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) c.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mDevices = new SparseArray<>();
        mRssis = new SparseIntArray();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            ((MainActivity) a).startBlueTooth();
        }
        startLeScan();
    }

    public static void resumeScan(){
        if(!mScanning){
            startLeScan();
        }
    }

    public static void stopScanning(){
        if(mScanning) {
            stopLeScan();
        }
    }

    private static void startLeScan() {
        mScanning = true;
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    private static void stopLeScan(){
        mScanning = false;
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    private static BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            mDevices.put(device.hashCode(), device);
            mRssis.put(device.hashCode(), rssi);
        }
    };


}
