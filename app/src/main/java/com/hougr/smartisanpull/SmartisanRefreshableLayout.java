package com.hougr.smartisanpull;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by hougr on 16/8/25.
 */
public class SmartisanRefreshableLayout extends LinearLayout {
    private static final float ZERO_FOR_COMPARE=0.005f;

    //所有子View
    private RelativeLayout mHeaderLayout;
    private SmartisanCircleView mCircleView;
    private TextView mDescriptionTextView;
    private ListView mListView;

    //布局相关
    private boolean mIsLayoutLoaded;    //是否已加载过一次layout
    private int mHeaderHeight;
    private MarginLayoutParams mHeaderMargin;   //下拉头的布局参数
    private ValueAnimator mCircleAnimator;
    private ValueAnimator mPulledAnimator;

    //事件相关
    private int mCurrentStatus;
    private boolean mInterceptBoolean = false;      //当前事件是否拦截
    private float mLastMoveY;
    private float mPulledDistance;   //HeadView被下拉的距离，不是手指移动距离
    private float mAnimatorDistance;
    private float mRubRatio = 1.0f;     //摩擦系数
    private float mListDividerHeight;

    private PullToRefreshListener mListener;    //下拉刷新的回调接口


    public SmartisanRefreshableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mHeaderLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.refreshablelayout_header,null,true);
        mCircleView = (SmartisanCircleView) mHeaderLayout.findViewById(R.id.smartisanView);
        mDescriptionTextView = (TextView) mHeaderLayout.findViewById(R.id.descriptionTextView);
        setOrientation(VERTICAL);
        addView(mHeaderLayout, 0);

        mPulledDistance=0;
        mAnimatorDistance=0;
    }


    public void setOnRefreshListener(PullToRefreshListener listener) {
        mListener = listener;
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && !mIsLayoutLoaded) {
            mHeaderMargin = (MarginLayoutParams) mHeaderLayout.getLayoutParams();
            mHeaderHeight = mHeaderLayout.getHeight();
            mHeaderMargin.topMargin = -mHeaderHeight/2;
            mHeaderMargin.bottomMargin = mHeaderMargin.topMargin;
            mHeaderLayout.setLayoutParams(mHeaderMargin);
            mListView = (ListView) getChildAt(1);       //这句需要改进

            mListDividerHeight = mListView.getDividerHeight();
            this.setBackgroundColor(Color.argb(255,255,255,255));       //把背景统一设置为白色

            mCurrentStatus = RefreshStatus.STATUS_ORIGIN;
            updateLayoutAndText();

            mIsLayoutLoaded = true;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mInterceptBoolean = false;      //避免不下拉，只是往下翻列表。因为此时不知道手势往上还是往下。
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                View firstChildView = mListView.getChildAt(0);  //用于判断ListView是否没有元素
                if(firstChildView == null || (firstChildView != null && mListView.getFirstVisiblePosition() == 0 && mListView.getChildAt(0).getTop() == 0)){  //万事俱备，只欠下拉。
                    float currentRawY = event.getRawY();
                    float tmpMoveY = currentRawY - mLastMoveY;
                    if((isEqualZero(mPulledDistance) && tmpMoveY >0) || (mPulledDistance > 0 && mCurrentStatus != RefreshStatus.STATUS_REFRESHING)) {
                        //两种情况：第一次下拉、已经下拉过了。
                        // 如果首个元素的上边缘，距离父布局值为0，就说明ListView滚动到了最顶部，此时应该允许下拉刷新

                        Log.d("是否拦截",":可下拉");
                        if(!mInterceptBoolean){
                            mLastMoveY = event.getRawY();
                        }
                        mInterceptBoolean = true;

//                        // 当前正处于下拉或释放状态，要让ListView失去焦点，否则被点击的那一项会一直处于选中状态
//                        mListView.setPressed(false);
//                        mListView.setFocusable(false);
//                        mListView.setFocusableInTouchMode(false);
//                        // 当前正处于下拉或释放状态，通过返回true屏蔽掉ListView的滚动事件
                    }else if(mCurrentStatus == RefreshStatus.STATUS_REFRESHING && tmpMoveY > 0){
                        mInterceptBoolean =false;
                    }else if(isEqualZero(mPulledDistance) && tmpMoveY < 0){

                    } else{
                        mInterceptBoolean = false;
                    }
                } else {
                    mInterceptBoolean = false;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                mInterceptBoolean = false;
                break;
            }
            default:
                break;
        }

        mLastMoveY = y;
        Log.d("是否拦截",":"+mInterceptBoolean);

        return mInterceptBoolean;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                float currentRawY = event.getRawY();
                float tmpMoveY = currentRawY - mLastMoveY;

                if(mPulledDistance < 0 || isEqualZero(mPulledDistance)){
                    mPulledDistance += tmpMoveY;
                }else {
                    if(mPulledDistance <= mHeaderHeight){
                        mCurrentStatus = RefreshStatus.STATUS_DISTANCE_UNFINISHED;
                        mRubRatio = 1.0f;
                    }else {
                        mCurrentStatus = RefreshStatus.STATUS_DISTANCE_FINISHED;
                        mRubRatio = (2-mPulledDistance/(float) mHeaderHeight)/4;
                    }
                    mPulledDistance += mRubRatio * tmpMoveY;
                    mAnimatorDistance = mPulledDistance;
                    updateAllView(mCurrentStatus,mPulledDistance,mAnimatorDistance);
                    mLastMoveY = currentRawY;
                }
                break;
            case MotionEvent.ACTION_UP:
            default:
                Log.d("事件","Action_up");
                if (mCurrentStatus == RefreshStatus.STATUS_DISTANCE_FINISHED) {
                    mCurrentStatus = RefreshStatus.STATUS_REFRESH_PREPARE;
                    updateAllView(mCurrentStatus, mPulledDistance,mAnimatorDistance);
                    new RefreshingTask().execute();
                } else if (mCurrentStatus == RefreshStatus.STATUS_DISTANCE_UNFINISHED) {
                    mCurrentStatus = RefreshStatus.STATUS_DISTANCE_UNFINISHED_BACK;
                    updateAllView(mCurrentStatus, mPulledDistance,mAnimatorDistance);
//                        new HideHeaderTask().execute();
                }
                break;
        }
        return true;
    }

    private void updateLayoutAndText(){
        float mockPulledDistance;
        if(mPulledDistance <= mListDividerHeight){
            mListView.scrollTo(0,(int)mPulledDistance);
            mockPulledDistance =0;
        }else{
            mListView.scrollTo(0,-mListView.getDividerHeight());
            mockPulledDistance = mPulledDistance-mListDividerHeight;
        }


        mHeaderMargin.topMargin = (int)(-mHeaderHeight/2 + mockPulledDistance/2);
        mHeaderMargin.bottomMargin = mHeaderMargin.topMargin;
        mHeaderLayout.setLayoutParams(mHeaderMargin);


        switch (mCurrentStatus){

            case RefreshStatus.STATUS_ORIGIN:
                mDescriptionTextView.setTextColor(Color.argb(0,120,120,120));
                break;
            case RefreshStatus.STATUS_DISTANCE_UNFINISHED:
            case RefreshStatus.STATUS_DISTANCE_UNFINISHED_BACK:
                mDescriptionTextView.setText("下拉即可刷新...");
                if(mockPulledDistance < mHeaderHeight/4){
                    mDescriptionTextView.setTextColor(Color.argb(0,120,120,120));
                }else {
                    mDescriptionTextView.setTextColor(Color.argb((int)((mockPulledDistance- (float) mHeaderHeight/4)/(mHeaderHeight*3/4)*255),120,120,120));
                }
                break;
            case RefreshStatus.STATUS_DISTANCE_FINISHED:
                mDescriptionTextView.setText("松开即可刷新...");
                mDescriptionTextView.setTextColor(Color.argb(255,120,120,120));
                break;
            case RefreshStatus.STATUS_REFRESH_PREPARE:
            case RefreshStatus.STATUS_REFRESHING:
                mDescriptionTextView.setText("正在刷新列表...");
                mDescriptionTextView.setTextColor(Color.argb(255,120,120,120));
                break;
            case RefreshStatus.STATUS_REFRESH_FINISHED_CIRCLE:
//            case RefreshStatus.STATUS_REFRESH_FINISHED_HIDE:
                mDescriptionTextView.setText("正在刷新列表...");
                if(mockPulledDistance < mHeaderHeight/4){
                    mDescriptionTextView.setTextColor(Color.argb(0,120,120,120));
                }else {
                    mDescriptionTextView.setTextColor(Color.argb((int)((mockPulledDistance- (float) mHeaderHeight/4)/(mHeaderHeight*3/4)*255),120,120,120));
                }
                break;
            default:
                break;
        }
    }

    public void updateAllView(int refreshStatus, float pulledDistance , final float animatorDistance){
        Log.d("更新视图",refreshStatus+","+pulledDistance);
        mCurrentStatus = refreshStatus;
        mPulledDistance = pulledDistance;
        mAnimatorDistance = animatorDistance;
        mCircleView.setStatusAndAnimatorDistance(refreshStatus, animatorDistance);

        updateLayoutAndText();

        switch (mCurrentStatus){
            case RefreshStatus.STATUS_ORIGIN:
                break;
            case RefreshStatus.STATUS_DISTANCE_UNFINISHED:
                mCircleView.setStatusAndAnimatorDistance(RefreshStatus.STATUS_DISTANCE_UNFINISHED, mAnimatorDistance);
                mCircleView.invalidate();
                break;
            case RefreshStatus.STATUS_DISTANCE_UNFINISHED_BACK:
                resetCircleAnimator(mAnimatorDistance, 0, 300, 0, new UpdateHeaderViewCallback() {
                    @Override
                    public void onAnimationUpdate(float animatorValue) {
                        Log.d("属性动画过程值：","是"+ animatorValue);
                        mPulledDistance = animatorValue;
                        mAnimatorDistance = animatorValue;
                        mCircleView.setStatusAndAnimatorDistance(RefreshStatus.STATUS_DISTANCE_UNFINISHED_BACK, mAnimatorDistance);
                        mCircleView.invalidate();
                        updateLayoutAndText();
                    }

                    @Override
                    public void onAnimationEnd() {
                        mCurrentStatus = RefreshStatus.STATUS_ORIGIN;
                        mPulledDistance =0;
                        mAnimatorDistance =mPulledDistance;
                        mCircleView.setStatusAndAnimatorDistance(RefreshStatus.STATUS_ORIGIN, 0);
                    }
                });
                break;
            case RefreshStatus.STATUS_DISTANCE_FINISHED:
                mCircleView.invalidate();
                break;
            case RefreshStatus.STATUS_REFRESH_PREPARE:
                resetPullAnimator(mPulledDistance, mHeaderHeight, 200, 0, new UpdateHeaderViewCallback() {
                    @Override
                    public void onAnimationUpdate(float animatorValue) {
                        mPulledDistance =animatorValue;
                        updateLayoutAndText();

                    }

                    @Override
                    public void onAnimationEnd() {
                        mCurrentStatus = RefreshStatus.STATUS_REFRESHING;
                        updateAllView(RefreshStatus.STATUS_REFRESHING, mCircleView.getHeight(),mAnimatorDistance);
                    }
                });
                resetCircleAnimator(mPulledDistance, mPulledDistance+ (float) (Math.PI * mCircleView.mCircleRadius*2), 350, -1, new UpdateHeaderViewCallback() {
                    @Override
                    public void onAnimationUpdate(float animatorValue) {
                        mAnimatorDistance =animatorValue;
                        mCircleView.setStatusAndAnimatorDistance(RefreshStatus.STATUS_REFRESHING, mAnimatorDistance);
                        mCircleView.invalidate();
                    }

                    @Override
                    public void onAnimationEnd() {
                    }
                });
                break;
            case RefreshStatus.STATUS_REFRESHING:
                break;
            case RefreshStatus.STATUS_REFRESH_FINISHED_CIRCLE:
                float doubleCircleCount = (mAnimatorDistance - mHeaderHeight/2)/((float) Math.PI * mCircleView.mCircleRadius * 2);
                float circleCount = doubleCircleCount/2;
                float smallCircle = circleCount % 1;
                Log.d("刷新后","smallCircle:"+smallCircle);
                if(smallCircle >0.5){
                    smallCircle -=0.5;
                }
                float nonCircleCount = 0.5f - smallCircle;
                float toDoCircleDistance = nonCircleCount * ((float) Math.PI * mCircleView.mCircleRadius * 2);
                final float finalTotalDistance = (float) Math.PI * mCircleView.mCircleRadius +  mCircleView.mLineLength+mHeaderHeight/4;
                final float toDoTotalDistance = toDoCircleDistance + mCircleView.mLineLength+mHeaderHeight/4;
                final float bili = mHeaderHeight / toDoTotalDistance;
                final float doneDistance = finalTotalDistance - toDoTotalDistance;
                mAnimatorDistance = doneDistance;

                resetCircleAnimator(mAnimatorDistance, finalTotalDistance, 200, 0, new UpdateHeaderViewCallback() {
                    @Override
                    public void onAnimationUpdate(float animatorValue) {
                        mAnimatorDistance=animatorValue;
                        mPulledDistance = (finalTotalDistance - animatorValue) * bili;
                        mCircleView.setStatusAndAnimatorDistance(RefreshStatus.STATUS_REFRESH_FINISHED_CIRCLE, mAnimatorDistance);
                        mCircleView.invalidate();
                        updateLayoutAndText();
                    }

                    @Override
                    public void onAnimationEnd() {
                        mCurrentStatus = RefreshStatus.STATUS_ORIGIN;
                        mPulledDistance =0;
                        mAnimatorDistance =mPulledDistance;
                        mCircleView.setStatusAndAnimatorDistance(RefreshStatus.STATUS_ORIGIN, 0);
                    }
                });
                break;
            default:
                break;
        }

    }

    private void resetCircleAnimator(float startValue, float endValue, int duration , int repeatCount, final UpdateHeaderViewCallback updateHeaderViewCallback){
        if (mCircleAnimator !=null && mCircleAnimator.isRunning()){
            mCircleAnimator.cancel();
        }
        mCircleAnimator = ValueAnimator.ofFloat(startValue, endValue).setDuration(duration);
        mCircleAnimator.setInterpolator(new LinearInterpolator());
        mCircleAnimator.setRepeatCount(repeatCount);
        mCircleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                updateHeaderViewCallback.onAnimationUpdate(animatedValue);
            }
        });
        mCircleAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                updateHeaderViewCallback.onAnimationEnd();
                animation.cancel();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mCircleAnimator.start();
    }

    private void resetPullAnimator(float startValue, float endValue, int duration , int repeatCount, final UpdateHeaderViewCallback updateHeaderViewCallback){
        if (mPulledAnimator !=null && mPulledAnimator.isRunning()){
            mPulledAnimator.cancel();
        }
        mPulledAnimator = ValueAnimator.ofFloat(startValue, endValue).setDuration(duration);
        mPulledAnimator.setInterpolator(new LinearInterpolator());
        mPulledAnimator.setRepeatCount(repeatCount);
        mPulledAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                updateHeaderViewCallback.onAnimationUpdate(animatedValue);
            }
        });
        mPulledAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                updateHeaderViewCallback.onAnimationEnd();
                animation.cancel();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mPulledAnimator.start();
    }

    public void finishRefreshing(){
        mCurrentStatus = RefreshStatus.STATUS_REFRESH_FINISHED_CIRCLE;
        updateAllView(mCurrentStatus, mHeaderHeight, mAnimatorDistance);
    }

    private static interface UpdateHeaderViewCallback{
        public void onAnimationUpdate(float animatorValue);
        public void onAnimationEnd();
    }


    public interface PullToRefreshListener {
        public void onRefresh();
        public void onRefreshFinished();

    }

    class RefreshingTask extends AsyncTask<Void, Float, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (mListener != null) {
                mListener.onRefresh();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Float... topMargin) {
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mListener != null) {
                mListener.onRefreshFinished();
            }
        }
    }

    private boolean isEqualZero(float floatValue){
        if(floatValue < ZERO_FOR_COMPARE && floatValue > -ZERO_FOR_COMPARE){
            return true;
        }else {
            return false;
        }
    }
}
