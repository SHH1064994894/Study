package com.example.bluetooth;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.example.bluetooth.R.layout;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnClickListener,
		OnCheckedChangeListener, OnItemClickListener {

	private LinearLayout bt_ll;
	private Button search_btn;
	private ToggleButton openBt_TB;
	private TextView hint_tv;
	private ListView paired_lv, usable_lv;
	private BluetoothAdapter bluetoothAdapter;
	private int state;

	private List<BluetoothDevice> bondeList = new ArrayList<BluetoothDevice>();// 已配对的设备列表
	private List<BluetoothDevice> usableList = new ArrayList<BluetoothDevice>();// 可用设备列表
	private deviceAdapter adapter;
	private Handler uiHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.USABLE_DEVICE:
				adapter = new deviceAdapter(getApplicationContext(), usableList);
				usable_lv.setAdapter(adapter);
				break;
			case Constants.PAIRED_DEVICE:
				adapter = new deviceAdapter(getApplicationContext(), bondeList);
				paired_lv.setAdapter(adapter);
				break;
			case Constants.DISCOVERY_FINISHED:
				setTitle("扫描完毕");
				break;
			case Constants.START_DISCOVERY:
				setTitle("正在扫描蓝牙设备");
				break;
			case Constants.CONNECTING:
				setTitle("正在连接");
				break;
			case Constants.CONNECT_FAILED:
				setTitle("连接失败");
				break;
			case Constants.CONNECT_OK:
				setTitle("连接成功");
				break;

			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		setOnclickListener();
		registerBluetoothReceiver();
		checkBT();

	}

	private void registerBluetoothReceiver() {
		// TODO Auto-generated method stub
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);// 蓝牙状态改变时
		filter.addAction(BluetoothDevice.ACTION_FOUND);// 扫描到设备
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);// 正在扫描

		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);// 扫描完毕

		registerReceiver(receiver, filter);// 注册广播
	}

	// 检查蓝牙是否已经打开
	private void checkBT() {
		if (bluetoothAdapter == null) {
			bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		}
		if (bluetoothAdapter.isEnabled()) {
			openBt_TB.setChecked(true);
			hint_tv.setVisibility(View.GONE);
			bt_ll.setVisibility(View.VISIBLE);
			getPairedDevices();
		}
	}

	private void getPairedDevices() {

		Set<BluetoothDevice> pairedDevices = bluetoothAdapter
				.getBondedDevices();
		Log.i("已配对的设备有", "=====>>" + pairedDevices.size());
		if (pairedDevices.size() > 0) {
			for (Iterator<BluetoothDevice> it = pairedDevices.iterator(); it
					.hasNext();) {
				BluetoothDevice device = it.next();
				bondeList.add(device);
			}
			Log.i("已配对的设备有", "=====>>" + bondeList.size());

		}

		Tools.senMsg(uiHandler, Constants.PAIRED_DEVICE);

	}

	private void initView() {

		openBt_TB = (ToggleButton) findViewById(R.id.openBt_TB);
		bt_ll = (LinearLayout) findViewById(R.id.bt_ll);
		hint_tv = (TextView) findViewById(R.id.hint_tv);
		search_btn = (Button) findViewById(R.id.search_btn);
		paired_lv = (ListView) findViewById(R.id.paired_lv);
		usable_lv = (ListView) findViewById(R.id.usable_lv);

	}

	private void setOnclickListener() {
		search_btn.setOnClickListener(this);
		openBt_TB.setOnCheckedChangeListener(this);
		paired_lv.setOnItemClickListener(this);
		usable_lv.setOnItemClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.search_btn:
			SearchDeives();
			break;
		}

	}

	private void SearchDeives() {
		if (usableList.size() > 0) {
			usableList.removeAll(usableList);
		}
		if (bluetoothAdapter.isDiscovering()) {
			Tools.LogMsg("正在扫描中", "正在扫描中");
			bluetoothAdapter.cancelDiscovery();
			Tools.LogMsg("取消扫描", "取消扫描");
			usableList.removeAll(usableList);
			usable_lv.setAdapter(null);
		}
		Tools.LogMsg("开始扫描", "开始扫描");
		bluetoothAdapter.startDiscovery();

	}

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					usableList.add(device);
					Log.i("可用设备有:", "====>>>" + usableList.size());
					Tools.senMsg(uiHandler, Constants.USABLE_DEVICE);
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				Tools.senMsg(uiHandler, Constants.DISCOVERY_FINISHED);
				Log.i("扫描完毕", "扫描完毕");

			} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				Tools.senMsg(uiHandler, Constants.START_DISCOVERY);
				Log.i("开始扫描", "开始扫描");
			} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				switch (bluetoothAdapter.getState()) {
				case BluetoothAdapter.STATE_ON:
					state = bluetoothAdapter.getState();
					Log.i("蓝牙的状态", "====>>>" + state);
					getPairedDevices();
					break;

				case BluetoothAdapter.STATE_OFF:
					state = bluetoothAdapter.getState();
					Log.i("蓝牙的状态", "====>>>" + state);
					break;

				}
			}

		}

	};
	private BluetoothSocket socket;

	// 打开蓝牙
	private void OpenBT() {
		// 1.获取默认蓝牙适配器

		if (bluetoothAdapter == null) {
			bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		}

		// 2.打开蓝牙
		if (!bluetoothAdapter.isEnabled()) {
			bluetoothAdapter.enable();
		}

		// 3.设置蓝牙可见，并设置可见时间
		// Intent visible = new Intent(
		// BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		// visible.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
		// startActivity(visible);

		String adress = bluetoothAdapter.getAddress();
		String name = bluetoothAdapter.getName();

		Log.i("蓝牙地址为:", "========>>>" + adress);
		Log.i("蓝牙名字为:", "========>>>" + name);

	}

	// 关闭蓝牙
	private void CloseBT() {

		if (bluetoothAdapter.isEnabled()) {
			bluetoothAdapter.disable();
		}
		state = bluetoothAdapter.getState();
		if (state == bluetoothAdapter.STATE_OFF) {
			Toast.makeText(getApplicationContext(), "蓝牙已关闭", 0).show();
		}

		paired_lv.setAdapter(null);
		if (bondeList.size() > 0) {
			bondeList.removeAll(bondeList);
		}

		usable_lv.setAdapter(null);
		if (usableList.size() > 0) {
			usableList.removeAll(usableList);
		}

		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (isChecked) {
			hint_tv.setVisibility(View.GONE);
			bt_ll.setVisibility(View.VISIBLE);
			OpenBT();
			Log.i("点击打开按钮", "====>>>点击打开");
		} else {
			bt_ll.setVisibility(View.GONE);
			hint_tv.setVisibility(View.VISIBLE);
			CloseBT();
		}

	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		BluetoothDevice selectDevice = (BluetoothDevice) parent.getAdapter()
				.getItem(position);
		Log.i("点击的设备名", "====>>>" + selectDevice.getName());
		Tools.senMsg(uiHandler, Constants.CONNECTING);
		if (bluetoothAdapter.isDiscovering()) {
			bluetoothAdapter.cancelDiscovery();
		}
		try {
			startConnect(selectDevice);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void startConnect(BluetoothDevice selectDevice) {
		// TODO Auto-generated method stub
		Log.i("开始连接", "连接的蓝牙名称" + selectDevice.getName());
		final String SPP_UUID = "00001106-0000-1000-8000-00805F9B34FB";

		UUID uuid = UUID.fromString(SPP_UUID);

		try {
			socket = selectDevice.createRfcommSocketToServiceRecord(uuid);
			Log.i("socket", "====>>" + socket);
			socket.connect();
			boolean iscont = socket.isConnected();
			Tools.senMsg(uiHandler, Constants.CONNECT_OK);
			Log.i("蓝牙连接OK！", "连接的蓝牙名称" + selectDevice.getName() + "状态" + iscont);
			Toast.makeText(getApplicationContext(), "连接成功", 0).show();
		} catch (IOException e) {
			try {
				socket.close();
				Tools.senMsg(uiHandler, Constants.CONNECT_FAILED);
				Log.i("关闭socket", "===>> 连接失败,关闭socket" + e.getMessage());
				Toast.makeText(getApplicationContext(),
						"连接失败" + e.getMessage(), 0).show();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}

	}

}
