package ca.uoguelph.pspenler.beacontracker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Objects;

class Rssi{
    private ArrayList<Integer> rssis;
    private int txPower;

    Rssi(int rssi, int txpwr){
        rssis = new ArrayList<>(10);
        rssis.add(rssi);
        this.txPower = txpwr;
    }

    public void add(int rssi){
        if(rssis.size() >= 10){
            rssis.remove(0);
        }
        rssis.add(rssi);
    }

    public int txPower(){
        return txPower;
    }

    public int value(){
        int i, total = 0;
        for(i = 0; i < rssis.size(); i++){
            total += rssis.get(i);
        }
        return (total / i);
    }
}

public final class BeaconManager {

    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothLeScanner mBluetoothScanner;

    private static boolean mScanning = true; //Whether or not scanning is happening
    private static boolean mRunning = false; //Whether or not scanning has been paused

    private static SparseArray<BluetoothDevice> mDevices; //List of bluetooth devices
    private static SparseArray<Rssi> mRssis; //List of RSSIs for bluetooth devices

    private static Handler scanHandler = new Handler(); //Handles scheduling of scanning

    //Returns list of bluetooth devices
    public static SparseArray<BluetoothDevice> getmDevices() {
        return mDevices;
    }

    //Returns RSSI value of specific bluetooth device from it's hash
    public static Rssi getRSSI(int device) {
        return mRssis.get(device);
    }

    //Returns list of all RSSI values
    public static SparseArray<Rssi> getmRssis() {
        return mRssis.clone();
    }

    //returns index of bluetooth device in list from it's hash
    public static int indexFromHash(int hash) {
        return mDevices.indexOfKey(hash);
    }

    //Starts the bluetooth scan and schedules the scan to stop after 1 second
    private static Runnable startScan = new Runnable() {
        @Override
        public void run() {
            startLeScan();
            scanHandler.postDelayed(stopScan, 1000);
        }
    };

    //Stops the bluetooth scan and schedules the restart of the scan after 200 ms
    private static Runnable stopScan = new Runnable() {
        @Override
        public void run() {
            if(mScanning) {
                stopLeScan();
                scanHandler.postDelayed(startScan, 200);
            }
        }
    };

    //Initializes bluetooth manager, adapter, and device and rssi lists, then starts scan
    public static void initialize(final Activity a) {
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) App.getApplication().getSystemService(Context.BLUETOOTH_SERVICE);
        assert bluetoothManager != null;
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mDevices = new SparseArray<>();
        mRssis = new SparseArray<>();

        assert mBluetoothAdapter != null;
        if (mBluetoothAdapter != null || !mBluetoothAdapter.isEnabled()) {
            ((MainActivity) a).startBlueTooth();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ((MainActivity) a).requestLocation();
        }

        createNewCallback();

        scanHandler.post(startScan);
        mRunning = true;
        //startLeScan();
    }

    //Public function for starting bluetooth scan
    public static void resumeScan() {
        if (!mScanning) {
            if(!mRunning){
                scanHandler.post(startScan);
            }
        }
    }

    //Public function for stopping bluetooth scan
    public static void stopScanning() {
        if (mScanning) {
            stopLeScan();
            mRunning = false;
        }
    }

    //Starts either current or legacy bluetooth scan depending on Android version
    private static void startLeScan() {
        mScanning = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mBluetoothScanner.startScan(newLeScanCallback);
        else
            mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    //Starts or stops current or legacy bluetooth scan depending on Android version
    private static void stopLeScan() {
        mScanning = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mBluetoothScanner.stopScan(newLeScanCallback);
        else
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    //Initializes current bluetooth scan callback
    private static ScanCallback newLeScanCallback;

    //Current Android scan callback
    private static void createNewCallback() {
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP){
            newLeScanCallback = new ScanCallback() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onScanResult(int callbackType, ScanResult r) {
                    if (PointManager.canAddPoints()) {
                        int txpwr;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            txpwr = r.getTxPower();
                        else
                            txpwr = Objects.requireNonNull(r.getScanRecord()).getTxPowerLevel();
                        mDevices.put(r.getDevice().hashCode(), r.getDevice());
                        if (mRssis.indexOfKey(r.getDevice().hashCode()) < 0) {
                            mRssis.put(r.getDevice().hashCode(), new Rssi(r.getRssi(), txpwr));
                        }
                        mRssis.get(r.getDevice().hashCode()).add(r.getRssi());
                    } else {
                        for (int i = 0; i < PointManager.numPoints(); i++) {
                            if (PointManager.getPoint(i).device == r.getDevice().hashCode()) {
                                Log.i("UPDATE", r.getDevice().getAddress());
                                mRssis.get(r.getDevice().hashCode()).add(r.getRssi());
                            }
                        }
                    }
                }
            };
        }
    }

    //Legacy Android scan callback
    private static BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            if (PointManager.canAddPoints()) {
                if ((scanRecord[2] == 6) && (scanRecord[3] == 26)) {
                    byte txpwr = scanRecord[29];
                    mDevices.put(device.hashCode(), device);
                    if (mRssis.indexOfKey(device.hashCode()) < 0) {
                        mRssis.put(device.hashCode(), new Rssi(rssi, (int) txpwr));
                    }
                    mRssis.get(device.hashCode()).add(rssi);
                }
            }else{
                for(int i = 0; i < PointManager.numPoints(); i++){
                    if(PointManager.getPoint(i).device == device.hashCode()){
                        Log.i("UPDATE", device.getAddress());
                        mRssis.get(device.hashCode()).add(rssi);
                    }
                }
            }
        }
    };
}
