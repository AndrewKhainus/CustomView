package com.radomar.customview.circleMenu2;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.radomar.customview.R;


public class SectorView extends View {

    public int mStartAngle;
    public int mEndAngle;

    private Paint mPaint;
    private RectF mOval;
    private float mSweepAngle;

    private int mRadius;
    private int mCenterX;
    private int mCenterY;

    private float mLeftX;
    private float mRightX;
    private float mY;

    private Drawable mIcon;
    private int mColor;
    private int mLineColor;


    public SectorView(Context context) {
        super(context);
        init();
    }

    public SectorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CircleMenu,
                0, 0
        );

        try {
            mIcon = a.getDrawable(R.styleable.CircleMenu_itemIcon);
        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mOval = new RectF();
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    public void setSweepAngle(float sweepAngle) {
        this.mSweepAngle = sweepAngle;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.rotate(90 + mSweepAngle / 2, mCenterX, mCenterY);
        mOval.set(mCenterX - mRadius, mCenterY - mRadius, mCenterX + mRadius, mCenterY + mRadius);

        float startAngle = 270 - mSweepAngle / 2;

//draw sectors
        mPaint.setColor(mColor);
        canvas.drawArc(mOval, startAngle, mSweepAngle, true, mPaint);

//draw delimiters
        mPaint.setColor(mLineColor);
        mPaint.setStrokeWidth(4f);
        canvas.drawLine(mCenterX, mCenterY, mLeftX, mY, mPaint);
        canvas.drawLine(mCenterX, mCenterY, mRightX, mY, mPaint);

//draw icons on sector
        int sizeImage = mRadius * 3 / 10;
        int positionLeft = mCenterX - sizeImage / 2;
        int positionTop = mCenterY - (mRadius * 2 / 3) - sizeImage / 2;
        mIcon.setBounds(positionLeft, positionTop, positionLeft + sizeImage, positionTop + sizeImage);
        mIcon.draw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mCenterX = getWidth() / 2;
        mCenterY = getHeight() / 2;
        mRadius = getWidth() / 2;

//calculate points positions on circle boarder
        mLeftX =  (float) (mCenterX + Math.cos((270 - mSweepAngle / 2)*Math.PI / 180F) * mRadius);
        mRightX = (float) (mCenterX + Math.cos((270 + mSweepAngle / 2)*Math.PI / 180F) * mRadius);
        mY =  (float) (mCenterY + Math.sin((270 - mSweepAngle / 2)*Math.PI / 180F) * mRadius);
    }

    public void rotateTo(float pieRotation) {
        setRotation(pieRotation);
    }

    public void setLineColor(int color) {
        this.mLineColor = color;
    }
}
