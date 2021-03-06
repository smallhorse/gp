package com.ubtechinc.goldenpig.pigmanager.widget;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.ubtechinc.bluetooth.UbtBluetoothDevice;
import com.ubtechinc.goldenpig.R;
import com.ubtechinc.goldenpig.base.BaseDialog;
import com.ubtechinc.goldenpig.comm.view.WrapContentLinearLayoutManager;
import com.yanzhenjie.recyclerview.swipe.widget.DefaultItemDecoration;

import java.util.ArrayList;

/**
 * @auther :hqt
 * @email :qiangta.huang@ubtrobot.com
 * @description :八戒机器人列表
 * @time :2018/8/24 20:44
 * @change :
 * @changetime :2018/8/24 20:44
 */
public class PigListDialog extends BaseDialog {
    private ArrayList<UbtBluetoothDevice> mLeList;
    private RecyclerView mPigRycView;
    private PigListAdapter mPigAdapter;
    private TextView mPigClose;

    public PigListDialog(@NonNull Context context) {
        super(context);
        init();
    }

    private void init() {
        View root = View.inflate(getContext(), R.layout.dialog_pig_list, null);

        Window dialogWindow = getWindow();
        dialogWindow.setBackgroundDrawableResource(R.color.ubt_transparent);

        setCanceledOnTouchOutside(false);
        this.setContentView(root);
        mLeList = new ArrayList<>();
        mPigRycView = findViewById(R.id.ubt_pig_list_ryv);
        mPigClose = findViewById(R.id.ube_tv_close);
        mPigRycView.setLayoutManager(new WrapContentLinearLayoutManager(getContext()));
        mPigRycView.addItemDecoration(new DefaultItemDecoration(getContext().getResources().getColor(R.color.ubt_wifi_list_divider)));
        mPigAdapter = new PigListAdapter(mLeList);
        mPigRycView.setAdapter(mPigAdapter);
        mPigAdapter.setCloseView(mPigClose);

        mPigClose.setOnClickListener(v -> cancel());
    }

    public void setBluetoothItemClickListener(OnPigListItemClickListener listener) {
        mPigAdapter.setItemClickListener(listener);
    }

    public void updateData(UbtBluetoothDevice ubtBleDevice) {
        if (ubtBleDevice != null) {
            int rawIndex = isHasDevice(ubtBleDevice.getDevice());
            if (rawIndex < 0) {
                mLeList.add(ubtBleDevice);
                mPigAdapter.updateList(mLeList);
                mPigAdapter.notifyItemInserted(mLeList.size());
            }
        }
    }

    private int isHasDevice(BluetoothDevice device) {
        if (mLeList == null) {
            return -1;
        }
        final int devLen = mLeList.size();
        for (int index = 0; index < devLen; index++) {
            if (mLeList.get(index).getDevice().getName().equals(device.getName())) {
                return index;
            }
        }
        return -1;
    }

    public ArrayList<UbtBluetoothDevice> getLeList() {
        return mLeList;
    }

}
