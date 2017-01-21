package com.widget.common;

import java.util.List;

/**
 * Created by zzq on 2017/1/21.
 */
public interface GestureListener {
    /**
     * 获得手势经过的点顺序集合
     * @param list
     * @return true为正确，false为错误
     */
    boolean getPointList(List<Point> list);
}
