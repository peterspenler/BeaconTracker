package ca.uoguelph.pspenler.beacontracker;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
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

//This class creates a dialog for calibrating the app

public class CalibrateDialog extends DialogFragment {

    private AlertDialog dialog;
    private float dist1 = 0, dist2 = 0, dist3 = 0; //Three distance values entered for calibration
    private double r1 = 0, r2 = 0, r3 = 0; //Three ratios measured for calibration
    private int readingNum = 0; //Number of calibration values entered

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Dialog_Alert)
                .setView(R.layout.dialog_calibrate)
                .setTitle("Calibrate first point")
                .setPositiveButton("Calibrate", null) //Listener is null, action added in OnShowListener
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton("Help", null)
                .create();

        //Using an on show listener to keep the positive button from automatically closing the dialog
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button calibrateButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button helpButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                final EditText distText = dialog.findViewById(R.id.dialogDist);
                final Spinner btSpinner = dialog.findViewById(R.id.calibrateSpinner);
                final TextView spinnerVal = dialog.findViewById(R.id.spinnerVal);
                final TextView rssiView = dialog.findViewById(R.id.rssiText);
                final TextView txpwrView = dialog.findViewById(R.id.txpowerText);

                assert distText != null;
                assert rssiView != null;
                assert txpwrView != null;
                assert btSpinner != null;
                assert spinnerVal != null;

                //This handler and runnable constantly refreshes the
                final Handler handler = new Handler();

                final Runnable refresh = new Runnable(){
                    @Override
                    public void run() {
                        Rssi rssi = BeaconManager.getRSSI(BeaconManager.getmDevices().valueAt(btSpinner.getSelectedItemPosition()).hashCode());
                        rssiView.setText(getString(R.string.rssi_value, rssi.value()));
                        handler.postDelayed(this, 1000);
                    }
                };
                handler.postDelayed(refresh, 1000);

                spinnerVal.setVisibility(View.GONE);

                //Initializing the spinner for selecting beacons
                List<String> categories2 = new ArrayList<>();
                if(BeaconManager.getmDevices() != null){
                    for(int i = 0; i < BeaconManager.getmDevices().size(); i++){
                        categories2.add(BeaconManager.getmDevices().valueAt(i).getAddress());
                    }
                }

                ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<>(App.getContext(), R.layout.spinner_item, categories2);
                dataAdapter2.setDropDownViewResource(R.layout.spinner_item);
                btSpinner.setAdapter(dataAdapter2);
                btSpinner.setSelection(0);
                btSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(readingNum == 0) {
                            Rssi rssi = BeaconManager.getRSSI(BeaconManager.getmDevices().valueAt(btSpinner.getSelectedItemPosition()).hashCode());
                            rssiView.setText(getString(R.string.rssi_value, rssi.value()));
                            txpwrView.setText(getString(R.string.tx_value, rssi.value()));
                        } else{
                            Toast.makeText(App.getContext(), "All calibration measurements must be done with the same beacon", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                calibrateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Rssi rssi = BeaconManager.getRSSI(BeaconManager.getmDevices().valueAt(btSpinner.getSelectedItemPosition()).hashCode());
                        //Records the distance and rssi to TxPower ratio for each calibration step
                        switch(readingNum) {
                            case 0:
                                try {
                                    dist1 = Float.parseFloat(distText.getText().toString());
                                    r1 = ((double)rssi.value()*1.0/rssi.txPower());
                                    readingNum = 1;
                                    dialog.setTitle("Calibrate Second Point");
                                    distText.setText("");

                                    //The spinnerVal textView replaces the spinner after the first calibration so that all calibrations
                                    //are performed with the same beacon
                                    btSpinner.setVisibility(View.GONE);
                                    spinnerVal.setText(BeaconManager.getmDevices().valueAt(btSpinner.getSelectedItemPosition()).getAddress());
                                    spinnerVal.setVisibility(View.VISIBLE);
                                }catch (Exception e){
                                    Toast.makeText(App.getContext(), "Need real distance value", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                                break;
                            case 1:
                                try {
                                    dist2 = Float.parseFloat(distText.getText().toString());
                                    r2 = ((double)rssi.value()*1.0/rssi.txPower());
                                    if(dist2 == dist1){
                                        Toast.makeText(App.getContext(), "All calibration distances must be different", Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                    readingNum = 2;
                                    dialog.setTitle("Calibrate Third Point");
                                    distText.setText("");
                                }catch (Exception e){
                                    Toast.makeText(App.getContext(), "Need real distance value", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                                break;
                            case 2:
                                try {
                                    dist3 = Float.parseFloat(distText.getText().toString());
                                    r3 = ((double)rssi.value()*1.0/rssi.txPower());
                                    if((dist3 == dist1)||(dist3 == dist2)){
                                        Toast.makeText(App.getContext(), "All calibration distances must be different", Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                    readingNum = 3;

                                    //After the third calibration measurement the values are passed to the calibrater class and the dialog is closed
                                    dialog.setTitle("Done Calibrating");
                                    Calibrater.calibrate(dist1, dist2, dist3, r1, r2, r3);
                                    handler.removeCallbacksAndMessages(null); //Clear any pending callbacks from the handler
                                    dialog.dismiss();
                                    Toast.makeText(App.getContext(), "App has been calibrated", Toast.LENGTH_SHORT).show();
                                }catch (Exception e){
                                    Toast.makeText(App.getContext(), "Need real distance value", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                break;
                            }
                        }
                    });
                helpButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Dialog_Alert)
                                .setTitle("Calibration Help")
                                .setMessage("In order to calibrate the app 3 measurements must be taken with the phone at 3 different distances from a single beacon.\n\n" +
                                        "To get the first reading, enter the distance that the phone is from the beacon in meters and tap calibrate.\n\n" +
                                        "Then move the phone to a new distance, enter that distance, and tap calibrate for each of the next two readings\n\n" +
                                        "The app will then automatically use these readings to calibrate itself.")
                                .setPositiveButton(android.R.string.ok, null)
                                .create().show();
                    }
                });
            }
        });

        return dialog;
    }
}
