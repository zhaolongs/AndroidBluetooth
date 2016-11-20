package com.androidlongs.commonbluetoothapplication.commonbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.androidlongs.commonbluetoothapplication.exector.ExectorManager;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by androidlongs on 16/11/20.
 * 站在顶峰，看世界
 * 落在谷底，思人生
 */

public class CommonBluetooth {

    private BluetoothAdapter mDefaultAdapter;

    private CommonBluetooth() {

    }

    public String getBluetoothName() {
        if (mDefaultAdapter != null) {
            return mDefaultAdapter.getName();
        }
        return null;
    }

    public String getBluetoothAddress() {
        if (mDefaultAdapter != null) {
            return mDefaultAdapter.getAddress();
        }
        return null;
    }

    public void setBluetoothName(String userName) {
        if (mDefaultAdapter != null) {
            mDefaultAdapter.setName(userName);
        }
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
        //校对本地数据
        //注册蓝牙设备接入监听
        registDeviceConnect();
        //设置可被发现
        setBluetoothDiscover();
        //接收消息
        registDeviceMsgFunction();
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

    //设置可被发现
    public void setBluetoothDiscover() {
        //获取蓝牙适配器
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        //通过反射方法设置蓝牙的可见性
        try {
            //获取设置被发现时间方法
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            //暴力解权
            setDiscoverableTimeout.setAccessible(true);
            //获取设置开启被发现方法
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
            //暴力解权
            setScanMode.setAccessible(true);
            //设置被发现时间
            setDiscoverableTimeout.invoke(adapter, Integer.MAX_VALUE);
            //设置被发现模式
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, Integer.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        //配对状态改变
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

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

            //配对状态改变时
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING://正在配对
                        Log.d("BlueToothTestActivity", "正在配对......");
                        //回调

                        break;
                    case BluetoothDevice.BOND_BONDED://配对结束
                        Log.d("BlueToothTestActivity", "完成配对");

                        //连接
                        connectDevice(device);

                        break;
                    case BluetoothDevice.BOND_NONE://取消配对/未配对
                        Log.d("BlueToothTestActivity", "取消配对");

                    default:
                        break;
                }
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


    //蓝牙设备接入监听
    public interface OnBluetoothDeviceConnectLiserner {
        void onDeviceConnect(BluetoothDevice device);
    }

    private OnBluetoothDeviceConnectLiserner mDeviceConnectLiserner;

    public void setDeviceConnectLiserner(OnBluetoothDeviceConnectLiserner deviceConnectLiserner) {
        mDeviceConnectLiserner = deviceConnectLiserner;
    }

    //蓝牙设备连接监听
    public interface OnBluetoothOnBondConnectLiserner {
        void onBondConnectSuccess(BluetoothSocket bluetoothSocket);

        void onBondConnectFailes();
    }

    private OnBluetoothOnBondConnectLiserner mOnBondConnectLiserner;

    public void setOnBondConnectLiserner(OnBluetoothOnBondConnectLiserner onBondConnectLiserner) {
        mOnBondConnectLiserner = onBondConnectLiserner;
    }


    //蓝牙消息接收回调
    public interface OnBluetoothMsgLiserner {
        void onReciveMsg(String msg, BluetoothDevice remoteDevice);
    }

    private OnBluetoothMsgLiserner mOnBluetoothMsgLiserner;

    public void setOnBluetoothMsgLiserner(OnBluetoothMsgLiserner onBluetoothMsgLiserner) {
        mOnBluetoothMsgLiserner = onBluetoothMsgLiserner;
    }
}
