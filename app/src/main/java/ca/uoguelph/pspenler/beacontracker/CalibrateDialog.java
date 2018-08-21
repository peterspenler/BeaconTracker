package ca.uoguelph.pspenler.beacontracker;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
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

public class CalibrateDialog extends DialogFragment {

    AlertDialog dialog;
    float dist1 = 0, dist2 = 0, dist3 = 0;
    double r1 = 0, r2 = 0, r3 = 0;
    int readingNum = 0;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Dialog_Alert)
                .setView(R.layout.dialog_calibrate)
                .setTitle("Calibrate first point")
                .setPositiveButton("Calibrate", null) //Set to null. We override the onclick
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
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

                final Handler handler = new Handler();
                int btPosition = 0;

                final Runnable refresh = new Runnable(){
                    @Override
                    public void run() {
                        Rssi rssi = BeaconManager.getRSSI(BeaconManager.getmDevices().valueAt(btSpinner.getSelectedItemPosition()).hashCode());
                        rssiView.setText("RSSI: " + rssi.value());
                        handler.postDelayed(this, 1000);
                    }
                };
                handler.postDelayed(refresh, 1000);

                spinnerVal.setVisibility(View.GONE);

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
                            rssiView.setText("RSSI: " + rssi.value());
                            txpwrView.setText("TxPwr: " + rssi.txPower());
                        } else{
                            Toast.makeText(App.getContext(), "All calibration measurements must be done with the same beacon", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Rssi rssi = BeaconManager.getRSSI(BeaconManager.getmDevices().valueAt(btSpinner.getSelectedItemPosition()).hashCode());
                        switch(readingNum) {
                            case 0:
                                try {
                                    dist1 = Float.parseFloat(distText.getText().toString());
                                    r1 = ((double)rssi.value()*1.0/rssi.txPower());
                                    readingNum = 1;
                                    dialog.setTitle("Calibrate Second Point");
                                    distText.setText("");
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
                                    dialog.setTitle("Done Calibrating");
                                    Calibrater.calibrate(dist1, dist2, dist3, r1, r2, r3);
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
            }
        });

        return dialog;
    }
}
