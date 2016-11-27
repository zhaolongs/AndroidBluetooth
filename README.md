# AndroidBluetooth 应用体验
   http://sj.qq.com/myapp/detail.htm?apkName=com.chat.blue.androidlongs.bluechatapplication
# AndroidBluetooth
1、安卓蓝牙聊天通迅 2、安卓BLE通信
## 说明
    1、在ble包中，是谷歌官方中提供的方式
    2、在BleConnectionActivity 中，是依据 ble包中的信息提取出来的 BLE设备连接通迅的
    3、在MainActiviy中 为扫描BLE 设备方法
    4、DeviceModel 为设置Model
#综述 
##
   BLE 是Bluetooth Low Energy的缩写 
   BL不再支持传统的 BR/RED协议 ，在BLE应用中 ,所有的协议 服务都是基于Gatt
## BLE协议蓝牙分类 
   单模 只支持BLE协议数据传输
   双模 支持经曲蓝牙协议与BLE协议数据传输
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


# 1、Android 中BLE协议通信
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

     BluetoothAdapter 代表要地的蓝牙适配器设备，通过此类可以让用户能用执行基本的蓝牙任务


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
##蓝牙设备接入监听
      当两个设备已配对后，对方可直接进行蓝牙连接，那么本方将为蓝牙设备接入
      public ConcurrentHashMap<String, BluetoothSocket> mSocketConcurrentHashMap = new ConcurrentHashMap<>();
        /**
         * 蓝牙UUID
         * UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
         */
      public static UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

      private boolean isConnect = true;

       //监听蓝牙设备接入
      private void registDeviceConnect() {
            if (mDefaultAdapter == null) {
                return;
            }
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    BluetoothServerSocket bluetoothServerSocket = null;
                    try {
                        /**
                         *
                         */
                        bluetoothServerSocket = mDefaultAdapter.listenUsingRfcommWithServiceRecord(UUID.randomUUID().toString(), SPP_UUID);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    BluetoothSocket bluetoothSocket;
                    while (isConnect) {
                        //注意，当accept()返回BluetoothSocket时，socket已经连接了，因此不应该调用connect方法。
                        //这里会线程阻塞，直到有蓝牙设备链接进来才会往下走
                        try {
                            bluetoothSocket = bluetoothServerSocket.accept();
                            Log.e("_-- ", "有新的设备接入");
                            if (bluetoothSocket != null) {
                                //mCommonBluetoothSocket = bluetoothSocket;

                                String name = bluetoothSocket.getRemoteDevice().getName();
                                if (mSocketConcurrentHashMap.containsKey(name)) {
                                    mSocketConcurrentHashMap.remove(name);
                                }
                                mSocketConcurrentHashMap.put(name, bluetoothSocket);

                                //回调结果通知
                                if (mDeviceConnectLiserner != null) {
                                    mDeviceConnectLiserner.onDeviceConnect(bluetoothSocket.getRemoteDevice());
                                }

                                //如果你的蓝牙设备只是一对一的连接，则执行以下代码
                                //bluetoothServerSocket.close();
                                //如果你的蓝牙设备是一对多的，则应该调用break；跳出循环
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (bluetoothServerSocket != null) {
                        try {
                            bluetoothServerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            ExectorManager.getInstance().addTask(runnable);
    }

##连接其他设备
    //连接设备
    public void connectDevice(BluetoothDevice device) {
            if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                //如果这个设备取消了配对，则尝试配对
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    //正在请求配对连接
                    device.createBond();

                }
            } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                //如果这个设备已经配对完成，则尝试连接
                BluetoothSocket bluetoothSocket = null;
                try {
                    //通过和服务器协商的uuid来进行连接
                    //mBluetoothSocket= btDev.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
                    //mBluetoothSocket = btDev.createRfcommSocketToServiceRecord(SPP_UUID);
                    int sdk = Integer.parseInt(Build.VERSION.SDK);
                    if (sdk >= 10) {
                        bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
                    } else {
                        bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                    }
                    if (bluetoothSocket != null)
                        //全局只有一个bluetooth，

                        //通过反射得到bltSocket对象，与uuid进行连接得到的结果一样，但这里不提倡用反射的方法
                        //mBluetoothSocket = (BluetoothSocket) btDev.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(btDev, 1);
                        Log.d("blueTooth", "开始连接...");
                    //在建立之前调用
                    if (mDefaultAdapter.isDiscovering())
                        //停止搜索
                        mDefaultAdapter.cancelDiscovery();
                    //如果当前socket处于非连接状态则调用连接
                    if (!bluetoothSocket.isConnected()) {
                        //你应当确保在调用connect()时设备没有执行搜索设备的操作。
                        // 如果搜索设备也在同时进行，那么将会显著地降低连接速率，并很大程度上会连接失败。
                        bluetoothSocket.connect();

                    }
                    Log.d("blueTooth", "已经链接");
                    if (mSocketConcurrentHashMap.containsKey(bluetoothSocket.getRemoteDevice().getName())) {
                        mSocketConcurrentHashMap.remove(bluetoothSocket.getRemoteDevice().getName());
                    }
                    mSocketConcurrentHashMap.put(bluetoothSocket.getRemoteDevice().getName(), bluetoothSocket);

                    if (mOnBondConnectLiserner != null) {
                        mOnBondConnectLiserner.onBondConnectSuccess(bluetoothSocket);
                    }

                } catch (Exception e) {
                    Log.e("blueTooth", "...链接超时");

                    e.printStackTrace();
                }
            }

    }
  BluetoothSocket的connect方法，是尝试连接到远程设备，该方法将阻塞，直到一个连接建立或者失效，如果该方法没有返回异常值，则该端口现在已经建立，
    当设备下在进行时，创建对远程蓝牙设备的新的连接将不可被尝试

##接收蓝牙消息监听
    private boolean isReciviceMsg = true;

        //接收蓝牙消息监听
    private void registDeviceMsgFunction() {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    try {
                        InputStream inputStream = null;
                        while (isReciviceMsg) {
                            for (Map.Entry<String, BluetoothSocket> stringBluetoothSocketEntry : mSocketConcurrentHashMap.entrySet()) {
                                BluetoothSocket bluetoothSocket = stringBluetoothSocketEntry.getValue();
                                BluetoothDevice remoteDevice = bluetoothSocket.getRemoteDevice();

                                inputStream = bluetoothSocket.getInputStream();
                                // 从客户端获取信息
                                BufferedReader bff = new BufferedReader(new InputStreamReader(inputStream));
                                String json;

                                while ((json = bff.readLine()) != null) {

                                    if (mOnBluetoothMsgLiserner != null) {
                                        mOnBluetoothMsgLiserner.onReciveMsg(json, remoteDevice);
                                    }
                                    //说明接下来会接收到一个文件流
                                    if ("file".equals(json)) {
                                        FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/test.gif");
                                        int length;
                                        int fileSzie = 0;
                                        byte[] b = new byte[1024];
                                        // 2、把socket输入流写到文件输出流中去
                                        while ((length = inputStream.read(b)) != -1) {
                                            fos.write(b, 0, length);
                                            fileSzie += length;
                                            System.out.println("当前大小：" + fileSzie);
                                            //这里通过先前传递过来的文件大小作为参照，因为该文件流不能自主停止，所以通过判断文件大小来跳出循环
                                        }
                                        fos.close();

                                    }
                                }
                            }

                        }

                        if (inputStream != null) {
                            inputStream.close();
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            ExectorManager.getInstance().addTask(runnable);
    }
    其中，ExectorManager是线程池管理子线程轮循任务
##发送消息
    当两个设备连接后，就可以进行省沟通
    发送普通文本消息
        /**
         * 发送消息
         *
         * @param msg
         */
    public void sendBluetoothMsg(String msg, BluetoothSocket bluetoothSocket) {
            if (bluetoothSocket == null || TextUtils.isEmpty(msg)) return;
            try {
                msg += "\n";
                OutputStream outputStream = bluetoothSocket.getOutputStream();
                outputStream.write(msg.getBytes("utf-8"));
                outputStream.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
     }
