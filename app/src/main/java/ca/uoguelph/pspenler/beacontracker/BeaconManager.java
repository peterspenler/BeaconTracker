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
import java.util.Random;

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

    public void setTxPower(int txpwr){
        this.txPower = txpwr;
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

    private static float locX = 1;
    private static float locY = 1;

    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothLeScanner mBluetoothScanner;
    private static boolean mScanning = true;
    private static SparseArray<BluetoothDevice> mDevices;
    private static SparseArray<Rssi> mRssis;
    private static Random rand = new Random();

    private static Handler scanHandler = new Handler();

    public static float getX() {
        locX += (2 * rand.nextFloat()) - 1;
        return locX;
    }

    public static float getY() {
        locY += (2 * rand.nextFloat()) - 1;
        return locY;
    }

    public static SparseArray<BluetoothDevice> getmDevices() {
        return mDevices;
    }

    public static Rssi getRSSI(int device) {
        return mRssis.get(device);
    }

    public static int indexFromHash(int hash) {
        return mDevices.indexOfKey(hash);
    }

    private static Runnable startScan = new Runnable() {
        @Override
        public void run() {
            startLeScan();
            scanHandler.postDelayed(stopScan, 500);
        }
    };

    private static Runnable stopScan = new Runnable() {
        @Override
        public void run() {
            if(mScanning) {
                stopLeScan();
                scanHandler.postDelayed(startScan, 100);
            }
        }
    };

    public static void initialize(final Activity a) {
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) App.getApplication().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mDevices = new SparseArray<>();
        mRssis = new SparseArray<>();

        if (mBluetoothAdapter != null || !mBluetoothAdapter.isEnabled()) {
            ((MainActivity) a).startBlueTooth();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ((MainActivity) a).requestLocation();
        }

        createNewCallback();

        scanHandler.post(startScan);
        //startLeScan();
    }

    public static void resumeScan() {
        if (!mScanning) {
            startLeScan();
        }
    }

    public static void stopScanning() {
        if (mScanning) {
            stopLeScan();
        }
    }

    private static void startLeScan() {
        mScanning = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mBluetoothScanner.startScan(newLeScanCallback);
        else
            mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    private static void stopLeScan() {
        mScanning = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mBluetoothScanner.stopScan(newLeScanCallback);
        else
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    private static ScanCallback newLeScanCallback;

    //Current scan callback
    private static void createNewCallback() {
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP){
            newLeScanCallback = new ScanCallback() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onScanResult(int callbackType, ScanResult r) {
                    //super.onScanResult(callbackType, result);
                    if (PointManager.canAddPoints()) {
                        int txpwr = 0;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            txpwr = r.getTxPower();
                        else
                            txpwr = Objects.requireNonNull(r.getScanRecord()).getTxPowerLevel();
                        mDevices.put(r.getDevice().hashCode(), r.getDevice());
                        if (mRssis.indexOfKey(r.getDevice().hashCode()) < 0) {
                            mRssis.put(r.getDevice().hashCode(), new Rssi(r.getRssi(), (int) txpwr));
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

    //Legacy scan callback
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
