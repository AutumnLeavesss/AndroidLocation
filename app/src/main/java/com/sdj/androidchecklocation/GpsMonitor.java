package com.sdj.androidchecklocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static android.content.Context.LOCATION_SERVICE;

/**
 * author:shendongjie
 * date 2019/9/18 16:01
 * desc:
 */
final class GpsMonitor {
    private LocationManager mLocationManager;

    private Set<Map<String, onGpsMonitorChangedListener>> mMonitorList = new HashSet<>();

    private GpsMonitor() {

    }

    static GpsMonitor getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        final static GpsMonitor INSTANCE = new GpsMonitor();
    }

    void registerGpsMonitor(Context context) {
        mLocationManager = (LocationManager) context.getApplicationContext().getSystemService(LOCATION_SERVICE);
        context.getApplicationContext().getContentResolver()
                .registerContentObserver(Settings.Secure.getUriFor(Settings.System.LOCATION_PROVIDERS_ALLOWED),
                        false, mGpsMonitor);
    }


    private final ContentObserver mGpsMonitor = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (mLocationManager != null) {
                boolean enabled = mLocationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);
                notifyChanged(enabled);
            }
        }
    };

    private void notifyChanged(boolean enabled) {
        for (Map<String, onGpsMonitorChangedListener> listener : mMonitorList) {
            Set<Map.Entry<String, onGpsMonitorChangedListener>> entry = listener.entrySet();
            for (Map.Entry<String, onGpsMonitorChangedListener> key : entry) {
                if (key.getValue() != null) {
                    key.getValue().onMonitorChanged(enabled);
                }
            }
        }
    }

    GpsMonitor addListener(String className, onGpsMonitorChangedListener listener) {
        Map<String, onGpsMonitorChangedListener> map = new HashMap<>();
        if (!TextUtils.isEmpty(className) && listener != null) {
            map.put(className, listener);
            mMonitorList.add(map);
        }
        return this;
    }

    //去除全局监听
    void unRegisterGpsMonitor(Context context) {
        context.getApplicationContext().getContentResolver().unregisterContentObserver(mGpsMonitor);
    }

    void removeListener(String className) {
        for (Map<String, onGpsMonitorChangedListener> changedListenerHashMap : mMonitorList) {
            Set<String> set = changedListenerHashMap.keySet();
            for (Iterator iterator = set.iterator(); iterator.hasNext(); ) {
                String key = (String) iterator.next();
                if (!TextUtils.isEmpty(key) && key.equalsIgnoreCase(className)) {
                    iterator.remove();
                }
            }
        }
    }

    public interface onGpsMonitorChangedListener {
        void onMonitorChanged(boolean enabled);
    }
}
