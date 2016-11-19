# AndroidBluetooth
1、安卓蓝牙聊天通迅 2、安卓BLE通信
## 说明
    1、在ble包中，是谷歌官方中提供的方式
    2、在BleConnectionActivity 中，是依据 ble包中的信息提取出来的 BLE设备连接通迅的
    3、在MainActiviy中 为扫描BLE 设备方法
    4、DeviceModel 为设置Model
#权限申请
       <uses-permission android:name="android.permission.BLUETOOTH"/>
       <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
       <uses-feature
            android:name="android.hardware.bluetooth_le"
            android:required="true"/>

       最好添加上以下权限，以适配某些机型
       <uses-permission android:name="android.permission.WRITE_SETTINGS"/>

       <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>

       <uses-permission-sdk-23 android:name="android.permission.ACCESS_COARSE_LOCATION"/>


# 1、BLE协议通信
##1.1 检查蓝牙是否支持BLE协议
     boolean isBleUsed = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);

##1.2 初始化操作
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


##1.3 开启扫描
###1.3.1 开始扫描与停止
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
###1、3、2 扫描到BLE设备回调方法
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


##1.4 连接BLE设备
###1.4.1 获取BLE设备
    点击列表跳转到设备连接页面，将BLE设备信息传递

    //对应的BLE蓝牙设备
    private BluetoothDevice mBluetoothDevice;

    //创建连接

    //连接服务对应的Gatt
    private BluetoothGatt mBluetoothGatt;

    mBluetoothGatt = mBluetoothDevice.connectGatt(this, true, mGattCallback);

    //连接服务的回调响应

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());
                isConnect = true;
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                isConnect = false;
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "onServicesDiscovered gatt succeess received: " + status);
                //处理服务数据
                dataInitFunction();
            } else {
                Log.w(TAG, "onServicesDiscovered gatt other received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {


            }

            byte[] value = characteristic.getValue();

            Log.d("ble--", "ble -- " + "收到消息 " + new String(value));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

        }
    };

    当连接成功后，会回调方法 onServicesDiscovered
    当状态信息为 status == BluetoothGatt.GATT_SUCCESS 的时候，可以获取到外围设备中提供的服务

###1.4.2 处理获取扫描到的外围设备的服务
    private void dataInitFunction() {
            //获取 外围设备中提供的服务
            List<BluetoothGattService> mBluetoothGattServices = mBluetoothGatt.getServices();

            if (mBluetoothGattServices != null) {
                //获取每个服务中
                for (BluetoothGattService mBluetoothGattService : mBluetoothGattServices) {
                    //获取服务的UUID
                    String uuid = mBluetoothGattService.getUuid().toString();

                    Log.e("ble--", "ble-- " + uuid);

                    //获取服务中对应的 所有的characteristic
                    List<BluetoothGattCharacteristic> characteristics = mBluetoothGattService.getCharacteristics();

                    for (BluetoothGattCharacteristic bluetoothGattCharacteristic : characteristics) {
                        //获取characteristic的信息
                        String charaUUid = bluetoothGattCharacteristic.getUuid().toString();
                        Log.e("ble--", "ble-- --- " + charaUUid);
                    }
                }
            }
        }

###1.4.3 发送消息到 外围设备中
        private String mServiceUUIDString = "50b168cf-85fa-43e5-9665-a0faefb42a89";
        private String mServiceUUID = "beb07058-7edd-46de-af18-a7d4ae069e53";

        private boolean isConnect = true;

        //写入BLE 消息
        private void writeBluetooh() {
            if (mBluetoothGatt != null) {
                if (!isConnect) {
                    Log.e("ble", "连接断开，重新连接");
                    //连接已断开
                    mBluetoothGatt.connect();
                    return;
                }
                //获取指定类型的服务
                BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(mServiceUUIDString));
                if (service != null) {
                    //获取指定的 characteristic
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(mServiceUUID));
                    if (characteristic != null) {
                        //设置数据
                        characteristic.setValue("this is a bluetooth test ".getBytes());
                        //写入
                        mBluetoothGatt.writeCharacteristic(characteristic);
                    }
                }
            } else {
                Log.e("ble", "Gatt 为 null 重新创建");
                mBluetoothGatt = mBluetoothDevice.connectGatt(this, false, mGattCallback);
            }

        }
###1.4.4 读取外围设备中的信息
        private String mServiceUUIDString = "50b168cf-85fa-43e5-9665-a0faefb42a89";
        private String mServiceUUID = "beb07058-7edd-46de-af18-a7d4ae069e53";

        private boolean isConnect = true;
        //读取BLE消息
        private void readBluetoohMsgFunction() {
            if (mBluetoothGatt != null) {
                 //获取指定类型的服务
                 BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(mServiceUUIDString));
                    if (service != null) {
                        //获取指定的 characteristic
                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(mServiceUUID));
                        if (characteristic != null) {
                            //读取数据
                            boolean b = mBluetoothGatt.readCharacteristic(characteristic);
                            if (b) {
                                Log.e(TAG, "readBluetoohMsgFunction: 读取成功");
                            } else {
                                Log.e(TAG, "readBluetoohMsgFunction: 读取失败");
                            }
                        }
                    }
             }
        }

        那么会在gattcallback方法中回调 onCharacteristicRead，以获取读到的消息
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                       BluetoothGattCharacteristic characteristic,
                                       int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {


            }

            byte[] value = characteristic.getValue();

             Log.d("ble--", "ble -- " + "收到消息 " + new String(value));
         }


 #经典蓝牙
 ##初始化操作
    1、打开蓝牙的方式有两种，这里采用 enable方法，另一种方法是使用广播意图的形式来打开，两者的区别是，
    前者不会提示用户，后者会提示用户选择性的打开蓝牙
    2、BluetoothAdapter 代表本地的蓝牙设备，通过此类可以让用户能执行基本的蓝牙任务
    3、在这里，蓝牙的状态改变，系统会发送广播通知，所以我们可以注册广播接收者来监听蓝牙状态的改变

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

            } else {
                //蓝牙未打开
                //打开蓝牙设备
                mDefaultAdapter.enable();
            }

            return true;
    }

    //蓝牙状态改变
    intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

 ##开启扫描 其他蓝牙设备
 ###开始扫描
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

      开始扫描蓝牙设备，系统发送广播消息通知
      //开始扫描设备
      intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
 ###发现设备
      当扫描到设备的时候 ，系统会发送广播消息通知

      intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
      广播接收，获取设备
      if (TextUtils.equals(action, BluetoothDevice.ACTION_FOUND)) {
             //发现设备
            //获取蓝牙对象
           BluetoothDevice devices = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
      }
 ###扫描完成
      扫描完成后，系统会发送广播消息通知
      //扫描完成
      intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);


 ##取消扫描
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
