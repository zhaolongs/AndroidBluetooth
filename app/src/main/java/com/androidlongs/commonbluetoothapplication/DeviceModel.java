package com.androidlongs.commonbluetoothapplication;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by androidlongs on 16/11/15.
 * 站在顶峰，看世界
 * 落在谷底，思人生
 */

public class DeviceModel implements Parcelable {
    //蓝牙设备
    public BluetoothDevice mBluetoothDevice;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mBluetoothDevice, flags);
    }

    public DeviceModel() {
    }

    protected DeviceModel(Parcel in) {
        this.mBluetoothDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
    }

    public static final Parcelable.Creator<DeviceModel> CREATOR = new Parcelable.Creator<DeviceModel>() {
        @Override
        public DeviceModel createFromParcel(Parcel source) {
            return new DeviceModel(source);
        }

        @Override
        public DeviceModel[] newArray(int size) {
            return new DeviceModel[size];
        }
    };
}
