package com.radomar.customview.circleMenu;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.radomar.customview.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Radomar on 06.10.2015
 */
public class TestViewGroup extends ViewGroup {

    private static final int FLING_VELOCITY_DOWNSCALE = 4;
    private static final int MOVE_UP_ANIM_DURATION = 400;

    private int mCenterX;
    private int mCenterY;
    private float mRadius;

    private int mBaseCircleColor;
    private int mItemNormalStateColor;
    private int mItemSelectedStateColor;

    private List<Item> mData = new ArrayList<>();

    private RectF mPieBounds = new RectF();

    private int mPieRotation;

    private ObjectAnimator mSectorMoveUpAnimator;

    private PieView mPieView;
    private Scroller mScroller;
    private ValueAnimator mScrollAnimator;
    private GestureDetector mDetector;

    public TestViewGroup(Context context) {
        super(context);
        init();
    }

    public TestViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CircleMenu,
                0, 0
        );

        try {
            mRadius = a.getDimension(R.styleable.CircleMenu_radius, 100f);
            mBaseCircleColor = a.getColor(R.styleable.CircleMenu_baseCircleColor, 0xff000000);
            mItemNormalStateColor = a.getColor(R.styleable.CircleMenu_itemNormalStateColor, 0xFF574153);
            mItemSelectedStateColor = a.getColor(R.styleable.CircleMenu_itemSelectedStateColor, 0xFF571853);
            Log.d("sometag", "item selected color ---- " + mItemSelectedStateColor);
        } finally {
            a.recycle();
        }

        init();
    }

    /**
     * Set the current rotation of the pie graphic. Setting this value may change
     * the current item.
     *
     * @param rotation The current pie rotation, in degrees.
     */
    public void setPieRotation(int rotation) {
        rotation = (rotation % 360 + 360) % 360;
        mPieRotation = rotation;
        mPieView.rotateTo(rotation);

    }

    public void initMenu(int[] icons) {
        Log.d("sometag", "initMenu");
        int startAngle = 0;
        int sweepAngle = 360/icons.length;

        for (int i = 0; i < icons.length; i++) {
            Item item = new Item();
            item.mColor = mItemNormalStateColor;
            item.mStartAngle = startAngle;
            item.mEndAngle = startAngle + sweepAngle;
            item.icon = icons[i];
            mData.add(item);
            startAngle += sweepAngle;

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Let the GestureDetector interpret this event
        mDetector.onTouchEvent(event);
//        return true if point situated in circle
        return inCircle(event.getX(), event.getY(), mCenterX, mCenterY, mRadius);
    }

    private boolean inCircle(float x, float y, float circleCenterX, float circleCenterY, float circleRadius) {
        double dx = Math.pow(x - circleCenterX, 2);
        double dy = Math.pow(y - circleCenterY, 2);

        if ((dx + dy) < Math.pow(circleRadius, 2)) {
            return true;
        }
        return false;
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = Math.max(minw, MeasureSpec.getSize(widthMeasureSpec));

        int minh = w + getPaddingBottom() + getPaddingTop();
        int h = Math.min(MeasureSpec.getSize(heightMeasureSpec), minh);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //
        // Set dimensions for text, pie chart, etc
        //
        // Account for padding
        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());

        float ww = (float) w - xpad;
        float hh = (float) h - ypad;

        // Figure out how big we can make the pie.
        float diameter = Math.min(ww, hh);
        mPieBounds = new RectF(
                0.0f,
                0.0f,
                diameter,
                diameter);
        mPieBounds.offsetTo(getPaddingLeft(), getPaddingTop());


        // Lay out the child view that actually draws the pie.
        mPieView.layout((int) mPieBounds.left,
                (int) mPieBounds.top,
                (int) mPieBounds.right,
                (int) mPieBounds.bottom);

    }


    /**
     * Initialize the control. This code is in a separate method so that it can be
     * called from both constructors.
     */
    private void init() {
        setLayerToSW(this);

        mPieView = new PieView(getContext());
        addView(mPieView);

        mScroller = new Scroller(getContext(), null, true);

        mScrollAnimator = ValueAnimator.ofFloat(0, 1);
        mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                tickScrollAnimation();
            }
        });

        // Create a gesture detector to handle onTouch messages
        mDetector = new GestureDetector(TestViewGroup.this.getContext(), new GestureListener());

        mSectorMoveUpAnimator = ObjectAnimator.ofInt(TestViewGroup.this, "PieRotation", 0);

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

    private void setLayerToSW(View v) {
        if (!v.isInEditMode() && Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    private void setLayerToHW(View v) {
        if (!v.isInEditMode() && Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    /**
     * Force a stop to all pie motion. Called when the user taps during a fling.
     */
    private void stopScrolling() {
        mScroller.forceFinished(true);
    }


    /**
     * Internal child class that draws the pie chart onto a separate hardware layer
     * when necessary.
     */
    private class PieView extends View {

        private float mSweepAngle;
        private float mX;
        private float mY;

        public PieView(Context context) {
            super(context);
        }

        /**
         * Enable hardware acceleration (consumes memory)
         */
        public void accelerate() {
            setLayerToHW(this);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Log.d("sometag", "onDraw");

            mSweepAngle = 360f / mData.size();

            mCenterX = getWidth()/2;
            mCenterY = getHeight()/2;

            drawBaseCircle(canvas);
            drawSectors(canvas);
            initPointCoordinates();
            drawDelimiters(canvas);
            drawCentralCircles(canvas);

        }

        private void drawBaseCircle(Canvas canvas) {
            Log.d("sometag", "drawBaseCircle");
            Paint paint = new Paint();
            paint.setColor(mBaseCircleColor);
            paint.setAntiAlias(true);
            canvas.drawCircle(mCenterX, mCenterY, mRadius * 1.05f, paint);
        }

        private void drawSectors(Canvas canvas) {
            Log.d("sometag", "drawSectors");
            float startAngle = -90 - mSweepAngle / 2;
            RectF mOval = new RectF();
            mOval.set(mCenterX - mRadius, mCenterY - mRadius, mCenterX + mRadius, mCenterY + mRadius);

            canvas.rotate(90 + mSweepAngle / 2, mCenterX, mCenterY);
            Log.d("sometag", "mData size = " + mData.size());
            for (int i = 0; i < mData.size(); i++) {
                Paint p2 = new Paint();
                p2.setColor(mData.get(i).mColor);
                p2.setAntiAlias(true);
                canvas.drawArc(mOval, startAngle, mSweepAngle, true, p2);

                //draw icons on sectors
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), mData.get(i).icon);
                canvas.drawBitmap(bitmap, mCenterX - bitmap.getWidth() / 2, mCenterY - (mRadius * 2 / 3) - bitmap.getHeight() / 2, p2);

                canvas.rotate(mSweepAngle, mCenterX, mCenterY);
            }
            Log.d("sometag", "end drawSectors");
        }

        private void initPointCoordinates() {
            mX =  (float) (mCenterX + Math.cos((270 - mSweepAngle / 2)*Math.PI / 180F) * mRadius);
            mY =  (float) (mCenterY + Math.sin((270 - mSweepAngle / 2)*Math.PI / 180F) * mRadius);
        }

        private void drawDelimiters(Canvas canvas) {
            Log.d("sometag", "draw delemiters");
            for (int i = 0; i < mData.size(); i++) {
                Paint paint = new Paint();
                paint.setColor(mBaseCircleColor);
                paint.setAntiAlias(true);
                paint.setStrokeWidth(4f);
                canvas.drawLine(mCenterX, mCenterY, mX, mY, paint);

                canvas.rotate(mSweepAngle, mCenterX, mCenterY);
            }
            Log.d("sometag", "end draw delimiters");
        }

        private void drawCentralCircles(Canvas canvas) {
            Paint paint = new Paint();
            paint.setColor(mItemSelectedStateColor);
            paint.setAntiAlias(true);
            canvas.drawCircle(mCenterX, mCenterY, mRadius / 7, paint);

            paint.setColor(mBaseCircleColor);
            paint.setAntiAlias(true);
            canvas.drawCircle(mCenterX, mCenterY, mRadius / 11, paint);
            refreshDrawableState();
        }

        public void rotateTo(float pieRotation) {
            setRotation(pieRotation);
        }

    }


    /**
     * Extends {@link GestureDetector.SimpleOnGestureListener} to provide custom gesture
     * processing.
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float scrollTheta = vectorToScalarScroll(
                    distanceX,
                    distanceY,
                    e2.getX() - mPieBounds.centerX(),
                    e2.getY() - mPieBounds.centerY());
            setPieRotation(mPieRotation - (int) scrollTheta / FLING_VELOCITY_DOWNSCALE);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Set up the Scroller for a fling
            float scrollTheta = vectorToScalarScroll(
                    velocityX,
                    velocityY,
                    e2.getX() - mPieBounds.centerX(),
                    e2.getY() - mPieBounds.centerY());
            mScroller.fling(
                    0,
                    (int) mPieRotation,
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
            mPieView.accelerate();
            stopScrolling();
            mSectorMoveUpAnimator.cancel();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            double touchAngle = getTouchAngle(e);

            for (Item item: mData) {
                item.mColor = mItemNormalStateColor;
                if (touchAngle > item.mStartAngle && touchAngle < item.mEndAngle) {
                    item.mColor = mItemSelectedStateColor;

                    moveItemUp(item);
                }
            }
            mPieView.invalidate();

            return super.onSingleTapUp(e);

        }
    }

    private double getTouchAngle(MotionEvent e) {
        int xPosition = (int)e.getX();
        int yPosition = (int)e.getY();

        int dx = mCenterX - xPosition;
        int dy = mCenterY - yPosition;

        double angle = Math.toDegrees(Math.atan2(dy, dx)) - 180;
        if (angle < 0) {
            angle += 360;
        }
        return (angle + 360 - mPieRotation) % 360;
    }

    private void moveItemUp(Item item) {
        int itemCenterAngle = ((item.mStartAngle + item.mEndAngle) / 2 ) + mPieRotation;
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

    /**
     * Helper method for translating (x,y) scroll vectors into scalar rotation of the pie.
     *
     * @param dx The x component of the current scroll vector.
     * @param dy The y component of the current scroll vector.
     * @param x  The x position of the current touch, relative to the pie center.
     * @param y  The y position of the current touch, relative to the pie center.
     * @return The scalar representing the change in angular position for this scroll.
     */
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
}
