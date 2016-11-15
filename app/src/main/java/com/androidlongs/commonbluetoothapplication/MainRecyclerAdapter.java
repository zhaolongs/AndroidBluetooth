package com.androidlongs.commonbluetoothapplication;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by androidlongs on 16/11/15.
 * 站在顶峰，看世界
 * 落在谷底，思人生
 */

public class MainRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<DeviceModel> mDeviceModelList;
    private Context mContext;

    public MainRecyclerAdapter(List<DeviceModel> deviceModelList, Context context) {
        this.mDeviceModelList = deviceModelList;
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.item_main_content, null);
        MainViewHolder viewHolder = new MainViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //设置数据
        ((MainViewHolder) holder).setDatas(position);
    }

    @Override
    public int getItemCount() {
        return mDeviceModelList.size();
    }


    //更新数据
    public void setListData(List<DeviceModel> deviceModelList) {
        this.mDeviceModelList = deviceModelList;
    }


    //设备ViewHolder
    private class MainViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private final TextView mTitleTextView;
        private final TextView mAddressTextView;


        private int mPosition;

        public MainViewHolder(View itemView) {
            super(itemView);

            //设置点击监听
            itemView.setOnClickListener(this);

            //初始化控件
            mTitleTextView = (TextView) itemView.findViewById(R.id.tv_home_item_title);
            mAddressTextView = (TextView) itemView.findViewById(R.id.tv_home_item_address);
        }

        private void setDatas(int position) {

            this.mPosition = position;
            //数据源
            DeviceModel deviceModel = mDeviceModelList.get(position);
            //蓝牙设备
            BluetoothDevice bluetoothDevice = deviceModel.mBluetoothDevice;

            //蓝牙名称
            String bluetoothName = bluetoothDevice.getName();
            //蓝牙地址
            String bluetoothAddress = bluetoothDevice.getAddress();

            //设置名称
            if (TextUtils.isEmpty(bluetoothName)) {
                mTitleTextView.setText("未知");
            }else {
                mTitleTextView.setText(bluetoothName);
            }

            //设置显示地址
            mAddressTextView.setText(bluetoothAddress);

        }

        @Override
        public void onClick(View v) {
            if (mOnItemDeviceClickListener != null) {
                mOnItemDeviceClickListener.onItemClick(v,mPosition);
            }
        }
    }

    private OnItemDeviceClickListener mOnItemDeviceClickListener;
    public interface OnItemDeviceClickListener{
        void onItemClick(View v, int position);
    }

    //设置条目点击监听
    public void setOnItemDeviceClickListener(OnItemDeviceClickListener onItemDeviceClickListener) {
        mOnItemDeviceClickListener = onItemDeviceClickListener;
    }
}
