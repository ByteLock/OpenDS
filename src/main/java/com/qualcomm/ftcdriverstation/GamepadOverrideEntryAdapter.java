package com.qualcomm.ftcdriverstation;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.qualcomm.ftcdriverstation.GamepadTypeOverrideMapper;
import java.util.List;

public class GamepadOverrideEntryAdapter extends BaseAdapter implements ListAdapter {
    private Activity activity;
    private List<GamepadTypeOverrideMapper.GamepadTypeOverrideEntry> gamepadOverrideEntries;

    public long getItemId(int i) {
        return 0;
    }

    public GamepadOverrideEntryAdapter(Activity activity2, List<GamepadTypeOverrideMapper.GamepadTypeOverrideEntry> list) {
        this.activity = activity2;
        this.gamepadOverrideEntries = list;
    }

    public int getCount() {
        return this.gamepadOverrideEntries.size();
    }

    public Object getItem(int i) {
        return this.gamepadOverrideEntries.get(i);
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = this.activity.getLayoutInflater().inflate(17367044, viewGroup, false);
        }
        GamepadTypeOverrideMapper.GamepadTypeOverrideEntry gamepadTypeOverrideEntry = this.gamepadOverrideEntries.get(i);
        ((TextView) view.findViewById(16908309)).setText(String.format("Mapped as %s", new Object[]{gamepadTypeOverrideEntry.mappedType.toString()}));
        ((TextView) view.findViewById(16908308)).setText(String.format("VID: 0x%X, PID: 0x%X", new Object[]{Integer.valueOf(gamepadTypeOverrideEntry.vid), Integer.valueOf(gamepadTypeOverrideEntry.pid)}));
        return view;
    }
}
