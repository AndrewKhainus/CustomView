package com.radomar.customview.circleMenu2;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.radomar.customview.R;

/**
 * Created by Radomar on 13.10.2015
 */
public class CircleMenuViewGroup extends ViewGroup {


    private static final int FLING_VELOCITY_DOWNSCALE = 4;
    private static final int MOVE_UP_ANIM_DURATION = 400;

    private Paint mPaint;

    private float mSweepAngle;
    private int mSectorsCount;
    private CentralCircle mCentralCircle;

    private int mCenterX;
    private int mCenterY;
    private int mRadius;

    private int mBaseCircleColor;
    private int mItemNormalStateColor;
    private int mItemSelectedStateColor;
    private int mStrokeWidth;

    private int mPieRotation;

    private Scroller mScroller;
    private GestureDetector mDetector;
    private ValueAnimator mScrollAnimator;
    private ObjectAnimator mSectorMoveUpAnimator;

    public CircleMenuViewGroup(Context context) {
        super(context);
        init();
    }

    public CircleMenuViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CircleMenu,
                0, 0
        );

        try {
            mStrokeWidth = a.getInt(R.styleable.CircleMenu_strokeWidth, 4);
            mBaseCircleColor = a.getColor(R.styleable.CircleMenu_baseCircleColor, 0xff000000);
            mItemSelectedStateColor = a.getColor(R.styleable.CircleMenu_itemSelectedStateColor, 0xFF571853);
            mItemNormalStateColor = a.getColor(R.styleable.CircleMenu_itemNormalStateColor, 0xFF574153);
        } finally {
            a.recycle();
        }
        init();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mSectorsCount = getChildCount();
        mSweepAngle = 360f / (mSectorsCount - 1);

        int startAngle = 0;
        int sizeArc = mRadius - mStrokeWidth;
        for (int i = 0; i < mSectorsCount - 1; i++) {
            final SectorView child = (SectorView) getChildAt(i);
            child.setSweepAngle(mSweepAngle);
            child.setColor(mItemNormalStateColor);
            child.setLineColor(mBaseCircleColor);

            child.mStartAngle = startAngle;
            child.mEndAngle = startAngle + (int) mSweepAngle;
            startAngle += mSweepAngle;

            child.layout(mCenterX - sizeArc, mCenterY - sizeArc, mCenterX + sizeArc, mCenterY + sizeArc);
            child.setRotation(mSweepAngle * i);
        }
//Draw central circle
        mCentralCircle.layout(mCenterX - mRadius, mCenterY - mRadius, mCenterX + mRadius, mCenterY + mRadius);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredWidth = 100;
        int desiredHeight = 100;

        int width;
        int height;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

//Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

//Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }
//SetMeasureDimension
        setMeasuredDimension(width, height);

//Calculate radius and center point position considering padding
        int xPad = getPaddingLeft() + getPaddingRight();
        int yPad = getPaddingTop() + getPaddingBottom();

        int w = width - xPad;
        int h = height - yPad;
        mRadius = Math.min(w, h) / 2;

        mCenterX = width / 2 + (getPaddingLeft() - getPaddingRight()) / 2;
        mCenterY = height / 2 + (getPaddingTop() - getPaddingBottom()) / 2;


    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCentralCircle = new CentralCircle(getContext());
        mCentralCircle.setItemSelectedColor(mItemSelectedStateColor);
        mCentralCircle.setBaseColor(mBaseCircleColor);
        addView(mCentralCircle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
//        return true if point situated in circle
        return inCircle(event.getX(), event.getY(), mCenterX, mCenterY, mRadius);
    }

    private boolean inCircle(float x, float y, float circleCenterX, float circleCenterY, float circleRadius) {
        double dx = Math.pow(x - circleCenterX, 2);
        double dy = Math.pow(y - circleCenterY, 2);

        return (dx + dy) < Math.pow(circleRadius, 2);
    }

    private void init() {
        setWillNotDraw(false);
        setLayerToHW();

        mPaint = new Paint();
        mPaint.setColor(mBaseCircleColor);
        mPaint.setAntiAlias(true);

        mScroller = new Scroller(getContext(), null, true);

        mScrollAnimator = ValueAnimator.ofFloat(0, 1);
        mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                tickScrollAnimation();
            }
        });

        mDetector = new GestureDetector(getContext(), new GestureListener());

        mSectorMoveUpAnimator = ObjectAnimator.ofInt(this, "PieRotation", 0);
    }

    private void setLayerToHW() {
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    public void setPieRotation(int rotation) {
        rotation = (rotation % 360 + 360) % 360;
        mPieRotation = rotation;

        for (int i = 0; i < mSectorsCount - 1; i++) {
            final SectorView child = (SectorView) getChildAt(i);
            child.rotateTo(rotation + mSweepAngle * i);
        }
    }

    private void tickScrollAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.computeScrollOffset();
            setPieRotation(mScroller.getCurrY());
        } else {
            if (Build.VERSION.SDK_INT >= 11) {
                mScrollAnimator.cancel();
            }
        }
    }

    private double getTouchAngle(MotionEvent e) {
        int xPosition = (int) e.getX();
        int yPosition = (int) e.getY();

        int dx = mCenterX - xPosition;
        int dy = mCenterY - yPosition;

        double angle = Math.toDegrees(Math.atan2(dy, dx)) - 180;
        if (angle < 0) {
            angle += 360;
        }
        return (angle + 360 - mPieRotation) % 360;
    }

    private void moveItemUp(SectorView sectorView) {
        int itemCenterAngle = ((sectorView.mStartAngle + sectorView.mEndAngle) / 2) + mPieRotation;
        itemCenterAngle %= 360;

        if (itemCenterAngle != 270) {
            int rotateAngle;
            if (itemCenterAngle > 270 || itemCenterAngle < 90) {
                rotateAngle = (270 - 360 - itemCenterAngle) % 360;
            } else {
                rotateAngle = (270 + 360 - itemCenterAngle) % 360;
            }

            mSectorMoveUpAnimator.setIntValues(mPieRotation, rotateAngle + mPieRotation);
            mSectorMoveUpAnimator.setDuration(MOVE_UP_ANIM_DURATION).start();
        }
    }

    private static float vectorToScalarScroll(float dx, float dy, float x, float y) {
        // get the length of the vector
        float l = (float) Math.sqrt(dx * dx + dy * dy);

        // decide if the scalar should be negative or positive by finding
        // the dot product of the vector perpendicular to (x,y).
        float crossX = -y;
        float crossY = x;

        float dot = (crossX * dx + crossY * dy);
        float sign = Math.signum(dot);

        return l * sign;
    }

    private void stopScrolling() {
        mScroller.forceFinished(true);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float scrollTheta = vectorToScalarScroll(
                    distanceX,
                    distanceY,
                    e2.getX() - mCenterX,
                    e2.getY() - mCenterY);
            setPieRotation(mPieRotation - (int) scrollTheta / FLING_VELOCITY_DOWNSCALE);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Set up the Scroller for a fling
            float scrollTheta = vectorToScalarScroll(
                    velocityX,
                    velocityY,
                    e2.getX() - mCenterX,
                    e2.getY() - mCenterY);
            mScroller.fling(
                    0,
                    mPieRotation,
                    0,
                    (int) scrollTheta / FLING_VELOCITY_DOWNSCALE,
                    0,
                    0,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE);

            // Start the animator and tell it to animate for the expected duration of the fling.
            mScrollAnimator.setDuration(mScroller.getDuration());
            mScrollAnimator.start();

            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            stopScrolling();
            mSectorMoveUpAnimator.cancel();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            double touchAngle = getTouchAngle(e);

            for (int i = 0; i < mSectorsCount - 1; i++) {
                final SectorView child = (SectorView) getChildAt(i);
                child.setColor(mItemNormalStateColor);
                if (touchAngle > child.mStartAngle && touchAngle < child.mEndAngle) {
                    child.setColor(mItemSelectedStateColor);

                    moveItemUp(child);
                }
                child.invalidate();
            }

            return super.onSingleTapUp(e);
        }
    }
}
