package ca.uoguelph.pspenler.beacontracker;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class App extends Application {

    private static Application sApplication;

    public static Application getApplication() {
        return sApplication;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
    }

    public static int getScreenHeight(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public static int getScreenWidth(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static boolean isFirstOpen(){
        SharedPreferences sharedPref = getContext().getSharedPreferences("useData", Context.MODE_PRIVATE);
        Boolean firstUse = sharedPref.getBoolean("firstUse", true);
        return firstUse;
    }

    public static void firstOpen(){
        SharedPreferences.Editor editor = getContext().getSharedPreferences("useData", MODE_PRIVATE).edit();
        editor.putBoolean("firstUse", false);
        editor.apply();
    }
}