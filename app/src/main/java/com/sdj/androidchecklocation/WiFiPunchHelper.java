package com.sdj.androidchecklocation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class WiFiPunchHelper {
    public static final String NONE = "<unknown ssid>";


    static boolean isNetworkConnected(Context context) {
        if (context == null) return false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    private static String getWifiMac(String mac) {
        if (!TextUtils.isEmpty(mac) && mac.contains(":")) {
            return mac.replaceAll(":", "-");
        }
        return "";
    }

    public static String getWifiMacString(String mac) {
        if (!TextUtils.isEmpty(mac) && mac.contains(":")) {
            return mac.replaceAll(":", "").toLowerCase();
        } else if (!TextUtils.isEmpty(mac) && mac.contains("-")) {
            return mac.replaceAll("-", "").toLowerCase();
        }
        return "";
    }


    public static String getWiFiMacAddress(WiFiBean wiFiBean) {
        if (null == wiFiBean) return "";
        return wiFiBean.getWifiMac();
    }

    private static String getWifiName(String wifiName) {
        if (!TextUtils.isEmpty(wifiName) && wifiName.contains("\"")) {
            return wifiName.replaceAll("\"", "");
        }
        return wifiName;
    }

    public static boolean isMobileNetwork(Context context) {
        if (context == null) return false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkINfo = cm.getActiveNetworkInfo();
            return networkINfo != null && networkINfo.getType() == ConnectivityManager.TYPE_MOBILE && networkINfo.isConnected();
        }
        return false;
    }

    public static boolean isWiFiNetwork(Context context) {
        if (context == null) return false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkINfo = cm.getActiveNetworkInfo();
            return networkINfo != null && networkINfo.getType() == ConnectivityManager.TYPE_WIFI && networkINfo.isConnected();
        }
        return false;
    }


    public static String getMac() {
        String macSerial = null;
        String str = "";

        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial;
    }

    private static String getMacAddress() {
        try {
            if (NetworkInterface.getNetworkInterfaces() == null) return "02:00:00:00:00:00";
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            Log.e("WiFiPunchHelper", Log.getStackTraceString(e));
        }
        return "02:00:00:00:00:00";
    }

    /**
     * 获取SSID
     *
     * @return WIFI 的SSID
     */
    public static String getWifiSSID(Activity activity) {
        String ssid = "unknown id";
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WifiManager mWifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            assert mWifiManager != null;
            WifiInfo info = mWifiManager.getConnectionInfo();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                return info.getSSID();
            } else {
                return info.getSSID().replace("\"", "");
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
            ConnectivityManager connManager = (ConnectivityManager) activity.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            assert connManager != null;
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            if (networkInfo.isConnected()) {
                if (networkInfo.getExtraInfo() != null) {
                    return networkInfo.getExtraInfo().replace("\"", "");
                }
            }
        }
        return ssid;
    }

    static WiFiBean getWiFiBean(Context context) {
        String macAddress = null;
        String wifiName = null;
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (null != wifiInfo) {
            macAddress = wifiInfo.getBSSID();
            wifiName = getWifiName(wifiInfo.getSSID());
        }
        if (TextUtils.isEmpty(wifiName) || wifiName.equals(NONE)) {
            NetworkInfo netWifiInfo = null;
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (null != connectivityManager) {
                netWifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            }
            if (null != netWifiInfo) {
                wifiName = netWifiInfo.getExtraInfo();
            }
        }
        if (TextUtils.isEmpty(macAddress) || macAddress.equals("02:00:00:00:00:00") || macAddress.equals("00:00:00:00:00:00")) {
            macAddress = getMacAddress();
        }
        WiFiBean wiFiBean = new WiFiBean();
        wiFiBean.setWifiMac(getWifiMac(macAddress));
        if (!TextUtils.isEmpty(wifiName)) {
            wiFiBean.setWifiName(getWifiName(wifiName));
        }
        return wiFiBean;
    }

    public static boolean isApplicationRunningInBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (null != am) {
            List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
            if (!tasks.isEmpty()) {
                ComponentName topActivity = tasks.get(0).topActivity;
                return !topActivity.getPackageName().equals(context.getPackageName());
            }
        }
        return false;
    }

    public static boolean isLocServiceEnable(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (null != manager) {
                return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean isWiFiEnable(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (null != wifiManager) {
            return wifiManager.isWifiEnabled();
        }
        return false;
    }
}
