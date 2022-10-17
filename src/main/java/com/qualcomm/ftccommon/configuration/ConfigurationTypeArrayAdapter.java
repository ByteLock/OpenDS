package com.qualcomm.ftccommon.configuration;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.qualcomm.ftccommon.configuration.EditActivity;

public class ConfigurationTypeArrayAdapter extends ArrayAdapter<EditActivity.ConfigurationTypeAndDisplayName> {
    static final /* synthetic */ boolean $assertionsDisabled = false;

    public ConfigurationTypeArrayAdapter(Context context, EditActivity.ConfigurationTypeAndDisplayName[] configurationTypeAndDisplayNameArr) {
        super(context, 17367049, configurationTypeAndDisplayNameArr);
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView textView = (TextView) view;
        if (textView == null) {
            textView = (TextView) LayoutInflater.from(getContext()).inflate(17367049, viewGroup, false);
        }
        EditActivity.ConfigurationTypeAndDisplayName configurationTypeAndDisplayName = (EditActivity.ConfigurationTypeAndDisplayName) getItem(i);
        if (configurationTypeAndDisplayName.configurationType.isDeprecated()) {
            textView.setPaintFlags(textView.getPaintFlags() | 16);
        }
        textView.setText(configurationTypeAndDisplayName.displayName);
        return textView;
    }
}
