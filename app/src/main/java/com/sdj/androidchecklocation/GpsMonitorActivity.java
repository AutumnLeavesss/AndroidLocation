package com.sdj.androidchecklocation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;


/**
 * author:shendongjie
 * date 2019/9/18 16:32
 * desc:
 */
public class GpsMonitorActivity extends AppCompatActivity {
    private TextView mGpsMonitorTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_monitor);
        mGpsMonitorTv = findViewById(R.id.gpsMonitorTv);
    }

    private GpsMonitor.onGpsMonitorChangedListener onGpsMonitorChangedListener = new GpsMonitor.onGpsMonitorChangedListener() {
        @Override
        public void onMonitorChanged(final boolean enabled) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGpsMonitorTv.setText("Gps is " + enabled);
                }
            });
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        GpsMonitor.getInstance().addListener(getLocalClassName(), onGpsMonitorChangedListener).registerGpsMonitor(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GpsMonitor.getInstance().removeListener(getLocalClassName());
    }
}
