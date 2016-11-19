package com.androidlongs.commonbluetoothapplication.commonbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by androidlongs on 16/11/20.
 * 站在顶峰，看世界
 * 落在谷底，思人生
 */

public class CommonBluetooth {

    private BluetoothAdapter mDefaultAdapter;

    private CommonBluetooth() {

    }

    private static class SingleBluetooth {
        private static CommonBluetooth sCommonBluetooth = new CommonBluetooth();
    }

    public static CommonBluetooth getInstance() {
        return SingleBluetooth.sCommonBluetooth;
    }

    public boolean initBlueFunction() {
        //获取 BluetoothAdapter
        mDefaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mDefaultAdapter == null) {
            //不支持蓝牙功能
            return false;
        }

        //蓝牙是否打开
        boolean enabled = mDefaultAdapter.isEnabled();

        if (enabled) {
            //蓝牙已打开
            //扫描设备
            startDiscovery();

        } else {
            //蓝牙未打开
            //打开蓝牙设备
            mDefaultAdapter.enable();
        }

        return true;
    }

    //开始扫描
    private void startDiscovery() {
        if (mDefaultAdapter != null) {
            //判断当前是否正在扫描
            boolean discovering = mDefaultAdapter.isDiscovering();
            if (!discovering) {
                //开启扫描
                mDefaultAdapter.startDiscovery();
            }
        }
    }

    //关闭扫描
    public boolean stopBluetoothDiscovery() {
        if (mDefaultAdapter != null) {
            boolean discovering = mDefaultAdapter.isDiscovering();
            if (discovering) {
                return mDefaultAdapter.cancelDiscovery();
            }

        }
        return true;
    }


    //注册蓝牙广播
    public void registBroadBluetooth(Activity activity) {

        IntentFilter intentFilter = new IntentFilter();
        //蓝牙状态改变
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //开始扫描设备
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //发现设备
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        //扫描完成
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        activity.registerReceiver(mBluetoothBroadcastReceiver, intentFilter);
    }

    //注销蓝牙广播
    public void unRegistBroadBluetooth(Activity activity) {
        activity.unregisterReceiver(mBluetoothBroadcastReceiver);
    }

    private BroadcastReceiver mBluetoothBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            //蓝牙状态改变
            if (TextUtils.equals(action, BluetoothAdapter.ACTION_STATE_CHANGED)) {

                //获取当前蓝牙的状态
                int currentStatue = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                //获取改变前的蓝牙的状态
                int preStatue = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, 0);

                switch (currentStatue) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                        //蓝牙将要打开
                        //接口回调
                        if (mStateOnLiserner != null) {
                            mStateOnLiserner.onBluetoothWillOpen();
                        }
                        break;
                    case BluetoothAdapter.STATE_ON:
                        //蓝牙打开
                        //接口回调
                        if (mStateOnLiserner != null) {
                            mStateOnLiserner.onBluetoothOpen();
                        }
                        //开启扫描
                        startDiscovery();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //蓝牙将要关闭,a
                        //接口回调
                        if (mStateOnLiserner != null) {
                            mStateOnLiserner.onBluetoothWillClose();
                        }
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        //蓝牙已关闭
                        //接口回调
                        if (mStateOnLiserner != null) {
                            mStateOnLiserner.onBluetoothClose();
                        }


                        break;

                }
                return;
            }


            //设备开始扫描
            if (TextUtils.equals(action, BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                Log.d("bluetooth ", "开始扫描蓝牙设备");
                if (mDeviceDiscoveryLiserner != null) {
                    mDeviceDiscoveryLiserner.onBluetoothStart();
                }
                return;
            }
            if (TextUtils.equals(action, BluetoothDevice.ACTION_FOUND)) {
                //发现设备
                //获取蓝牙对象
                BluetoothDevice devices = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDeviceDiscoveryLiserner != null) {
                    mDeviceDiscoveryLiserner.onBluetoothDiscover(devices);
                }
                return;
            }
            if (TextUtils.equals(action, BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                //扫描完成
                if (mDeviceDiscoveryLiserner != null) {
                    mDeviceDiscoveryLiserner.onBluetoothDiscoverFinish();
                }
                return;
            }

        }
    };


    //蓝牙状态的监听接口
    public interface OnBluetoothStateOnLiserner {
        //打开
        void onBluetoothOpen();

        //关闭
        void onBluetoothClose();

        //将要打开
        void onBluetoothWillOpen();

        //将要关闭
        void onBluetoothWillClose();
    }

    private OnBluetoothStateOnLiserner mStateOnLiserner;

    public void setStateOnLiserner(OnBluetoothStateOnLiserner stateOnLiserner) {
        mStateOnLiserner = stateOnLiserner;
    }

    //扫描到设备监听接口
    public interface OnBluetoothDeviceDiscoveryLiserner {
        //开始扫描
        void onBluetoothStart();

        //发现设备
        void onBluetoothDiscover(BluetoothDevice device);

        //扫描完成
        void onBluetoothDiscoverFinish();
    }

    private OnBluetoothDeviceDiscoveryLiserner mDeviceDiscoveryLiserner;

    public void setDeviceDiscoveryLiserner(OnBluetoothDeviceDiscoveryLiserner deviceDiscoveryLiserner) {
        mDeviceDiscoveryLiserner = deviceDiscoveryLiserner;
    }
}
