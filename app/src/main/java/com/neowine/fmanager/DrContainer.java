package com.neowine.fmanager;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public class DrContainer {
    static public DrContainer con = null;
    private DrContainer()
    {

    }
    public static DrContainer getInstance()
    {
        if(con == null) {
            con = new DrContainer();
        }
        return con;
    }

    public void SetUSB(UsbManager mUsbManager,UsbDevice mDevice)
    {
        this.mUsbManager = mUsbManager;
        this.mDevice = mDevice;
    }

    public UsbManager mUsbManager;
    public UsbDevice mDevice;




}
