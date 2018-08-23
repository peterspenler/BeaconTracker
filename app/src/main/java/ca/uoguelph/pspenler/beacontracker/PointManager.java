package ca.uoguelph.pspenler.beacontracker;

import android.util.SparseArray;

import java.util.ArrayList;

//The Point class stores the real X and Y values, the virtual X and Y values,
//and the hash of the associated beacon
class Point{
      float x;
      float y;
      float realX;
      float realY;
      int device;

      Point(float realX, float realY, int device){
          this.x = -1;
          this.y = -1;
          this.realX = realX;
          this.realY = realY;
          this.device = device;
      }
}

public final class PointManager {

    private static final ArrayList<Point> points = new ArrayList<>(); //List of points
    private static boolean canAddPoints = true; //Whether or not points can be added

    //These variables store the scaling factors to turn real X and Y values into virtual X and Y values
    private static  float factorX = 0; //Scaling factor for real X to display X
    private static float factorY = 0; //Scaling factor for real Y to display Y

    //These variables store the extremes of the beacon coordinates
    private static float maxStart = Float.MAX_VALUE; //Farthest right beacon coordinate
    private static float maxEnd = Float.MIN_VALUE; //Farthest left beacon coordinate
    private static float maxTop = Float.MAX_VALUE; //Farthest up beacon coordinate
    private static float maxBottom = Float.MIN_VALUE; //Farthest down beacon coordinate

    //Gets point by index
    public static Point getPoint(int i) {
        return points.get(i);
    }

    //Returns number of points
    public static int numPoints(){
        return points.size();
    }

    //Adds point to the list
    public static boolean addPoint(float realX, float realY, int device){
        if(canAddPoints) {
            points.add(new Point(realX, realY, device));
            remapPoints(false);
            return true;
        }
        return false;
    }

    //Update point values
    public static boolean changePoint(float realX, float realY, int device, int i){
        if(canAddPoints){
            points.get(i).realX = realX;
            points.get(i).realY = realY;
            points.get(i).device = device;
            remapPoints(true);
            return true;
        }
        return false;
    }

    //Disables the ability to add points
    public static void donePoints(){
        canAddPoints = false;
    }

    //Returns whether or not points can be added
    public static boolean canAddPoints() {
        return canAddPoints;
    }

    //Returns whether or not a bluetooth device has already been assigned to a point
    public static boolean pointUsed(int hashcode){
        for(int i = 0; i < points.size(); i++){
            if(points.get(i).hashCode() == hashcode){
                return true;
            }
        }
        return false;
    }

    //Recalculates the display position of points on the map
    private static void remapPoints(boolean change){
        int i;
        int height = App.getScreenHeight();
        int width = App.getScreenWidth();

        //Handles mappings with a single point
        if(points.size() == 1){
            points.get(0).x = width / 2;
            points.get(0).y = height / 2;
            maxStart = maxEnd = points.get(0).realX;
            maxTop = maxBottom = points.get(0).realY;
            return;
        }

        //Resets the maximum coordinates if a point's position has changed
        if(change){
            maxStart = Float.MAX_VALUE;
            maxEnd = Float.MIN_VALUE + 1;
            maxTop = Float.MAX_VALUE;
            maxBottom = Float.MIN_VALUE + 1;
        }

        //Finds the maximum point coordinates
        for(i = 0; i < points.size(); i++){
            if(points.get(i).realX > maxEnd)
                maxEnd = points.get(i).realX;
            if(points.get(i).realX < maxStart)
                maxStart = points.get(i).realX;
            if(points.get(i).realY < maxTop)
                maxTop = points.get(i).realY;
            if(points.get(i).realY > maxBottom)
                maxBottom = points.get(i).realY;
        }

        factorX = (width - 100) / (maxEnd - maxStart);
        factorY = (height - 400) / (maxBottom - maxTop);

        //Handles cases where all X coordinates are the same
        if(maxStart == maxEnd){
            for(i = 0; i < points.size(); i++){
                points.get(i).x = width / 2;
            }
        }
        //Calculates all virtual X values
        else{
            for(i = 0; i < points.size(); i++){
                points.get(i).x = 50 + (factorX * (points.get(i).realX - maxStart));
            }
        }
        //Handles cases where all Y coordinates are the same
        if(maxTop == maxBottom){
            for(i = 0; i < points.size(); i++){
                points.get(i).y = height / 2;
            }
        }
        //Calculates all virtual Y values
        else {
            for (i = 0; i < points.size(); i++) {
                points.get(i).y = 50 + (factorY * (points.get(i).realY - maxTop));
            }
        }
    }

    //Gets display X position from real X position
    public static int findXFromRealX(float realX){
        int pos = (int)(50 + (factorX * (realX - maxStart)));
        if(pos > (App.getScreenWidth() - 25))
            return App.getScreenWidth() - 25;
        if(pos < 25)
            return 25;
        return pos;
    }

    //Gets display Y position from real Y position
    public static int findYFromRealY(float realY){
        int pos = (int)(50 + (factorY * (realY - maxTop)));
        if(pos > (App.getScreenHeight() - 50))
            return App.getScreenHeight() - 50;
        if(pos < 25)
            return 25;
        return pos;
    }

    //Calculates the distance to a device based on rssi
    private static double calculateAccuracy(int rssi, int txPower) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }
        double ratio = ((double)rssi)*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            return  Calibrater.A()*Math.pow(ratio,Calibrater.B()) + Calibrater.C();
        }
    }

    //Calculates X position of phone relative to bluetooth devices
    public static float phoneX(){
        if(points.size() < 3)
            return 0;

        double A, B, C, D, E, F, r1, r2, r3;

        int [] RSSIs = closeRSSI();

        r1 = calculateAccuracy(BeaconManager.getRSSI(points.get(RSSIs[0]).device).value(), BeaconManager.getRSSI(points.get(0).device).txPower());
        r2 = calculateAccuracy(BeaconManager.getRSSI(points.get(RSSIs[1]).device).value(), BeaconManager.getRSSI(points.get(1).device).txPower());
        r3 = calculateAccuracy(BeaconManager.getRSSI(points.get(RSSIs[2]).device).value(), BeaconManager.getRSSI(points.get(2).device).txPower());

        A = -2*points.get(0).realX + 2*points.get(1).realX;
        B = -2*points.get(0).realY + 2*points.get(1).realY;
        C = Math.pow(r1, 2) - Math.pow(r2, 2) - Math.pow(points.get(0).realX, 2)
                + Math.pow(points.get(1).realX, 2) - Math.pow(points.get(0).realY, 2)
                + Math.pow(points.get(1).realY, 2);
        D = -2*points.get(1).realX + 2*points.get(2).realX;
        E = -2*points.get(1).realY + 2*points.get(2).realY;
        F = Math.pow(r2, 2) - Math.pow(r3, 2) - Math.pow(points.get(1).realX, 2)
                + Math.pow(points.get(2).realX, 2) - Math.pow(points.get(1).realY, 2)
                + Math.pow(points.get(2).realY, 2);

        return (float) (((C*E) - (F*B))/((E*A) - (B*D)));
    }

    //Calculates Y position of phone relative to bluetooth devices
    public static float phoneY(){
        if(points.size() < 3)
            return 0;

        double A, B, C, D, E, F, r1, r2, r3;

        int [] RSSIs = closeRSSI();

        r1 = calculateAccuracy(BeaconManager.getRSSI(points.get(RSSIs[0]).device).value(), BeaconManager.getRSSI(points.get(0).device).txPower());
        r2 = calculateAccuracy(BeaconManager.getRSSI(points.get(RSSIs[1]).device).value(), BeaconManager.getRSSI(points.get(1).device).txPower());
        r3 = calculateAccuracy(BeaconManager.getRSSI(points.get(RSSIs[2]).device).value(), BeaconManager.getRSSI(points.get(2).device).txPower());

        A = -2*points.get(0).realX + 2*points.get(1).realX;
        B = -2*points.get(0).realY + 2*points.get(1).realY;
        C = Math.pow(r1, 2) - Math.pow(r2, 2) - Math.pow(points.get(0).realX, 2)
                + Math.pow(points.get(1).realX, 2) - Math.pow(points.get(0).realY, 2)
                + Math.pow(points.get(1).realY, 2);
        D = -2*points.get(1).realX + 2*points.get(2).realX;
        E = -2*points.get(1).realY + 2*points.get(2).realY;
        F = Math.pow(r2, 2) - Math.pow(r3, 2) - Math.pow(points.get(1).realX, 2)
                + Math.pow(points.get(2).realX, 2) - Math.pow(points.get(1).realY, 2)
                + Math.pow(points.get(2).realY, 2);

        return (float) (((C*D) - (F*A))/((B*D) - (A*E)));
    }

    //Finds the index of the 3 devices with the closest RSSIs in order to handle many points
    private static int[] closeRSSI(){
        int values[] = {0,1,2};

        SparseArray<Rssi> RSSIs = BeaconManager.getmRssis();

        for(int i = 0; i < points.size(); i++){
            for(int j = 0; j < 3; j++){
                if(RSSIs.get(points.get(i).device).value() >= RSSIs.get(points.get(values[j]).device).value()){
                    values[j] = i;
                    break;
                }
            }
        }
        return values;
    }
}