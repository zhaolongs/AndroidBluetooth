package com.androidlongs.commonbluetoothapplication.customble;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidlongs.commonbluetoothapplication.DeviceModel;
import com.androidlongs.commonbluetoothapplication.R;

import java.util.List;
import java.util.UUID;

import static android.content.ContentValues.TAG;
import static com.androidlongs.commonbluetoothapplication.ble.BluetoothLeService.EXTRA_DATA;
import static com.androidlongs.commonbluetoothapplication.ble.BluetoothLeService.UUID_HEART_RATE_MEASUREMENT;

/**
 * Created by androidlongs on 16/11/16.
 * 站在顶峰，看世界
 * 落在谷底，思人生
 */

public class BleConnectionActivity extends Activity {

    private String mDeviceName;
    private String mDeviceAddress;

    //连接服务对应的Gatt
    private BluetoothGatt mBluetoothGatt;


    //对应的BLE蓝牙设备
    private BluetoothDevice mBluetoothDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_blue_connect);


        //获取数据
        Intent intent = this.getIntent();

        DeviceModel deviceModel = intent.getParcelableExtra("model");
        if (deviceModel == null) {
            Toast.makeText(this, "蓝牙设备不可使用", Toast.LENGTH_SHORT).show();
            return;
        }

        //获取蓝牙设备
        mBluetoothDevice = deviceModel.mBluetoothDevice;

        if (mBluetoothDevice == null) {
            Toast.makeText(this, "蓝牙设备不可使用", Toast.LENGTH_SHORT).show();
            return;
        }

        //获取蓝牙名称
        mDeviceName = mBluetoothDevice.getName();
        //获取蓝牙地址
        mDeviceAddress = mBluetoothDevice.getAddress();


        TextView titleTextView = (TextView) findViewById(R.id.tv_connetc_title);
        titleTextView.setText(mDeviceName + " 连接页面");

        //发送消息按钮
        Button sendMsgButton = (Button) findViewById(R.id.bt_connect_send);
        sendMsgButton.setOnClickListener(mSendMsgOnClickListener);

        //读取消息
        Button readMsgButton = (Button) findViewById(R.id.bt_connect_read);
        readMsgButton.setOnClickListener(mReadMsgOnClickListener);

        //创建连接
        mBluetoothGatt = mBluetoothDevice.connectGatt(this, true, mGattCallback);

    }


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


    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        String stringData = "";

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            stringData = String.valueOf(heartRate);
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
                stringData = new String(data) + "\n" + stringBuilder.toString();
            }
        }

        Log.e("ble--", "ble - " + stringData);
        sendBroadcast(intent);


    }

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

    //发送BLE消息的按钮点击监听
    private View.OnClickListener mSendMsgOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            writeBluetooh();
        }
    };
    //读取BLE消息的按钮点击监听
    private View.OnClickListener mReadMsgOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            readBluetoohMsgFunction();
        }
    };

}
