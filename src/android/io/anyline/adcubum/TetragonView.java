package io.anyline.adcubum;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;

public class TetragonView extends FrameLayout {

    final String TAG = TetragonView.class.getSimpleName();

    Context context;

    //@BindView(getResources().getIdentifier("tetragon_point1", "id", "io.anyline.adcubum"))
    ImageView point1; //starts TopLeft

    //@BindView(getResources().getIdentifier("tetragon_point2", "id", "io.anyline.adcubum"))
    ImageView point2; //starts TopRight

    //@BindView(getResources().getIdentifier("tetragon_point3", "id", "io.anyline.adcubum"))
    ImageView point3; //starts BottomRight

    //@BindView(getResources().getIdentifier("tetragon_point4", "id", "io.anyline.adcubum"))
    ImageView point4; //starts BottomLeft

    private Paint paintEdges = new Paint();
    private Paint paintEdgesAccent = new Paint();
    private Paint paintArea = new Paint();
    private Paint paintAreaAccent = new Paint();

    private ArrayList<ImageView> order;

    private int mImageViewOffsetX = 0;
    private int mImageViewOffsetY = 0;

    private int mImageHeight;
    private int mImageWidth;

    private CornerTouchListener semaphoreOnTouchListener=null;

    private MagnifyImageView magnify = null;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //ButterKnife.bind(this);

        point1 = (ImageView) findViewById(getResources().getIdentifier("tetragon_point1", "id", "io.anyline.adcubum")) ;
        point2= (ImageView) findViewById(getResources().getIdentifier("tetragon_point2", "id", "io.anyline.adcubum")) ;
        point3= (ImageView) findViewById(getResources().getIdentifier("tetragon_point3", "id", "io.anyline.adcubum")) ;
        point4 = (ImageView) findViewById(getResources().getIdentifier("tetragon_point4", "id", "io.anyline.adcubum")) ;

        point1.setOnTouchListener(new CornerTouchListener());
        point2.setOnTouchListener(new CornerTouchListener());
        point3.setOnTouchListener(new CornerTouchListener());
        point4.setOnTouchListener(new CornerTouchListener());

        order = new ArrayList<>();
        order.add(point1);
        order.add(point2);
        order.add(point3);
        order.add(point4);
    }

    public TetragonView(Context context) {
        super(context);
        this.context = context;
        initPaintAttributes();
    }

    public TetragonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initPaintAttributes();
    }

    public TetragonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initPaintAttributes();
    }

    public void setCorners (ArrayList<PointF> corners){
        if(corners.size() != 4)
            throw new IllegalArgumentException("Tetragon View expects exactly 4 corners");

        for(int i = 0; i < corners.size(); i++){

            order.get(i).setX(restrictToXMargin(corners.get(i).x+this.mImageViewOffsetX));
            order.get(i).setY(restrictToYMargin(corners.get(i).y+this.mImageViewOffsetY));

        }
    }

    public ArrayList<PointF> getCorners (){
        ArrayList<PointF> currentCorners = new ArrayList<>();
        for(ImageView corner : order)
            currentCorners.add(new PointF(
                    corner.getX()-this.mImageViewOffsetX,
                    corner.getY()-this.mImageViewOffsetY));

        return currentCorners;
    }

    public void setImageOffset(int scaledHeight,int scaledWidth){
        this.mImageHeight = scaledHeight;
        this.mImageWidth = scaledWidth;

        //Log.d(TAG,"myHeight:"+this.getHeight());
        //Log.d(TAG,"imageHeight:"+scaledHeight);
        this.mImageViewOffsetY = Math.round(((float)(this.getHeight()-scaledHeight))/2-((float)this.point1.getHeight()/2));
        //Log.d(TAG,"imageViewOffsetY:"+mImageViewOffsetY);
        this.mImageViewOffsetX = Math.round(((float)(this.getWidth()-scaledWidth))/2-((float)this.point1.getWidth()/2));
        //Log.d(TAG,"imageViewOffsetX:"+mImageViewOffsetX);
    }

    private void initPaintAttributes() {
        this.paintEdges.setColor(getResources().getColor(getResources().getIdentifier("tetragon_edge_color", "color", "io.anyline.adcubum")));
        TypedValue floatFromR = new TypedValue();
        getResources().getValue(getResources().getIdentifier("tetragon_edge_width", "dimen", "io.anyline.adcubum"), floatFromR, true);
        this.paintEdges.setStrokeWidth(floatFromR.getFloat());
        this.paintEdges.setAntiAlias(true);
        this.paintEdges.setStyle(Paint.Style.STROKE);

        this.paintEdgesAccent.setColor(getResources().getColor(getResources().getIdentifier("tetragon_edge_color_accent", "color", "io.anyline.adcubum")));
        this.paintEdgesAccent.setStrokeWidth(floatFromR.getFloat());
        this.paintEdgesAccent.setAntiAlias(true);
        this.paintEdgesAccent.setStyle(Paint.Style.STROKE);

        this.paintArea.setColor(getResources().getColor(getResources().getIdentifier("tetragon_area_color", "color", "io.anyline.adcubum")));
        this.paintArea.setStyle(Paint.Style.FILL);

        this.paintAreaAccent.setColor(getResources().getColor(getResources().getIdentifier("tetragon_area_color_accent", "color", "io.anyline.adcubum")));
        this.paintAreaAccent.setStyle(Paint.Style.FILL);

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        float offset = this.point1.getWidth()/2;


        Path bounds = new Path();
        bounds.moveTo(order.get(0).getX(),order.get(0).getY());
        for(int i = 1; i <order.size(); i++){
            bounds.lineTo(order.get(i).getX(),order.get(i).getY());
        }
        bounds.close();
        bounds.offset(offset,offset);

        if(isConvex()) {
            canvas.drawPath(bounds, paintArea);
            canvas.drawPath(bounds, paintEdges);
        }else{
            canvas.drawPath(bounds, paintAreaAccent);
            canvas.drawPath(bounds, paintEdgesAccent);
        }
        super.dispatchDraw(canvas);
    }

    private boolean isConvex(){
        float sign= 0;
        ArrayList<EdgeVector> vectors = new ArrayList<>();
        for(int i = 0; i < order.size();i++){
            vectors.add(new EdgeVector(
                    new PointF(order.get((i+1)%order.size()).getX(),
                            order.get((i+1)%order.size()).getY()),
                    new PointF(order.get(i).getX(),
                            order.get(i).getY()
                    )
            ));

        }
        for(int i = 0; i < vectors.size();i++){
            float currSign = getSignOfCrossProduct(vectors.get(i),vectors.get((i+1)%vectors.size()));
            if(i == 0) {
                sign = currSign;
                continue;
            }
            if(currSign != sign)
                return false;
        }
        return true;
    }


    private float restrictToXMargin(float x){
        if(x<mImageViewOffsetX)
            return mImageViewOffsetX;
        if(x>mImageWidth+mImageViewOffsetX)
            return mImageWidth+mImageViewOffsetX;

        return x;

    }

    private float restrictToYMargin(float y){
        if(y<mImageViewOffsetY)
            return mImageViewOffsetY;
        if(y>mImageHeight+mImageViewOffsetY)
            return mImageHeight+mImageViewOffsetY;

        return y;
    }

    private boolean isValidPoint(PointF leftP, PointF oppP, PointF rightP, PointF check){

        EdgeVector leftEdgeVector; // Left Hand line
        EdgeVector rightEdgeVector; // Right Hand line
        EdgeVector checkEdgeVector; // Vector to Point to be checked

        leftEdgeVector = new EdgeVector(leftP, oppP);
        rightEdgeVector = new EdgeVector(rightP,oppP);
        checkEdgeVector = new EdgeVector(check, oppP);

        //Log.d(TAG,""+point1.getWidth());
        //Log.d(TAG,""+distance(check,leftP));

        if( getSignOfCrossProduct(leftEdgeVector,rightEdgeVector) == getSignOfCrossProduct(leftEdgeVector,checkEdgeVector) &&
                getSignOfCrossProduct(leftEdgeVector,rightEdgeVector) * -1.0f == getSignOfCrossProduct(rightEdgeVector,checkEdgeVector) ){
            return true;
        }
        return false;
    }

    private class EdgeVector{
        protected float x;
        protected float y;

        EdgeVector (PointF direction, PointF origin){
            x = direction.x - origin.x;
            y = direction.y - origin.y;
        }
    }

    private float getSignOfCrossProduct(EdgeVector e1, EdgeVector e2){
        return Math.signum(e1.x*e2.y - e1.y*e2.x);
    }

    private class CornerTouchListener implements OnTouchListener {
        //PointF startPosition = new PointF();
        PointF[] otherPoints;
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_MOVE:
                    if(semaphoreOnTouchListener != this)
                        return false;

                    //offset for cornerbutton
                    float touchOffset = view.getWidth()/2;

                    PointF move = new PointF(motionEvent.getX(), motionEvent.getY());



                    if(magnify!=null)
                        magnify.cornerActionMove(
                                //call Magnification with new X,Y coordinates
                                restrictToXMargin(view.getX() + move.x-touchOffset ),
                                restrictToYMargin(view.getY() + move.y-touchOffset ),
                                //and the offset for Imageview
                                mImageViewOffsetX,
                                mImageViewOffsetY);

                    //Log.d(TAG, "move coords:"+move);
                    PointF check = new PointF(view.getX() + move.x-touchOffset,view.getY() + move.y-touchOffset);
                    if(isValidPoint(otherPoints[0],otherPoints[1],otherPoints[2],check)) {

                        float [] distance = new float [3];
                        float minDistance = Float.MAX_VALUE;
                        for (int i = 0; i<distance.length; i++){
                            distance[i] = distance(otherPoints[i],check);
                            if(distance[i] < point1.getWidth()/2){
                                minDistance = distance[i];

                                //Create "Semi-Circle" Rotation if too close to another corner
                                view.setX(restrictToXMargin((point1.getWidth()/2)/minDistance * (check.x-otherPoints[i].x) + otherPoints[i].x));
                                view.setY(restrictToYMargin((point1.getWidth()/2)/minDistance * (check.y-otherPoints[i].y) + otherPoints[i].y));
                            }
                        }
                        if(minDistance == Float.MAX_VALUE){
                            view.setX(restrictToXMargin(check.x));
                            view.setY(restrictToYMargin(check.y));
                        }
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                    if(semaphoreOnTouchListener != null)
                        return false;
                    semaphoreOnTouchListener = this;
                    if(magnify!=null)
                        magnify.cornerActionDown();
                    otherPoints = getOtherPoints(view.getId());
                    break;
                case MotionEvent.ACTION_UP:
                    if(magnify!=null)
                        magnify.cornerActionUp();
                    semaphoreOnTouchListener = null;
                    break;

            }
            TetragonView.this.invalidate();


            return true;
        }
    }

    public void setMagnifyView(MagnifyImageView magnify){
        this.magnify = magnify;
    }

    private PointF[] getOtherPoints(int viewID){
        PointF points [] = {new PointF(0,0),new PointF(0,0),new PointF(0,0)};

        for (int i = 0; i < order.size(); i ++){
            if(order.get(i).getId() == viewID){
                for(int j = 0; j < points.length; j++){
                    i++;
//                    Log.d(TAG,"i="+i+" j="+j);
                    points[j].x = order.get(i%order.size()).getX();
                    points[j].y = order.get(i%order.size()).getY();
                }
            }
        }
        return points;
    }

    private float distance (PointF p1, PointF p2) {
        double base = Math.pow(Math.abs(p1.x - p2.x), 2.0f) + Math.pow(Math.abs(p1.y - p2.y), 2.0f);
        return (float) Math.sqrt(base);
    }

}
