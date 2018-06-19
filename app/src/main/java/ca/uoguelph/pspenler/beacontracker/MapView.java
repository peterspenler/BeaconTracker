package ca.uoguelph.pspenler.beacontracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;

public class MapView extends android.support.v7.widget.AppCompatImageView {

    private static final int MAX_CLICK_DURATION = 150;
    private static final int MAX_TAP_DISTANCE = 30;
    private long startClickTime;

    private int touchMod = 0;

    private Context context;

    private PointManager p;
    private Paint pointPaint = new Paint();
    private Paint textPaint = new Paint();
    private Paint mapPaint = new Paint();

    private int screenHeight;
    private int screenWidth;

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        pointPaint.setColor(getResources().getColor(R.color.colorAccent));
        textPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
        mapPaint.setColor(getResources().getColor(R.color.background));

        screenHeight = App.getScreenHeight();
        screenWidth = App.getScreenWidth();

        this.context = context;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        for (int i = 0; i < p.numPoints(); i++){
            canvas.drawCircle(p.getPoint(i).x, p.getPoint(i).y, 10, pointPaint);
            canvas.drawText(Integer.toString(BeaconManager.getRSSI(PointManager.getPoint(i).device)), p.getPoint(i).x, p.getPoint(i).y, textPaint);
        }
        canvas.restore();
    }

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
                        dist = (float) Math.sqrt(Math.pow((p.getPoint(i).x - xPos), 2) + Math.pow((p.getPoint(i).y - yPos - touchMod), 2));
                        if((dist < closeDist) && (dist < MAX_TAP_DISTANCE)){
                            closeDist = dist;
                            closeID = i;
                        }
                    }

                    if(closeID != -1){
                        Toast.makeText(context, "Existing Point", Toast.LENGTH_SHORT).show();
                        ((MainActivity)context).pointDialog(true, closeID);
                    }
                }
                break;
        }
        return true;
    }
}
