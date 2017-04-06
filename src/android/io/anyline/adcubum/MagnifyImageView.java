package io.anyline.adcubum;

import android.content.Context;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

/**
 * Created by Lois-9Y on 30/08/2016.
 */
public class MagnifyImageView extends ImageView {

    private final String TAG = MagnifyImageView.class.getSimpleName();

    private Context context;

    private boolean zooming = false;

    private ImageView magnifyTarget;
    private PointF zoomPosition = new PointF();
    private BitmapShader shader;
    private Matrix matrix = new Matrix();;
    private Paint shaderPaint;

    private boolean topLeftAnchor= true;


    private float scaleFactor=1.0f;
    private float magFactor;

    private Paint crosshairPaint;
    TranslateAnimation animation;


    //public MagnifyOnTouchListener magnifyOnTouchListener = new MagnifyOnTouchListener();

    private void initValues(){
        TypedValue magFactor = new TypedValue();
        getResources().getValue(getResources().getIdentifier("magnify_magnification_factor", "dimen", this.context.getPackageName()), magFactor, true);
        this.magFactor = magFactor.getFloat();



        crosshairPaint = new Paint();
        crosshairPaint.setColor(getResources().getIdentifier("tetragon_edge_color", "color", this.context.getPackageName()));
        crosshairPaint.setStyle(Paint.Style.STROKE);
        crosshairPaint.setStrokeWidth(getResources().getIdentifier("magnify_crosshair_stroke", "color", this.context.getPackageName()));
        crosshairPaint.setAntiAlias(true);

    }

    public MagnifyImageView(Context context) {
        super(context);
        this.context = context;
        initValues();

    }

    public MagnifyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initValues();
    }

    public MagnifyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initValues();
    }

    public void setScaleFactor(float factor){
        this.scaleFactor=factor;
    }

    public void setMagnifyTarget(ImageView magnifyTarget){
        this.magnifyTarget = magnifyTarget;
        shader = new BitmapShader( ((BitmapDrawable)magnifyTarget.getDrawable()).getBitmap(),
                Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP);
        shaderPaint = new Paint();

        shaderPaint.setShader(shader);
    }

    public void cornerActionDown (){
        zooming = true;
        MagnifyImageView.this.setVisibility(VISIBLE);
        invalidate();
    }

    private int getRelativeLeft(View myView) {
        if (myView.getParent() == myView.getRootView())
            return myView.getLeft();
        else
            return myView.getLeft() + getRelativeLeft((View) myView.getParent());
    }

    public void cornerActionMove(float  x, float y, float offsetX, float offsetY){

        final int rightXAnchor = ((View)getParent()).getRight() -getWidth()-getRelativeLeft(MagnifyImageView.this);
        final int leftXAnchor = getRelativeLeft(this);



        if((animation == null || animation.hasEnded())
                && (x-offsetX) < getWidth() && (y-offsetY)< getWidth() && topLeftAnchor) {


            animation = new TranslateAnimation(0, rightXAnchor, 0, 0);
            animation.setDuration(300);

            animation.setFillAfter(false);
            animation.setAnimationListener(new Animation.AnimationListener(){
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    setX(rightXAnchor);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            this.startAnimation(animation);

            topLeftAnchor = false;
        }
        else if ((animation == null || animation.hasEnded())
                && !topLeftAnchor  && ((x-offsetX)>getWidth() || (y-offsetY) >getWidth())){

            animation = new TranslateAnimation(0, -rightXAnchor, 0, 0);
            animation.setDuration(300);
            //animation.setStartOffset(300);
            animation.setFillAfter(false);
            animation.setAnimationListener(new Animation.AnimationListener(){
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    setX(leftXAnchor);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            this.startAnimation(animation);
            topLeftAnchor = true;
        }

        zoomPosition.x = (x-offsetX)/scaleFactor;
        zoomPosition.y = (y-offsetY)/scaleFactor ;
        invalidate();
    }

    public void cornerActionUp(){
        MagnifyImageView.this.setVisibility(INVISIBLE);
        zooming = false;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(zooming) {
            //Log.d(TAG, "am i zooming? :");

            float magnifySize =  this.getWidth()/2;

            matrix.reset();
            matrix.postScale(magFactor,magFactor);
            matrix.postTranslate(-zoomPosition.x*magFactor+magnifySize,-zoomPosition.y*magFactor+magnifySize);
            //matrix.postScale(magFactor,magFactor,);
            shaderPaint.getShader().setLocalMatrix(matrix);
            shaderPaint.setAntiAlias(true);

            float radius = getWidth()/2;
            float border =  getResources().getDimension(getResources().getIdentifier("magnify_border", "dimen", this.context));

            canvas.drawCircle(radius  , radius, radius-border, shaderPaint);

            //draw crosshair

            canvas.drawCircle(radius  , radius, getResources().getDimension(getResources().getIdentifier("magnify_crosshair_radius", "dimen", this.context.getPackageName())), crosshairPaint);
        }
    }
    //not used:
//    private class MagnifyOnTouchListener implements OnTouchListener{
//        @Override
//        public boolean onTouch(View view, MotionEvent motionEvent) {
//
//            int action = motionEvent.getAction();
//
//            switch (action) {
//                case MotionEvent.ACTION_DOWN:
//                    zooming = true;
//                    MagnifyImageView.this.setVisibility(VISIBLE);
//
//
//                    shader = new BitmapShader( ((BitmapDrawable)((ImageView)view).getDrawable()).getBitmap(),
//                            Shader.TileMode.CLAMP,
//                            Shader.TileMode.CLAMP);
//
//                    shaderPaint = new Paint();
//
//                    shaderPaint.setShader(shader);
//                    invalidate();
//
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    zoomPosition.x = motionEvent.getX()/(1/magFactor*scaleFactor)-view.getX();
//                    zoomPosition.y = motionEvent.getY()/(1/magFactor*scaleFactor)-view.getY();
//                    invalidate();
//                    break;
//                case MotionEvent.ACTION_UP:
//                    MagnifyImageView.this.setVisibility(INVISIBLE);
//                    zooming = false;
//                    invalidate();
//                    break;
//            }
//
//            return true;
//        }
//    }
}
