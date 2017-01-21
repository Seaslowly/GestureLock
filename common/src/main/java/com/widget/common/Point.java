package com.widget.common;

/**
 * Created by zzq on 2017/1/19.
 */
public class Point {
    private final float x;
    private final float y;
    public int state = STATE_Normal;
    public static int STATE_PRESSED = 0;
    public static int STATE_ERROR = 1;
    public static int STATE_Normal = 2;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}