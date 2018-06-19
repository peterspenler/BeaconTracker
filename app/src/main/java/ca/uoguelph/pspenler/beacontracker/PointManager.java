package ca.uoguelph.pspenler.beacontracker;

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
        return (int)(50 + (factorX * (realX - maxStart)));
    }

    public static int findYFromRealY(float realY){
        return (int)(50 + (factorY * (realY - maxTop)));
    }

    public void setPointDevice(int i, int device){
        points.get(i).device = device;
    }
}
