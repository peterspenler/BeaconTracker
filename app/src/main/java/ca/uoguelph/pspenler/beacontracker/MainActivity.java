package ca.uoguelph.pspenler.beacontracker;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

    public static void addPointDialog(View view){
        final AlertDialog dialog = new AlertDialog.Builder(activity, R.style.Theme_AppCompat_Dialog_Alert)
                .setView(R.layout.dialog_add_point)
                .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                final EditText xText = dialog.findViewById(R.id.dialogXValue);
                final EditText yText = dialog.findViewById(R.id.dialogYValue);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something
                        try {
                            if (PointManager.addPoint(Float.parseFloat(xText.getText().toString()), Float.parseFloat(yText.getText().toString()))) {
                                Toast.makeText(activity, "Point added", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
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
