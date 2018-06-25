package ca.uoguelph.pspenler.beacontracker;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public final class MainActivity extends AppCompatActivity {

    private static Context context;
    private static Activity activity;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        activity = this;

        fab = findViewById(R.id.addPointFab);
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(activity, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
                        .setMessage("Finish adding points?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PointManager.donePoints();
                                fab.setVisibility(View.INVISIBLE);
                                Toast.makeText(context, "Finished adding points", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", null)
                        .create();
                dialog.show();
                return true;
            }
        });
        BeaconManager.initialize(context, activity);
    }

    public void addPointDialog(View view){
        pointDialog(false, -1);
    }

    public void pointDialog(final boolean changing, final int index){
        String title;
        if(changing)
            title = "Change Point";
        else
            title = "Add Point";

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
                final EditText xText = dialog.findViewById(R.id.dialogXValue);
                final EditText yText = dialog.findViewById(R.id.dialogYValue);
                final Spinner btSpinner = dialog.findViewById(R.id.bluetoothDeviceSpinner);
                final TextView rssiView = dialog.findViewById(R.id.rssiText);
                final TextView txpwrView = dialog.findViewById(R.id.txpowerText);

                assert xText != null;
                assert yText != null;
                assert rssiView != null;
                assert txpwrView != null;

                BeaconManager.stopScanning();

                List<String> categories = new ArrayList<String>();
                if(BeaconManager.getmDevices() != null){
                    for(int i = 0; i < BeaconManager.getmDevices().size(); i++){
                        categories.add(BeaconManager.getmDevices().valueAt(i).getAddress());
                    }
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, R.layout.spinner_item, categories);
                dataAdapter.setDropDownViewResource(R.layout.spinner_item);
                btSpinner.setAdapter(dataAdapter);
                btSpinner.setSelection(0);
                btSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Rssi rssi = BeaconManager.getRSSI(BeaconManager.getmDevices().valueAt(btSpinner.getSelectedItemPosition()).hashCode());
                        rssiView.setText("RSSI: " + rssi.value());
                        txpwrView.setText("TxPwr: " + rssi.txPower());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                if(changing){
                    Point point = PointManager.getPoint(index);
                    xText.setText(Float.toString(point.realX));
                    yText.setText(Float.toString(point.realY));
                    int spinnerIndex = BeaconManager.indexFromHash(PointManager.getPoint(index).device);
                    if(spinnerIndex >= 0) {
                        btSpinner.setSelection(spinnerIndex);
                    }
                }

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
                                Toast.makeText(activity, "Point added", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                BeaconManager.resumeScan();
                            }else{
                                Toast.makeText(activity, "Invalid values", Toast.LENGTH_SHORT).show();
                            }
                        }catch (NumberFormatException nfe){
                            Toast.makeText(activity, "Values must be decimal numbers", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    public void startBlueTooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, 1);
    }

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

}
