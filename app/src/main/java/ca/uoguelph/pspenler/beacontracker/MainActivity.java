package ca.uoguelph.pspenler.beacontracker;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = findViewById(R.id.addPointFab);
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
                        .setMessage("Finish adding points?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PointManager.donePoints();
                                fab.setVisibility(View.INVISIBLE);
                                Toast.makeText(App.getContext(), "Finished adding points", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", null)
                        .create();
                dialog.show();
                return true;
            }
        });

        BeaconManager.initialize(this);
        if(App.isFirstOpen()) {
            showInstructions();
        }
    }

    //Shows instructions for the app in a dialog
    private void showInstructions(){
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog_Alert)
                .setTitle("BeaconTracker")
                .setMessage("Welcome to BeaconTracker!\n\n" +
                        " - Use the add button to add a new beacon\n" +
                        " - Tap on an existing beacon to edit it\n" +
                        " - Long press the add button to finish adding beacons")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        App.firstOpen();
                    }
                })
                .create();
        dialog.show();
    }

    //Starts the add point dialog when the add button is pressed
    public void addPointDialog(View view){
        pointDialog(false, -1);
    }

    //Creates and shows the dialog for adding or changing points
    public void pointDialog(final boolean changing, final int index){
        String title;
        if(changing)
            title = "Change Point";
        else
            title = "Add Point";

        BeaconManager.stopScanning();

        final AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog_Alert)
                .setView(R.layout.dialog_add_point)
                .setTitle(title)
                .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button cancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                final EditText xText = dialog.findViewById(R.id.dialogXValue);
                final EditText yText = dialog.findViewById(R.id.dialogYValue);
                final Spinner btSpinner = dialog.findViewById(R.id.bluetoothDeviceSpinner);
                final TextView rssiView = dialog.findViewById(R.id.rssiText);
                final TextView txpwrView = dialog.findViewById(R.id.txpowerText);

                assert xText != null;
                assert yText != null;
                assert rssiView != null;
                assert txpwrView != null;
                assert btSpinner != null;

                BeaconManager.stopScanning();

                List<String> categories = new ArrayList<>();
                if(BeaconManager.getmDevices() != null){
                    for(int i = 0; i < BeaconManager.getmDevices().size(); i++){
                        categories.add(BeaconManager.getmDevices().valueAt(i).getAddress());
                    }
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(App.getContext(), R.layout.spinner_item, categories);
                dataAdapter.setDropDownViewResource(R.layout.spinner_item);
                btSpinner.setAdapter(dataAdapter);
                btSpinner.setSelection(0);
                btSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Rssi rssi = BeaconManager.getRSSI(BeaconManager.getmDevices().valueAt(btSpinner.getSelectedItemPosition()).hashCode());
                        rssiView.setText(getString(R.string.rssi_value, rssi.value()));
                        txpwrView.setText(getString(R.string.tx_value, rssi.txPower()));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                if(changing){
                    Point point = PointManager.getPoint(index);
                    xText.setText(fmt(point.realX));
                    yText.setText(fmt(point.realY));
                    int spinnerIndex = BeaconManager.indexFromHash(PointManager.getPoint(index).device);
                    if(spinnerIndex >= 0) {
                        btSpinner.setSelection(spinnerIndex);
                    }
                }

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BeaconManager.resumeScan();
                        dialog.dismiss();
                    }
                });

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            boolean result;
                            if(changing){
                                BluetoothDevice device = BeaconManager.getmDevices().valueAt(btSpinner.getSelectedItemPosition());
                                result = PointManager.changePoint(Float.parseFloat(xText.getText().toString()), Float.parseFloat(yText.getText().toString()), device.hashCode(), index);
                            }else{
                                BluetoothDevice device = BeaconManager.getmDevices().valueAt(btSpinner.getSelectedItemPosition());
                                result = PointManager.addPoint(Float.parseFloat(xText.getText().toString()), Float.parseFloat(yText.getText().toString()), device.hashCode());
                            }

                            if(result){
                                Toast.makeText(App.getContext(), "Point added", Toast.LENGTH_SHORT).show();
                                BeaconManager.resumeScan();
                                dialog.dismiss();
                            }else{
                                Toast.makeText(App.getContext(), "Invalid values", Toast.LENGTH_SHORT).show();
                            }
                        }catch (NumberFormatException nfe){
                            Toast.makeText(App.getContext(), "Values must be decimal numbers", Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            Toast.makeText(App.getContext(), "Error processing request", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    //Starts bluetooth if not already running
    public void startBlueTooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, 1);
    }

    //Requests location access for better beacon scanning
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestLocation(){
        final Activity activity = this;
        if (App.getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }
            });
            builder.show();
        }
    }

    //Callback for location permissions request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
            }
        }
    }

    //These functions start and stop bluetooth scanning when app is stopped and started
    @Override
    protected void onResume() {
        BeaconManager.resumeScan();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        BeaconManager.stopScanning();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        BeaconManager.stopScanning();
        super.onPause();
    }

    @Override
    protected void onStop() {
        BeaconManager.stopScanning();
        super.onStop();
    }

    //Starts the calibration dialog when the calibrate FAB is pressed
    public void calibrate(View view) {
        new CalibrateDialog().show(getFragmentManager(), "Calibrate Dialog");
    }

    //Formats doubles/floats into strings without trailing zeros
    public static String fmt(float d)
    {
        if(d == (long) d)
            return String.format(Locale.CANADA,"%d",(long)d);
        else
            return Float.toString(d);
    }
}