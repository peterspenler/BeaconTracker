package ca.uoguelph.pspenler.beacontracker;

import android.util.Log;

//The Calibrater class calculates the three values needed for calculating distance from a beacon based on RSSI
public final class Calibrater {

    private static double A = 0.5502258081, B = 6.3279130055, C = 0.2970323207; //Variables for the distance calculation
    private static double dist1 = 0, dist2 = 0, dist3 = 0; //Inputted distance values
    private static double ratio1 = 0, ratio2 = 0, ratio3 = 0; // Ratios between RSSI and TxPwr

    //this method stores the three distance and three ratio values needed, and then calculates the A, B, and C values.
    public static boolean calibrate(double d1, double d2, double d3, double r1, double r2, double r3){
        if(d1 < 1 || d2 < 1 || d3 < 1 || r1 < 1 || r2 < 1 || r3 < 1){
            return false;
        }
        dist1 = d1;
        dist2 = d2;
        dist3 = d3;
        ratio1 = r1;
        ratio2 = r2;
        ratio3 = r3;
        //Values must be calculated in this order as the A calculation needs
        //the B value, and the C calculation needs the A and B values.
        findB();
        findA();
        findC();
        Log.e("A", Double.toString(A));
        Log.e("B", Double.toString(B));
        Log.e("C", Double.toString(C));
        return true;
    }

    //Finds the A value using the dist1, dist2, ratio1, ratio2, and B values.
    private static double findA(){
        A = (dist1 - dist2) / (Math.pow(ratio1, B) - Math.pow(ratio2, B));
        return A;
    }

    //Finds the B value using the dist2, dist3, ratio2, and ratio3 values.
    private static double findB(){
        B = (Math.log10(dist2) - Math.log10(dist3))/(Math.log10(ratio2) - Math.log10(ratio3));
        return B;
    }

    //Finds the C value using the dist1, ratio1, A, and B values.
    private static double findC(){
        C = dist1 - (A * Math.pow(ratio1, B));
        return C;
    }

    //Returns the A value
    public static double A(){
        return A;
    }

    //Returns the B value
    public static double B(){
        return B;
    }

    //Returns the C value
    public static double C(){
        return C;
    }
}
