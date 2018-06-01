package ca.uoguelph.pspenler.beacontracker;

import android.util.DisplayMetrics;

import java.util.ArrayList;

class Point{
      float x;
      float y;
      float realX;
      float realY;

      Point(float realX, float realY){
          this.x = -1;
          this.y = -1;
          this.realX = realX;
          this.realY = realY;
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

    public static boolean addPoint(float realX, float realY){
        if(canAddPoints) {
            points.add(new Point(realX, realY));
            remapPoints();
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

    private static void remapPoints(){
        int i;
        height = App.getScreenHeight();
        width = App.getScreenWidth();

        if(points.size() == 1){
            points.get(0).x = width / 2;
            points.get(0).y = height / 2;
            return;
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
}
