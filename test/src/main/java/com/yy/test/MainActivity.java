package com.yy.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.widget.common.GestureListener;
import com.widget.common.GestureLockView;
import com.widget.common.Point;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final GestureLockView glv = (GestureLockView) findViewById(R.id.g);
        glv.setResetHaltTime(2000);
        glv.setSelectedMinSize(4);
        glv.setGestureListener(new GestureListener() {
            @Override
            public boolean getPointList(List<Point> list) {
                if (list.size() > 5) {
                    glv.resetNormalState();
                    return true;
                }
                return false;
            }
        });
//        setContentView(new TestView(this));
    }
}
