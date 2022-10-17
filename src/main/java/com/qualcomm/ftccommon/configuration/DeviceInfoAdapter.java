package com.qualcomm.ftccommon.configuration;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.qualcomm.robotcore.hardware.configuration.ControllerConfiguration;
import java.util.LinkedList;
import java.util.List;

public class DeviceInfoAdapter extends BaseAdapter implements ListAdapter {
    private List<ControllerConfiguration> deviceControllers;
    private EditActivity editActivity;
    private int list_id;

    public long getItemId(int i) {
        return 0;
    }

    public DeviceInfoAdapter(EditActivity editActivity2, int i, List<ControllerConfiguration> list) {
        new LinkedList();
        this.editActivity = editActivity2;
        this.deviceControllers = list;
        this.list_id = i;
    }

    public int getCount() {
        return this.deviceControllers.size();
    }

    public Object getItem(int i) {
        return this.deviceControllers.get(i);
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = this.editActivity.getLayoutInflater().inflate(this.list_id, viewGroup, false);
        }
        ((TextView) view.findViewById(16908309)).setText(EditActivity.formatSerialNumber(this.editActivity, this.deviceControllers.get(i)));
        ((TextView) view.findViewById(16908308)).setText(this.deviceControllers.get(i).getName());
        return view;
    }
}
