package com.example.bluetooth;

import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.hardware.Camera.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class deviceAdapter extends BaseAdapter {
	private Context context;
	private List<BluetoothDevice> list;

	public deviceAdapter(Context context, List<BluetoothDevice> list) {
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		

		ViewHolder holder;
		if (convertView == null) {

			convertView = LayoutInflater.from(context).inflate(
					R.layout.device_item, null);

			holder = new ViewHolder();
			holder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
			holder.address_tv = (TextView) convertView
					.findViewById(R.id.address_tv);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.name_tv.setText(list.get(position).getName());
		//holder.address_tv.setText(list.get(position).getAddress());
		return convertView;
	}

	class ViewHolder {
		private TextView name_tv, address_tv;

	}

}
