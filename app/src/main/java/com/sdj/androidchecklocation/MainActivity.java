package com.sdj.androidchecklocation;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.location.LocationManager.MODE_CHANGED_ACTION;
import static android.location.LocationManager.PROVIDERS_CHANGED_ACTION;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean hasAddTestProvider = false;
    private String[] locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private TextView mTextTv;
    private LocationManager mLocationManager;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        Log.i(TAG, "isOpen= " + isOpen() + Settings.Secure.getInt(getApplicationContext().getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, -1));
//        new RxPermissions(this).request(locationPermissions).subscribe(new Consumer<Boolean>() {
//            @Override
//            public void accept(Boolean aBoolean) throws Exception {
//                new Handler().postDelayed(new RunnableMockLocation(), 1000);
//            }
//        });
//        boolean enableAdb = (Settings.Secure.getInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0) > 0);
//        if (enableAdb) {
//            Toast.makeText(this, "enable", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, "not enable", Toast.LENGTH_SHORT).show();
//        }
//        Intent intent =  new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
//        startActivity(intent);
//        getInstallApkList();
//        getRunningList();
//        getRunningApp();
//        checkBackgroundAppList();
//        areThereMockPermissionApps(this);
        mTextTv = findViewById(R.id.text);
        mTextTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        WiFiBean wiFiBean = WiFiPunchHelper.getWiFiBean(MainActivity.this);
                        if (null != wiFiBean) {
                            mTextTv.setText("wifiName=" + wiFiBean.getWifiName() + "\nwifiMac=" + wiFiBean.getWifiMac());
                        }
                    }
                }, 100);
            }
        });
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        findViewById(R.id.gpsMonitorTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GpsMonitorActivity.class));
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationManager.MODE_CHANGED_ACTION);
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(mGpsStateReceiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GpsMonitor.getInstance().addListener(getLocalClassName(), onGpsMonitorChangedListener).registerGpsMonitor(this);
    }

    private BroadcastReceiver mGpsStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!TextUtils.isEmpty(intent.getAction())) {
                if (intent.getAction().equalsIgnoreCase(MODE_CHANGED_ACTION)
                        || intent.getAction().equalsIgnoreCase(PROVIDERS_CHANGED_ACTION)) {
                    Toast.makeText(MainActivity.this, "Gps state changed", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        GpsMonitor.getInstance().removeListener(getLocalClassName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGpsStateReceiver);
    }

    private GpsMonitor.onGpsMonitorChangedListener onGpsMonitorChangedListener = new GpsMonitor.onGpsMonitorChangedListener() {
        @Override
        public void onMonitorChanged(boolean enabled) {
            Log.i("MainActivity", enabled + "");
        }
    };

    private boolean isUsedMockLocation() {
        boolean isUsedMockLocation = false;

        return isUsedMockLocation;
    }

    public void getRunningApp() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = am
                .getRunningAppProcesses();
        ActivityManager.RunningAppProcessInfo processInfo = processes.get(0);
        String appPackageName = processInfo.processName;


        Log.e(TAG, appPackageName);
    }

    private void getInstallApkList() {
        PackageManager packageManager = getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfoList) {
            // 判断系统/非系统应用
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) // 非系统应用
            {
                System.out.println("MainActivity.getAppList, packageInfo=" + packageInfo.packageName);
            } else {
                // 系统应用
            }
        }
    }

    private void getRunningList() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = am.getRunningAppProcesses();
        am.getRunningTasks(10);
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
            if (null != appProcessInfo) {
                System.out.println("MainActivity.getAppList, appProcessInfo=" + appProcessInfo.processName);
            }
        }
    }

    private void checkBackgroundAppList() {
        List<ActivityManager.RunningAppProcessInfo> runningAppsInfo = new ArrayList<ActivityManager.RunningAppProcessInfo>();
        PackageManager pm = getPackageManager();
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        am.getRunningAppProcesses();
        List<ActivityManager.RunningServiceInfo> runningServices = am
                .getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : runningServices) {

            String pkgName = service.process.split(":")[0];
            try {
                ActivityManager.RunningAppProcessInfo item = new ActivityManager.RunningAppProcessInfo();
                item.pkgList = new String[]{pkgName};
                item.pid = service.pid;
                item.processName = service.process;
                item.uid = service.uid;

                runningAppsInfo.add(item);

            } catch (Exception e) {

            }
        }
    }

    public static boolean areThereMockPermissionApps(Context context) {
        int count = 0;
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages =
                pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName,
                        PackageManager.GET_PERMISSIONS);
                // Get Permissions
                String[] requestedPermissions = packageInfo.requestedPermissions;
                if (requestedPermissions != null) {
                    for (int i = 0; i < requestedPermissions.length; i++) {
                        if (requestedPermissions[i]
                                .equals("android.permission.ACCESS_MOCK_LOCATION")
                                && !applicationInfo.packageName.equals(context.getPackageName())) {
                            count++;
                            Log.i(TAG, "name=" + applicationInfo.packageName);
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("NameNotFoundException", e.getMessage());
            }
        }
        if (count > 0)
            return true;
        return false;
    }

    private boolean isOpen() {
        return Settings.Secure.getInt(getApplicationContext().getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, -1) == 1;
    }

    private boolean testProvider() {
        boolean canMockPosition = (Settings.Secure.getInt(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0)
                || Build.VERSION.SDK_INT > 22;
        if (canMockPosition && hasAddTestProvider == false) {
            try {
                String providerStr = LocationManager.GPS_PROVIDER;
                LocationProvider provider = mLocationManager.getProvider(providerStr);
                if (provider != null) {
                    mLocationManager.addTestProvider(
                            provider.getName()
                            , provider.requiresNetwork()
                            , provider.requiresSatellite()
                            , provider.requiresCell()
                            , provider.hasMonetaryCost()
                            , provider.supportsAltitude()
                            , provider.supportsSpeed()
                            , provider.supportsBearing()
                            , provider.getPowerRequirement()
                            , provider.getAccuracy());
                } else {
                    mLocationManager.addTestProvider(
                            providerStr
                            , true, true, false, false, true, true, true
                            , Criteria.POWER_HIGH, Criteria.ACCURACY_FINE);
                }
                mLocationManager.setTestProviderEnabled(providerStr, true);
                mLocationManager.setTestProviderStatus(providerStr, LocationProvider.AVAILABLE, null, System.currentTimeMillis());

                // 模拟位置可用
                hasAddTestProvider = true;
                canMockPosition = true;
            } catch (SecurityException e) {
                canMockPosition = false;
            }
        }
        return hasAddTestProvider;
    }

    private class RunnableMockLocation implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                    if (testProvider() == false) {
                        continue;
                    }

                    try {
                        // 模拟位置（addTestProvider成功的前提下）
                        String providerStr = LocationManager.GPS_PROVIDER;
                        Location mockLocation = new Location(providerStr);
                        mockLocation.setLatitude(22);   // 维度（度）
                        mockLocation.setLongitude(113);  // 经度（度）
                        mockLocation.setAltitude(30);    // 高程（米）
                        mockLocation.setBearing(180);   // 方向（度）
                        mockLocation.setSpeed(10);    //速度（米/秒）
                        mockLocation.setAccuracy(0.1f);   // 精度（米）
                        mockLocation.setTime(new Date().getTime());   // 本地时间
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                        }
                        mLocationManager.setTestProviderLocation(providerStr, mockLocation);
                    } catch (Exception e) {
                        // 防止用户在软件运行过程中关闭模拟位置或选择其他应用
                        stopMockLocation();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 停止模拟位置，以免启用模拟数据后无法还原使用系统位置
     * 若模拟位置未开启，则removeTestProvider将会抛出异常；
     * 若已addTestProvider后，关闭模拟位置，未removeTestProvider将导致系统GPS无数据更新；
     */
    public void stopMockLocation() {
        if (hasAddTestProvider) {
            try {
                mLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
                // 若未成功addTestProvider，或者系统模拟位置已关闭则必然会出错
            }
            hasAddTestProvider = false;
        }
    }
}
