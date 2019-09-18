package com.sdj.androidchecklocation;

import java.io.Serializable;

public class WiFiBean implements Serializable {
    private String wifiName;
    private String wifiMac;

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public String getWifiMac() {
        return wifiMac;
    }

    public void setWifiMac(String wifiMac) {
        this.wifiMac = wifiMac;
    }
}
