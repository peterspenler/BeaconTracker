package ca.uoguelph.pspenler.beacontracker;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.Objects;

public class App extends Application {

    private static Application sApplication; //Stores application reference

    //Returns application in a static context
    public static Application getApplication() {
        return sApplication;
    }

    //Returns application context in a static context
    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
    }

    //Returns screen height
    public static int getScreenHeight(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) Objects.requireNonNull(getContext().getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    //Returns screen width
    public static int getScreenWidth(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) Objects.requireNonNull(getContext().getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    //Checks whether or not this is the first time the app is opened
    public static boolean isFirstOpen(){
        SharedPreferences sharedPref = getContext().getSharedPreferences("useData", Context.MODE_PRIVATE);
        return sharedPref.getBoolean("firstUse", true);
    }

    //Designates that the app has been opened for the first time
    public static void firstOpen(){
        SharedPreferences.Editor editor = getContext().getSharedPreferences("useData", MODE_PRIVATE).edit();
        editor.putBoolean("firstUse", false);
        editor.apply();
    }
}