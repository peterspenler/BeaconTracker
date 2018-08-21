package ca.uoguelph.pspenler.beacontracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.Calendar;

//The MapView class draws the phone point and beacon points on a view.
public class MapView extends android.support.v7.widget.AppCompatImageView {

    private static final int MAX_CLICK_DURATION = 150; //Length of click in ms
    private static final int MAX_TAP_DISTANCE = 30; //Distance tap can be from point to register
    private long startClickTime; //Holds the time the tap starts

    private Context context; //Application context

    private Paint pointPaint = new Paint(); //Paint for the beacon points
    private Paint textPaint = new Paint(); //Paint for the text
    private Paint phonePaint = new Paint(); //Paint for the phone point

    //Sets paint colours and context
    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        pointPaint.setColor(getResources().getColor(R.color.colorAccent));
        textPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
        phonePaint.setColor(getResources().getColor(R.color.background));

        this.context = context;
    }

    //Draws all points on map
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        int phoneX = PointManager.findXFromRealX(PointManager.phoneX());
        int phoneY = PointManager.findYFromRealY(PointManager.phoneY());
        canvas.drawCircle(phoneX, phoneY, 10, phonePaint);
        for (int i = 0; i < PointManager.numPoints(); i++){
            canvas.drawCircle(PointManager.getPoint(i).x, PointManager.getPoint(i).y, 10, pointPaint);
            canvas.drawText(Integer.toString(BeaconManager.getRSSI(PointManager.getPoint(i).device).value()), PointManager.getPoint(i).x, PointManager.getPoint(i).y, textPaint);
        }
        canvas.drawText("X: " + Float.toString(PointManager.phoneX()), 10, 10, textPaint);
        canvas.drawText("Y: " + Float.toString(PointManager.phoneY()), 10, 20, textPaint);
        canvas.restore();
        invalidate();
    }

    //Lets points be edited when tapped
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                startClickTime = Calendar.getInstance().getTimeInMillis();
                break;
            case MotionEvent.ACTION_UP:
                long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                if(clickDuration < MAX_CLICK_DURATION){
                    float xPos = event.getX();
                    float yPos = event.getY();
                    float dist;
                    int closeID = -1;
                    float closeDist = 999999999;

                    for(int i = 0; i < PointManager.numPoints(); i++){
                        int touchMod = 0;
                        dist = (float) Math.sqrt(Math.pow((PointManager.getPoint(i).x - xPos), 2) + Math.pow((PointManager.getPoint(i).y - yPos - touchMod), 2));
                        if((dist < closeDist) && (dist < MAX_TAP_DISTANCE)){
                            closeDist = dist;
                            closeID = i;
                        }
                    }

                    if(closeID != -1){
                        ((MainActivity)context).pointDialog(true, closeID);
                    }
                }
                break;
        }
        return true;
    }
}
