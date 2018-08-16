package ca.uoguelph.pspenler.beacontracker;

public final class Calibrater {

    private static double A = 0.5502258081, B = 6.3279130055, C = 0.2970323207; //Variables for the distance calculation
    private double dist1 = 0, dist2 = 0, dist3 = 0; //Inputted distance values
    private double r1 = 0, r2 = 0, r3 = 0; // Ratios between RSSI and TxPwr

    public boolean calibrate(double d1, double d2, double d3, double r1, double r2, double r3){
        if(d1 < 1 || d2 < 1 || d3 < 1 || r1 < 1 || r2 < 1 || r3 < 1){
            return false;
        }
        dist1 = d1;
        dist2 = d2;
        dist3 = d3;
        this.r1 = r1;
        this.r2 = r2;
        this.r3 = r3;
        findB();
        findA();
        findC();
        return true;
    }

    private double findA(){
        A = (dist1 - dist2) / (Math.pow(r1, B) - Math.pow(r2, B));
        return A;
    }

    private double findB(){
        B = (Math.log10(dist2) - Math.log10(dist3))/(Math.log10(r2) - Math.log10(r3));
        return B;
    }

    private double findC(){
        C = dist1 - (A * Math.pow(r1, B));
        return C;
    }

    public static double A(){
        return A;
    }

    public static double B(){
        return B;
    }

    public static double C(){
        return C;
    }
}
