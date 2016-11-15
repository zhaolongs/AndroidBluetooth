# AndroidBluetooth
1、安卓蓝牙聊天通迅 2、安卓BLE通信

#检查蓝牙是否支持BLE协议
     boolean isBleUsed = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);

# 初始化操作
     //获取 BluetoothManager
     mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
     //获取 BluetoothAdapter
     mBluetoothAdapter = mBluetoothManager.getAdapter();
     //判断是否支持
     if (mBluetoothAdapter == null) {
          Log.e("ble","设备不支持蓝牙");
          Toast.makeText(this,"设备不支持蓝牙",Toast.LENGTH_SHORT).show();
      }else {
            Log.d("ble","设备支持蓝牙");
       }


# 开启扫描
###  开始扫描与停止
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
###  扫描到BLE设备回调方法
    //扫描到BLE设备的回调
        private BluetoothAdapter.LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    // Android 5.0 及以上

                } else {
                    // Android 5.0 以下
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });


                }
            }
        };