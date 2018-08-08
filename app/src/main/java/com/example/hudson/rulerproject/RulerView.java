package com.example.hudson.rulerproject;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

public class RulerView extends View {

    private static final String TAG = "RulerView";

    private float maxValue;
    private float minValue;
    private float mOffset;
    private float maxOffset;
    private float mSelectorValue;
    private float lineMinHeight;
    private float lineMidHeight;
    private float lineMaxHeight;

    private int lineWidth = 4;
    private float lineSpaceWidth = 15.0f;
    private float mXLastPosition;
    private float mMove;

    private int mWidth;
    private int mHeight;
    private int mTotalLine;

    private Paint linePaint;
    private Paint textPaint;
    private Paint midPaint;

    private Scroller mScroller;
    private VelocityTracker mTracker;
    private int minSpeed;

    private OnValueChangeListener onValueChangeListener;

    public RulerView(Context context) {
        super(context);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RulerView);
        maxValue = typedArray.getFloat(R.styleable.RulerView_maxValue, 200.0f);
        minValue = typedArray.getFloat(R.styleable.RulerView_minValue, 0.0f);
        mSelectorValue = typedArray.getFloat(R.styleable.RulerView_selectorValue, 100.0f);
        lineMinHeight = typedArray.getDimension(R.styleable.RulerView_lineMinHeight, 0);
        lineMidHeight = typedArray.getDimension(R.styleable.RulerView_lineMidHeight, 0);
        lineMaxHeight = typedArray.getDimension(R.styleable.RulerView_lineMaxHeight, 0);
        typedArray.recycle();
        Log.i(TAG, "lineMinHeight:" + lineMinHeight + ";lineMidHeight:" + lineMidHeight + ";lineMaxHeight:" + lineMaxHeight);
        mTotalLine = (int) (maxValue - minValue);
        mOffset = (minValue - mSelectorValue) * lineSpaceWidth;
        maxOffset = -(mTotalLine * lineSpaceWidth);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(lineWidth);
        linePaint.setColor(Color.GRAY);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30);

        midPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        midPaint.setStrokeWidth(lineWidth);
        midPaint.setColor(Color.RED);

        mScroller = new Scroller(context);
        minSpeed = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();

    }

    private void moveRuler() {
        mOffset -= mMove;
        mXLastPosition = 0;
        mMove = 0;
        if (mOffset >= 0) {
            mOffset = 0;
            mScroller.forceFinished(true);
        }
        if (mOffset <= maxOffset) {
            mOffset = maxOffset;
            mScroller.forceFinished(true);
        }
//        Log.i(TAG, "mOffset:" + mOffset + ";maxOffset:" + maxOffset);
        float value = minValue + Math.round(Math.abs(mOffset) / lineSpaceWidth);
        onValueChange(value);
        postInvalidate();
    }

    private void moveRulerEnd() {
        mOffset -= mMove;
        mMove = 0;
        mXLastPosition = 0;
        if (mOffset >= 0) {
            mOffset = 0;
        }
        if (mOffset <= maxOffset) {
            mOffset = maxOffset;
        }

        float value = minValue + Math.round(Math.abs(mOffset) / lineSpaceWidth);
        mOffset = (minValue - value) * lineSpaceWidth;
        onValueChange(value);
        postInvalidate();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mWidth = w;
        this.mHeight = h;
        mMove = 0;
        if (mOffset >= 0) {
            mOffset = 0;
        }
        if (mOffset <= maxOffset) {
            mOffset = maxOffset;
        }
        postInvalidate();
    }

    public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
        this.onValueChangeListener = onValueChangeListener;
    }

    private void onValueChange(float val) {
        Log.i(TAG, "val=====" + val);
        if (onValueChangeListener != null) {
            onValueChangeListener.onChange(val);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float height;
        float left;
        for (int i = 0; i <= mTotalLine; i++) {
            left = mWidth / 2 + mOffset + i * lineSpaceWidth;
            if (i % 10 == 0) {
                height = lineMaxHeight;
            } else if (i % 5 == 0) {
                height = lineMidHeight;
            } else {
                height = lineMinHeight;
            }
            canvas.drawLine(left, 0, left, height, linePaint);
            if (i % 10 == 0) {
                String value = String.valueOf(minValue + i);
                canvas.drawText(value, left - textPaint.measureText(value) / 2, height + 30, textPaint);
            }
        }
        canvas.drawLine(mWidth / 2, 0, mWidth / 2, 200, midPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float xPosition = event.getX();
        if (mTracker == null) {
            mTracker = VelocityTracker.obtain();
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mScroller.forceFinished(true);
                mXLastPosition = xPosition;
                mMove = 0;

                break;
            case MotionEvent.ACTION_MOVE:
                mMove = mXLastPosition - event.getX();
                Log.i(TAG, "mMove:" + mMove + ";mXLastPosition:" + mXLastPosition + ";nowPosition:" + event.getX());
                moveRuler();
                mTracker.computeCurrentVelocity(1000);
                mTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "XVelocity:" + mTracker.getXVelocity());
                moveRulerEnd();
                float mXVelocity = mTracker.getXVelocity();
                if (Math.abs(mXVelocity) > minSpeed) {
                    mScroller.fling(0, 0, (int) mXVelocity, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
                }
                return false;
            default:
                break;
        }
        mXLastPosition = event.getX();
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        Log.i(TAG, "" + mScroller.getCurrX());
        if (mScroller.computeScrollOffset()) { //滚动中
            if (mScroller.getCurrX() == mScroller.getFinalX()) {
                moveRulerEnd();
            } else {
                Log.i(TAG, "mXLastPosition=====" + mXLastPosition);
                int xPosition = mScroller.getCurrX();
                mMove = mXLastPosition - xPosition;
                moveRuler();
                mXLastPosition = xPosition;
            }
        }
        mMove = 0;
    }
}
