package com.kissinchat.clubmape;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

public class FinderActivity extends AppCompatActivity {
    private View bottle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_finder);

        bottle = findViewById(R.id.bottleView);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        final Handler h = new Handler();
        final int delay = 2000; //milliseconds

        h.postDelayed(new Runnable() {
            public void run() {
                spinBottle(bottle.getRotation() + 180);
                h.postDelayed(this, delay);
            }
        }, delay);
    }

    private void spinBottle(float to) {
        float from = bottle.getRotation();

        RotateAnimation rotate = new RotateAnimation(from, to, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(2000);
        rotate.setRepeatCount(0);
        rotate.setFillAfter(true);

        bottle.startAnimation(rotate);
    }
}
