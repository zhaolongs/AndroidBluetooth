package com.androidlongs.commonbluetoothapplication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends Activity {


    private Handler mHandler;
    private RecyclerView mRecyclerView;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isScanBleDevice) {
            //停止扫描
            mBluetoothAdapter.stopLeScan(mScanCallback);
            //更新标识
            isScanBleDevice = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);


        mHandler = new Handler();


        initViewFunction();


        //蓝牙相关操作-——————————————————————————————————————————————————————————————————————————————————-——
        //检查设备是否支持BLE协议
        boolean isBleUsed = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        if (!isBleUsed) {
            Log.e("ble", "设备不支持BLE协议");
            Toast.makeText(this, "设备不支持BLE协议", Toast.LENGTH_SHORT).show();
        } else {
            //初始化蓝牙适配器
            initBluetooth();
        }


    }

    //初始化控件相关操作
    private void initViewFunction() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_home_content);


        setRecyclerData();

    }

    private MainRecyclerAdapter mMainRecyclerAdapter;

    private List<DeviceModel> mDeviceModelList = new ArrayList<>();

    private void setRecyclerData() {

        //先清除list中数据
        mDeviceModelList.clear();
        //再将map中的数据 写入list中
        for (Map.Entry<String, DeviceModel> stringDeviceModelEntry : mModelDeviceMap.entrySet()) {
            mDeviceModelList.add(stringDeviceModelEntry.getValue());
        }

        //更新操作

        if (mMainRecyclerAdapter == null) {
            //创建适配器
            mMainRecyclerAdapter = new MainRecyclerAdapter(mDeviceModelList, this);
            //设置 线性布局
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            //设置数据源
            mRecyclerView.setAdapter(mMainRecyclerAdapter);
            //设置点击监听
            mMainRecyclerAdapter.setOnItemDeviceClickListener(mOnItemDeviceClickListener);
        } else {
            //更新数据
            mMainRecyclerAdapter.setListData(mDeviceModelList);
            //刷新列表
            mMainRecyclerAdapter.notifyDataSetChanged();
        }


    }

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;

    private void initBluetooth() {
        //获取 BluetoothManager
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        //获取 BluetoothAdapter
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        //判断是否支持
        if (mBluetoothAdapter == null) {
            Log.e("ble", "设备不支持蓝牙");
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
        } else {

            //开始扫描BLE设备
            scanBleDevice(true);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanBleDevice(false);
                }
            }, 5000);
        }
    }

    //正在扫描标识
    // true 正在扫描
    // false 不在扫描
    private boolean isScanBleDevice = false;

    /**
     * 扫描BLE设备
     */
    private void scanBleDevice(boolean isScanBleDevice) {
        if (!isScanBleDevice) {
            //开始扫描
            mBluetoothAdapter.startLeScan(mScanCallback);
            //更新标识
            isScanBleDevice = true;
        } else {
            //停止扫描
            mBluetoothAdapter.stopLeScan(mScanCallback);
            //更新标识
            isScanBleDevice = false;
        }

    }


    private void addDeviceToList(BluetoothDevice device) {
        if (device != null) {
            //获取蓝牙设备的名称
            String name = device.getName();
            //构造新的设备
            DeviceModel deviceModel = new DeviceModel();
            deviceModel.mBluetoothDevice = device;

            //保存设备
            if (!TextUtils.isEmpty(name)) {
                //
                if (mModelDeviceMap.contains(name)) {
                    mModelDeviceMap.remove(name);
                }

                mModelDeviceMap.put(name, deviceModel);
            } else {
                //设置 未知名称
                name = "未知 - " + device.getAddress();
                mModelDeviceMap.put(name, deviceModel);
            }

            //更新显示列表
            setRecyclerData();
        }
    }

    private ConcurrentHashMap<String, DeviceModel> mModelDeviceMap = new ConcurrentHashMap<>();
    //扫描到BLE设备的回调
    private BluetoothAdapter.LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d("ble", "扫描到设备 " + device.getName());
            if (Looper.myLooper() == Looper.getMainLooper()) {
                // Android 5.0 及以上
                //添加设备
                addDeviceToList(device);
                //更新列表
                setRecyclerData();
            } else {
                // Android 5.0 以下
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //添加设备
                        addDeviceToList(device);
                        //更新列表
                        setRecyclerData();
                    }
                });


            }
        }
    };


    //条目点击事件监听
    private MainRecyclerAdapter.OnItemDeviceClickListener mOnItemDeviceClickListener = new MainRecyclerAdapter.OnItemDeviceClickListener() {
        @Override
        public void onItemClick(View v, int position) {
            Log.e("BLE","click "+position);

            //
            DeviceModel deviceModel = mDeviceModelList.get(position);


        }
    };

}
