package ca.uoguelph.pspenler.beacontracker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        activity = this;
    }

    protected static void addPointDialog(final float screenX, final float screenY){
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
                EditText yText = dialog.findViewById(R.id.dialogYValue);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something
                        try {
                            if (PointManager.addPoint(screenX, screenY, Float.parseFloat(xText.getText().toString()), Float.parseFloat(xText.getText().toString()))) {
                                Toast.makeText(activity, "Point added", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(activity, "Invalid values", Toast.LENGTH_SHORT).show();
                            }
                        }catch (NumberFormatException nfe){
                            Toast.makeText(activity, "Values must be decimal numbers", Toast.LENGTH_SHORT).show();
                        }
                        //Dismiss once everything is OK.
                        //dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }
}
