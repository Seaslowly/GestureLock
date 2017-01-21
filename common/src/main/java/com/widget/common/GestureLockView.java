package com.widget.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zzq on 2017/1/19.
 */
public class GestureLockView extends View {
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
    private int mSelectedMinSize = 3;
    private boolean createPointsFinish = false;
    private List<Point> mPointList = new ArrayList();
    private List<Point> mSelectedPointList = new ArrayList();
    private Bitmap mPointPressedBitmap;
    private Bitmap mPointErrorBitmap;
    private Bitmap mPointNormalBitmap;
    private int mRPoint;
    private float moveX;
    private float moveY;
    private boolean startDraw = false;
    private boolean endDraw = false;
    private Point mCurrPoint;
    private Point mPrePoint;
    private Point mTmpPoint;
    private Bitmap mTmpBitmap;
    private long mUpTime = 666;//毫秒
    private int mResetTag = 0;
    private GestureListener mGestureListener;
    private Handler stateResetHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what != mResetTag) return;
            setNormalStatePointList();
            invalidate();
        }
    };

    public GestureLockView(Context context) {
        this(context, null);
    }

    public GestureLockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {//xml直接配置drawable资源
            TypedArray attrsTypedArray = context.obtainStyledAttributes(attrs, R.styleable.GestureLockView);
            if (attrsTypedArray != null) {
                int stateDrw;
                stateDrw = attrsTypedArray.getResourceId(R.styleable.GestureLockView_drawableNormal, -1);
                if (stateDrw != -1)
                    mPointNormalBitmap = getBitmap(stateDrw);
                stateDrw = attrsTypedArray.getResourceId(R.styleable.GestureLockView_drawableError, -1);
                if (stateDrw != -1)
                    mPointErrorBitmap = getBitmap(stateDrw);
                stateDrw = attrsTypedArray.getResourceId(R.styleable.GestureLockView_drawablePressed, -1);
                if (stateDrw != -1)
                    mPointPressedBitmap = getBitmap(stateDrw);
                attrsTypedArray.recycle();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!createPointsFinish) {//初始化点坐标
            loadRes();
            createPoints();
            createPointsFinish = true;
        }
        drawPoints(canvas);//画点
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        moveX = event.getX();
        moveY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mTmpPoint = checkTouchGesturePoint();
                if (mTmpPoint != null && !mSelectedPointList.contains(mTmpPoint)) {
                    mPrePoint = mCurrPoint;
                    mCurrPoint = mTmpPoint;//记录为当前
                    setPressedPointState(mCurrPoint);
                }
                break;
            case MotionEvent.ACTION_DOWN:
                mTmpPoint = checkTouchGesturePoint();
                if (mTmpPoint != null) {
                    mCurrPoint = mTmpPoint;//记录为当前
                    endDraw = false;
                    startDraw = true;
                    setPressedPointState(mCurrPoint);
                }
                break;
            case MotionEvent.ACTION_UP:
                endDraw = true;
                startDraw = false;
                break;
        }

        //一次完整的绘制结果处理
        if (endDraw) {
            //手势过于简单 或 getPointList返回结果处理状态
            if ((mSelectedMinSize > 0 && mSelectedPointList.size() <= mSelectedMinSize) ||
                    (mGestureListener != null && !mGestureListener.getPointList(mSelectedPointList)))
                setErrorStateSelectedPointList();
            mSelectedPointList.clear();//3.清空选中缓冲区点记录
        }
        invalidate();//UI线程可以直接使用
        return true;
    }

    /**
     * 设置手势监听
     *
     * @param listener
     */

    public void setGestureListener(GestureListener listener) {
        mGestureListener = listener;
    }

    /**
     * 设置错误状态资源id
     *
     * @param id
     */
    public void setErrorStateRes(int id) {
        mPointErrorBitmap = getBitmap(id);
    }

    /**
     * 设置错误状态Bitmap
     *
     * @param b
     */
    public void setErrorStateBitmap(Bitmap b) {
        mPointErrorBitmap = b;
    }

    /**
     * 设置正常状态资源id
     *
     * @param id
     */
    public void setNormalStateRes(int id) {
        mPointNormalBitmap = getBitmap(id);
    }

    /**
     * 设置正常状态Bitmap
     *
     * @param b
     */
    public void setNormalStateBitmap(Bitmap b) {
        mPointNormalBitmap = b;
    }

    /**
     * 设置按下状态资源id
     *
     * @param id
     */
    public void setPressedStateRes(int id) {
        mPointPressedBitmap = getBitmap(id);
    }

    /**
     * 设置按下状态Bitmap
     *
     * @param b
     */
    public void setPressedStateBitmap(Bitmap b) {
        mPointPressedBitmap = b;
    }

    /**
     * 设置手势密码下限点数
     *
     * @param size
     */
    public void setSelectedMinSize(int size) {
        mSelectedMinSize = size;
    }

    /**
     * 设置恢复默认状态的时间
     *
     * @param time
     */
    public void setResetHaltTime(long time) {
        mUpTime = time;
    }

    /**
     *
     */
    public void resetNormalState() {
        stateResetHandler.sendEmptyMessageDelayed(mResetTag, mUpTime);
    }

    /**
     * 设置选中状态
     *
     * @param p
     */
    private void setPressedPointState(Point p) {
        p.state = Point.STATE_PRESSED;
        mSelectedPointList.add(p);
    }

    /**
     * 设置错误状态
     */
    private void setErrorStateSelectedPointList() {
        for (Point p : mSelectedPointList) {
            p.state = Point.STATE_ERROR;
        }
        resetNormalState();
    }

    /**
     * 将9个点恢复初始状态
     */
    private void setNormalStatePointList() {
        for (Point p : mPointList)
            if (p.state != Point.STATE_Normal)
                p.state = Point.STATE_Normal;
    }

    /**
     * 检查触摸时是否在手势点范围内
     *
     * @return
     */
    private Point checkTouchGesturePoint() {
        for (Point tmp : mPointList) {
            if (!selectedRange(tmp.getX(), tmp.getY(), moveX, moveY, mRPoint)) continue;
            return tmp;
        }
        return null;
    }

    /**
     * 依据状态绘制点
     *
     * @param canvas
     */
    private void drawPoints(Canvas canvas) {
        if (mPointList == null || mPointList.size() < 1) return;
        for (Point tmp : mPointList) {
            if (tmp.state == Point.STATE_PRESSED)
                mTmpBitmap = mPointPressedBitmap;
            else if (tmp.state == Point.STATE_ERROR)
                mTmpBitmap = mPointErrorBitmap;
            else
                mTmpBitmap = mPointNormalBitmap;
            canvas.drawBitmap(mTmpBitmap, tmp.getX() - mRPoint, tmp.getY() - mRPoint, mPaint);
        }
    }

    /**
     * 加载图片资源
     */
    private void loadRes() {
        if (mPointPressedBitmap == null)
            mPointPressedBitmap = getBitmap(R.drawable.startpwd_gesture_logo_green);
        if (mPointErrorBitmap == null)
            mPointErrorBitmap = getBitmap(R.drawable.startpwd_gesture_logo_red);
        if (mPointNormalBitmap == null)
            mPointNormalBitmap = getBitmap(R.drawable.startpwd_gesture_logo);
        mRPoint = mPointNormalBitmap.getWidth() / 2;
    }

    /**
     * 初始化创建点
     */
    private void createPoints() {
        int w = getWidth();
        int h = getHeight();

        float space, offsetX = 0, offsetY = 0;
        if (w <= h) {//竖屏
            offsetX = space = w / 4;
            offsetY = (h - w) / 2 + space;
        } else {
            offsetY = space = h / 4;
            offsetX = (w - h) / 2 + space;
        }

        //创建点坐标
        mPointList.add(new Point(offsetX, offsetY));//[0][0]
        mPointList.add(new Point(offsetX + space, offsetY));//[0][1]
        mPointList.add(new Point(offsetX + space * 2, offsetY));//[0][2]

        mPointList.add(new Point(offsetX, offsetY + space));//[1][0]
        mPointList.add(new Point(offsetX + space, offsetY + space));//[1][1]
        mPointList.add(new Point(offsetX + space * 2, offsetY + space));//[1][2]

        mPointList.add(new Point(offsetX, offsetY + space * 2));//[2][0]
        mPointList.add(new Point(offsetX + space, offsetY + space * 2));//[2][1]
        mPointList.add(new Point(offsetX + space * 2, offsetY + space * 2));//[2][2]
    }

    /**
     * 获得两点距离
     *
     * @param _PointX
     * @param _PointY
     * @param __PointX
     * @param __PointY
     * @return
     */
    private double pointDistance(float _PointX, float _PointY, float __PointX, float __PointY) {
        float absX = Math.abs(_PointX - __PointX);
        float absY = Math.abs(_PointY - __PointY);
        return Math.sqrt(absX * absX + absY * absY);
    }

    /**
     * 判断点在圆内
     *
     * @param PointX
     * @param PointY
     * @param moveCurrX
     * @param moveCurrY
     * @param r
     * @return
     */
    private boolean selectedRange(float PointX, float PointY, float moveCurrX, float moveCurrY, float r) {
        return pointDistance(PointX, PointY, moveCurrX, moveCurrY) < r;//true在圆内
    }

    /**
     * 图片资源id获取bitmap
     *
     * @param id
     */
    private Bitmap getBitmap(int id) {
        return BitmapFactory.decodeResource(getResources(), id);
    }

    @Override
    protected void onDetachedFromWindow() {
        mGestureListener = null;
        mPaint = null;
        mPointList = null;
        mSelectedPointList = null;
        mPointPressedBitmap = null;
        mPointErrorBitmap = null;
        mPointNormalBitmap = null;
        mCurrPoint = null;
        mPrePoint = null;
        mTmpPoint = null;
        mTmpBitmap = null;
        stateResetHandler = null;
        super.onDetachedFromWindow();
    }
}