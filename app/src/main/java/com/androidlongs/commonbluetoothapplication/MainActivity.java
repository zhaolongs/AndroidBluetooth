package com.androidlongs.commonbluetoothapplication;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {
    private BluetoothManager mBluetoothManager;


    // Used to load the 'native-lib' library on application startup.
//    static {
//        System.loadLibrary("native-lib");
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //检查设备是否支持BLE协议
        boolean isBleUsed = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        if (!isBleUsed) {
            Log.e("ble","设备不支持BLE协议");
            Toast.makeText(this,"设备不支持BLE协议",Toast.LENGTH_SHORT).show();
        }else {
            //初始化蓝牙适配器
            initBluetooth();
        }
    }
    private BluetoothAdapter mBluetoothAdapter;
    private void initBluetooth() {
        //获取 BluetoothManager
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        //获取 BluetoothAdapter
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        //判断是否支持
        if (mBluetoothAdapter == null) {
            Log.e("ble","设备不支持蓝牙");
            Toast.makeText(this,"设备不支持蓝牙",Toast.LENGTH_SHORT).show();
        }else {

        }
    }

    /**
     * 扫描BLE设备
     */
    private boolean isScanBleDevice = false;
    private void scanBleDevice(){
        if (!isScanBleDevice) {
            //开始扫描
            mBluetoothAdapter.startLeScan(mScanCallback);
        }else {
            //停止扫描
            mBluetoothAdapter.stopLeScan(mScanCallback);
        }

    }


    //扫描到BLE设备的回调
    private BluetoothAdapter.LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback(){

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

        }
    };
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
