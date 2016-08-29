package com.hougr.smartisanpull;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by hougr on 16/8/25.
 */
public class SmartisanCircleView extends View {
//    public static final float LINE_LENGTH = 45;
//    private static final float ARROW_LENGTH = 8;
//    public static final float CIRCLE_RADIUS = 20;

    float Width;
    float Height;
    public float mLineLength;
    public float mArrowLength;
    public float mCircleRadius;
    public float mStrokeWidth;
    public float mLineSpaceHalf;
    Paint mPaint;

    private int mRefreshStatus;
    private float mAnimatorDistance;
//    private long animatorDuration = 5000;
//    private TimeInterpolator timeInterpolator = new DecelerateInterpolator();
//    private TimeInterpolator timeInterpolator = new LinearInterpolator();


    public SmartisanCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);


        mRefreshStatus = RefreshStatus.STATUS_ORIGIN;
        mAnimatorDistance =0;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Width = getWidth();
        Height = getHeight();

        mCircleRadius = Height/2f/(float) (2* Math.PI);
        mStrokeWidth = mCircleRadius/3f;
        mArrowLength = mStrokeWidth * 1.2f;
        mLineLength = mCircleRadius * (float) Math.PI * 6.2f/8f;
        mLineSpaceHalf = (mCircleRadius * (float) Math.PI - mLineLength)/2;


        mPaint=new Paint();
        mPaint.setStyle(Paint.Style.STROKE);//设置画笔样式为描边，如果已经设置，可以忽略
//        mPaint.setColor(Color.GREEN);
        mPaint.setColor(Color.argb(255,200, 200, 200));
        mPaint.setStrokeCap(Paint.Cap.ROUND);//圆角笔触
//        mPaint.setStrokeWidth(10);
        mPaint.setStrokeWidth(mStrokeWidth);
//        updateCircleView(animatorDuration);
    }

//    public float getViewHeight(){
//        return Height;
//    }

    public void setStatusAndAnimatorDistance(int status, float animatorDistance){
        mRefreshStatus = status;
        mAnimatorDistance = animatorDistance;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(Width/2,Height/2);

        switch (mRefreshStatus){
            case RefreshStatus.STATUS_ORIGIN:
                break;
            case RefreshStatus.STATUS_DISTANCE_UNFINISHED:
                drawDistanceFinished(canvas);
                break;
            case RefreshStatus.STATUS_DISTANCE_UNFINISHED_BACK:
                drawDistanceFinished(canvas);
                break;
            case RefreshStatus.STATUS_DISTANCE_FINISHED:
                drawDistanceFinished(canvas);
                break;
            case RefreshStatus.STATUS_REFRESH_PREPARE:
                drawRefreshing(canvas);
                break;
            case RefreshStatus.STATUS_REFRESHING:
                drawRefreshing(canvas);
                break;
            case RefreshStatus.STATUS_REFRESH_FINISHED_CIRCLE:
                drawRefreshFinished(canvas);
                break;
//            case RefreshStatus.STATUS_REFRESH_FINISHED_HIDE:
//                drawRefreshFinished(canvas);
//                break;
            default:
                break;
        }
    }


    private void drawDistanceFinished(Canvas canvas){
        mAnimatorDistance = mAnimatorDistance - mLineSpaceHalf*2;
        if(mAnimatorDistance < Height/2 ){
            float leftStartX = -mCircleRadius;
            float leftStartY = Height/4 - mAnimatorDistance/2;
            float leftStopX = leftStartX;
            float leftStopY = leftStartY + mLineLength;
            canvas.drawLine(leftStartX, leftStartY, leftStopX, leftStopY, mPaint);
            float leftArrowX = leftStartX - mArrowLength * (float) Math.sin(Math.toRadians(30));
            float leftArrowY = leftStartY + mArrowLength * (float) Math.cos(Math.toRadians(30));
            canvas.drawLine(leftStartX, leftStartY, leftArrowX, leftArrowY, mPaint);


            float rightStartX = -leftStartX;
            float rightStartY = -leftStartY;
            float rightStopX = -leftStopX;
            float rightStopY = -leftStopY;
            canvas.drawLine(rightStartX,rightStartY,rightStopX,rightStopY,mPaint);
            float rightArrowX = -leftArrowX;
            float rightArrowY = -leftArrowY;
            canvas.drawLine(rightStartX, rightStartY, rightArrowX, rightArrowY, mPaint);
        }else{
            float circleValue = (mAnimatorDistance - Height/2)/2;

            float radius = mCircleRadius;
            RectF rectF = new RectF(-radius,-radius,radius,radius);
            float swipeAngle = (float) Math.toDegrees(circleValue/radius);
            float totalAngle = (float) Math.toDegrees(mLineLength/radius);

            float leftArcStartX = mCircleRadius * (float) Math.cos(Math.toRadians(180+swipeAngle));
            float leftArcStartY = mCircleRadius * (float) Math.sin(Math.toRadians(180+swipeAngle));
            float leftArcArrowX = leftArcStartX - mArrowLength * (float) Math.sin(Math.toRadians(swipeAngle+30));
            float leftArcArrowY = leftArcStartY + mArrowLength * (float) Math.cos(Math.toRadians(swipeAngle+30));
            canvas.drawLine(leftArcStartX, leftArcStartY, leftArcArrowX, leftArcArrowY, mPaint);

            float rightArcStartX = mCircleRadius * (float) Math.cos(Math.toRadians(swipeAngle));
            float rightArcStartY = mCircleRadius * (float) Math.sin(Math.toRadians(swipeAngle));
            float rightArcArrowX = rightArcStartX + mArrowLength * (float) Math.sin(Math.toRadians(swipeAngle+30));
            float rightArcArrowY = rightArcStartY - mArrowLength * (float) Math.cos(Math.toRadians(swipeAngle+30));
            canvas.drawLine(rightArcStartX, rightArcStartY, rightArcArrowX, rightArcArrowY, mPaint);

            if(circleValue < totalAngle/360 *(float) Math.PI * mCircleRadius*2 ){
                canvas.drawArc(rectF,180,swipeAngle,false,mPaint);
                canvas.drawArc(rectF,0,swipeAngle,false,mPaint);

                float leftStartX = -mCircleRadius;
                float leftStartY = 0;
                float leftStopX = leftStartX;
                float leftStopY = mLineLength - circleValue;
                canvas.drawLine(leftStartX, leftStartY, leftStopX, leftStopY, mPaint);

                float rightStartX = -leftStartX;
                float rightStartY = -leftStartY;
                float rightStopX = -leftStopX;
                float rightStopY = -leftStopY;
                canvas.drawLine(rightStartX, rightStartY, rightStopX, rightStopY, mPaint);
            }else {
                canvas.drawArc(rectF,swipeAngle-totalAngle,totalAngle,false,mPaint);
                canvas.drawArc(rectF,swipeAngle+180-totalAngle,totalAngle,false,mPaint);
            }
        }


    }

    private void drawRefreshing(Canvas canvas){

        mAnimatorDistance = mAnimatorDistance - mLineSpaceHalf*2;
        float circleValue = (mAnimatorDistance - Height/2)/2;

        float radius = mCircleRadius;
        RectF rectF = new RectF(-radius,-radius,radius,radius);
        float swipeAngle = (float) Math.toDegrees(circleValue/radius);
        float totalAngle = (float) Math.toDegrees(mLineLength/radius);

        float leftArcStartX = mCircleRadius * (float) Math.cos(Math.toRadians(180+swipeAngle));
        float leftArcStartY = mCircleRadius * (float) Math.sin(Math.toRadians(180+swipeAngle));
        float leftArcArrowX = leftArcStartX - mArrowLength * (float) Math.sin(Math.toRadians(swipeAngle+30));
        float leftArcArrowY = leftArcStartY + mArrowLength * (float) Math.cos(Math.toRadians(swipeAngle+30));
        canvas.drawLine(leftArcStartX, leftArcStartY, leftArcArrowX, leftArcArrowY, mPaint);

        float rightArcStartX = mCircleRadius * (float) Math.cos(Math.toRadians(swipeAngle));
        float rightArcStartY = mCircleRadius * (float) Math.sin(Math.toRadians(swipeAngle));
        float rightArcArrowX = rightArcStartX + mArrowLength * (float) Math.sin(Math.toRadians(swipeAngle+30));
        float rightArcArrowY = rightArcStartY - mArrowLength * (float) Math.cos(Math.toRadians(swipeAngle+30));
        canvas.drawLine(rightArcStartX, rightArcStartY, rightArcArrowX, rightArcArrowY, mPaint);


        canvas.drawArc(rectF,swipeAngle-totalAngle,totalAngle,false,mPaint);
        canvas.drawArc(rectF,swipeAngle+180-totalAngle,totalAngle,false,mPaint);
    }

    private void drawRefreshFinished(Canvas canvas){

        mAnimatorDistance = mAnimatorDistance - mLineSpaceHalf;
        if(mAnimatorDistance < (float) Math.PI * mCircleRadius){
            float radius = mCircleRadius;
            RectF rectF = new RectF(-radius,-radius,radius,radius);
            float swipeAngle = (float) Math.toDegrees(mAnimatorDistance/radius);
            float totalAngle = (float) Math.toDegrees(mLineLength/radius);

            float leftArcStartX = mCircleRadius * (float) Math.cos(Math.toRadians(180+swipeAngle));
            float leftArcStartY = mCircleRadius * (float) Math.sin(Math.toRadians(180+swipeAngle));
            float leftArcArrowX = leftArcStartX - mArrowLength * (float) Math.sin(Math.toRadians(swipeAngle+30));
            float leftArcArrowY = leftArcStartY + mArrowLength * (float) Math.cos(Math.toRadians(swipeAngle+30));
            canvas.drawLine(leftArcStartX, leftArcStartY, leftArcArrowX, leftArcArrowY, mPaint);

            float rightArcStartX = mCircleRadius * (float) Math.cos(Math.toRadians(swipeAngle));
            float rightArcStartY = mCircleRadius * (float) Math.sin(Math.toRadians(swipeAngle));
            float rightArcArrowX = rightArcStartX + mArrowLength * (float) Math.sin(Math.toRadians(swipeAngle+30));
            float rightArcArrowY = rightArcStartY - mArrowLength * (float) Math.cos(Math.toRadians(swipeAngle+30));
            canvas.drawLine(rightArcStartX, rightArcStartY, rightArcArrowX, rightArcArrowY, mPaint);

            canvas.drawArc(rectF,swipeAngle-totalAngle,totalAngle,false,mPaint);
            canvas.drawArc(rectF,swipeAngle+180-totalAngle,totalAngle,false,mPaint);
        }else if(mAnimatorDistance < (float) Math.PI * mCircleRadius + mLineLength){
            float nonCircleValue = mAnimatorDistance - (float) Math.PI * mCircleRadius;

            float leftStartX = -mCircleRadius;
            float leftStartY = -nonCircleValue;
            float leftStopX = leftStartX;
            float leftStopY = 0;
            canvas.drawLine(leftStartX, leftStartY, leftStopX, leftStopY, mPaint);
            float leftArcArrowX = leftStartX - mArrowLength * (float) Math.sin(Math.toRadians(30));
            float leftArcArrowY = leftStartY + mArrowLength * (float) Math.cos(Math.toRadians(30));
            canvas.drawLine(leftStartX, leftStartY, leftArcArrowX, leftArcArrowY, mPaint);

            float rightStartX = -leftStartX;
            float rightStartY = -leftStartY;
            float rightStopX = -leftStopX;
            float rightStopY = -leftStopY;
            canvas.drawLine(rightStartX, rightStartY, rightStopX, rightStopY, mPaint);
            float rightArcArrowX = rightStartX + mArrowLength * (float) Math.sin(Math.toRadians(30));
            float rightArcArrowY = rightStartY - mArrowLength * (float) Math.cos(Math.toRadians(30));
            canvas.drawLine(rightStartX, rightStartY, rightArcArrowX, rightArcArrowY, mPaint);


            float radius = mCircleRadius;
            RectF rectF = new RectF(-radius,-radius,radius,radius);
            float swipeAngle = (float) Math.toDegrees((mLineLength-nonCircleValue)/radius);
            float smallAngle = 180 - swipeAngle;

            canvas.drawArc(rectF,smallAngle,swipeAngle,false,mPaint);
            canvas.drawArc(rectF,smallAngle+180,swipeAngle,false,mPaint);

        }else{
            mAnimatorDistance = mAnimatorDistance - (float) Math.PI * mCircleRadius - mLineLength;
            float leftStopX = -mCircleRadius;
            float leftStopY = - mAnimatorDistance;
            float leftStartX = leftStopX;
            float leftStartY = leftStopY - mLineLength;
            canvas.drawLine(leftStartX, leftStartY, leftStopX, leftStopY, mPaint);
            float leftArrowX = leftStartX - mArrowLength * (float) Math.sin(Math.toRadians(30));
            float leftArrowY = leftStartY + mArrowLength * (float) Math.cos(Math.toRadians(30));
            canvas.drawLine(leftStartX, leftStartY, leftArrowX, leftArrowY, mPaint);

            float rightStartX = -leftStartX;
            float rightStartY = -leftStartY;
            float rightStopX = -leftStopX;
            float rightStopY = -leftStopY;
            canvas.drawLine(rightStartX,rightStartY,rightStopX,rightStopY,mPaint);
            float rightArrowX = -leftArrowX;
            float rightArrowY = -leftArrowY;
            canvas.drawLine(rightStartX, rightStartY, rightArrowX, rightArrowY, mPaint);
        }
    }


//    protected float dp2px(float dp){
//        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
//    }
}