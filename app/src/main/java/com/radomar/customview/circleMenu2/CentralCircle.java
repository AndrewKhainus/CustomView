package com.radomar.customview.circleMenu2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Radomar on 14.10.2015
 */
public class CentralCircle extends View {

    private Paint mPaint;
    private int mItemSelectedColor;
    private int mBaseColor;

    public CentralCircle(Context context) {
        super(context);
        init();
    }

    public CentralCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int mCenterX = getWidth() / 2;
        int mCenterY = getHeight() / 2;
        int mRadius = getWidth() / 2;

        mPaint.setColor(mItemSelectedColor);
        mPaint.setAntiAlias(true);
        canvas.drawCircle(mCenterX, mCenterY, mRadius / 7, mPaint);

        mPaint.setColor(mBaseColor);
        mPaint.setAntiAlias(true);
        canvas.drawCircle(mCenterX, mCenterY, mRadius / 11, mPaint);
    }

    private void init() {
        mPaint = new Paint();
    }

    public void setItemSelectedColor(int itemSelectedColor) {
        mItemSelectedColor = itemSelectedColor;
    }

    public void setBaseColor(int baseColor) {
        mBaseColor = baseColor;
    }

}
