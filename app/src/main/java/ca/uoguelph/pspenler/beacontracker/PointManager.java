package ca.uoguelph.pspenler.beacontracker;

import android.util.SparseArray;

import java.util.ArrayList;

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

    private static ArrayList<Point> points = new ArrayList<>();
    private static boolean canAddPoints = true;

    private static int height = 0;
    private static int width = 0;
    private static  float factorX = 0;
    private static float factorY = 0;
    private static float maxStart = Float.MAX_VALUE;
    private static float maxEnd = Float.MIN_VALUE;
    private static float maxTop = Float.MAX_VALUE;
    private static float maxBottom = Float.MIN_VALUE;

    public static Point getPoint(int i) {
        return points.get(i);
    }

    public static int numPoints(){
        return points.size();
    }

    public static boolean addPoint(float realX, float realY, int device){
        if(canAddPoints) {
            points.add(new Point(realX, realY, device));
            remapPoints(false);
            return true;
        }
        return false;
    }

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

    public static void donePoints(){
        canAddPoints = false;
    }

    public static boolean canAddPoints() {
        return canAddPoints;
    }

    public static boolean pointUsed(int hashcode){
        for(int i = 0; i < points.size(); i++){
            if(points.get(i).hashCode() == hashcode){
                return true;
            }
        }
        return false;
    }

    private static void remapPoints(boolean change){
        int i;
        height = App.getScreenHeight();
        width = App.getScreenWidth();

        if(points.size() == 1){
            points.get(0).x = width / 2;
            points.get(0).y = height / 2;
            return;
        }

        if(change){
            maxStart = Float.MAX_VALUE;
            maxEnd = Float.MIN_VALUE;
            maxTop = Float.MAX_VALUE;
            maxBottom = Float.MIN_VALUE;
        }

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

        for(i = 0; i < points.size(); i++){
            points.get(i).x = 50 + (factorX * (points.get(i).realX - maxStart));
            points.get(i).y = 50 + (factorY * (points.get(i).realY - maxTop));
        }
    }

    public static int findXFromRealX(float realX){
        int pos = (int)(50 + (factorX * (realX - maxStart)));
        if(pos > (App.getScreenWidth() - 25))
            return App.getScreenWidth() - 25;
        if(pos < 25)
            return 25;
        return pos;
    }

    public static int findYFromRealY(float realY){
        int pos = (int)(50 + (factorY * (realY - maxTop)));
        if(pos > (App.getScreenHeight() - 25))
            return App.getScreenHeight() - 25;
        if(pos < 25)
            return 25;
        return pos;
    }

    public void setPointDevice(int i, int device){
        points.get(i).device = device;
    }

    private static double rssiToMeter(int rssi, int txPower){
        /*int*/ txPower = -76;
    /*
        if(rssi == 0)
            return -1;

        float ratio = (float) rssi * (1/txPower);

        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double distance =  (0.89976) * Math.pow(ratio,7.7095) + 0.111;
            return distance;
        }*/
        return Math.pow(10d, ((double) txPower - rssi) / (10 * 2));
    }

    private static double calculateAccuracy(int rssi, int txPower) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = ((double)rssi)*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            return  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
        }
    }

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
