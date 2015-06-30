package com.napcosecurity.android.ilock.ui.viewcontrollers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.napcosecurity.android.ilock.R;
import com.napcosecurity.android.ilock.lock.Lock;

import java.util.ArrayList;

public class BatteryStatsAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Lock> arrayList;

    public BatteryStatsAdapter(Context context, ArrayList<Lock> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.activity_battery_list_row, parent, false);
        TextView tvLockName = (TextView) convertView.findViewById(R.id.tvLockName);
        TextView tvBattStat = (TextView) convertView.findViewById(R.id.tvBatteryStatus);
        /*tvLockName.setText(arrayList.get(position).getLockName());
        tvBattStat.setText(arrayList.get(position).getLockBatteryStatus());*/
        return convertView;
    }
}
