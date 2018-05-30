package ca.uoguelph.pspenler.beacontracker;

import java.util.ArrayList;

class Point{
      float x;
      float y;
      float realX;
      float realY;

      Point(float x, float y, float realX, float realY){
          this.x = x;
          this.y = y;
          this.realX = realX;
          this.realY = realY;
      }
}

public final class PointManager {

    private static ArrayList<Point> points = new ArrayList<>();
    private static boolean canAddPoints = true;

    public static Point getPoint(int i) {
        return points.get(i);
    }

    public static int numPoints(){
        return points.size();
    }

    public static boolean addPoint(float x, float y, float realX, float realY){
        if(canAddPoints) {
            points.add(new Point(x, y, realX, realY));
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
}
