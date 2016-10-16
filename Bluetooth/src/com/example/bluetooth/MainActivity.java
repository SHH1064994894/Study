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

	private List<BluetoothDevice> bondeList = new ArrayList<BluetoothDevice>();// ����Ե��豸�б�
	private List<BluetoothDevice> usableList = new ArrayList<BluetoothDevice>();// �����豸�б�
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
				setTitle("ɨ�����");
				break;
			case Constants.START_DISCOVERY:
				setTitle("����ɨ�������豸");
				break;
			case Constants.CONNECTING:
				setTitle("��������");
				break;
			case Constants.CONNECT_FAILED:
				setTitle("����ʧ��");
				break;
			case Constants.CONNECT_OK:
				setTitle("���ӳɹ�");
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
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);// ����״̬�ı�ʱ
		filter.addAction(BluetoothDevice.ACTION_FOUND);// ɨ�赽�豸
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);// ����ɨ��

		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);// ɨ�����

		registerReceiver(receiver, filter);// ע��㲥
	}

	// ��������Ƿ��Ѿ���
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
		Log.i("����Ե��豸��", "=====>>" + pairedDevices.size());
		if (pairedDevices.size() > 0) {
			for (Iterator<BluetoothDevice> it = pairedDevices.iterator(); it
					.hasNext();) {
				BluetoothDevice device = it.next();
				bondeList.add(device);
			}
			Log.i("����Ե��豸��", "=====>>" + bondeList.size());

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
			Tools.LogMsg("����ɨ����", "����ɨ����");
			bluetoothAdapter.cancelDiscovery();
			Tools.LogMsg("ȡ��ɨ��", "ȡ��ɨ��");
			usableList.removeAll(usableList);
			usable_lv.setAdapter(null);
		}
		Tools.LogMsg("��ʼɨ��", "��ʼɨ��");
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
					Log.i("�����豸��:", "====>>>" + usableList.size());
					Tools.senMsg(uiHandler, Constants.USABLE_DEVICE);
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				Tools.senMsg(uiHandler, Constants.DISCOVERY_FINISHED);
				Log.i("ɨ�����", "ɨ�����");

			} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				Tools.senMsg(uiHandler, Constants.START_DISCOVERY);
				Log.i("��ʼɨ��", "��ʼɨ��");
			} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				switch (bluetoothAdapter.getState()) {
				case BluetoothAdapter.STATE_ON:
					state = bluetoothAdapter.getState();
					Log.i("������״̬", "====>>>" + state);
					getPairedDevices();
					break;

				case BluetoothAdapter.STATE_OFF:
					state = bluetoothAdapter.getState();
					Log.i("������״̬", "====>>>" + state);
					break;

				}
			}

		}

	};
	private BluetoothSocket socket;

	// ������
	private void OpenBT() {
		// 1.��ȡĬ������������

		if (bluetoothAdapter == null) {
			bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		}

		// 2.������
		if (!bluetoothAdapter.isEnabled()) {
			bluetoothAdapter.enable();
		}

		// 3.���������ɼ��������ÿɼ�ʱ��
		// Intent visible = new Intent(
		// BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		// visible.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
		// startActivity(visible);

		String adress = bluetoothAdapter.getAddress();
		String name = bluetoothAdapter.getName();

		Log.i("������ַΪ:", "========>>>" + adress);
		Log.i("��������Ϊ:", "========>>>" + name);

	}

	// �ر�����
	private void CloseBT() {

		if (bluetoothAdapter.isEnabled()) {
			bluetoothAdapter.disable();
		}
		state = bluetoothAdapter.getState();
		if (state == bluetoothAdapter.STATE_OFF) {
			Toast.makeText(getApplicationContext(), "�����ѹر�", 0).show();
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
			Log.i("����򿪰�ť", "====>>>�����");
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
		Log.i("������豸��", "====>>>" + selectDevice.getName());
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
		Log.i("��ʼ����", "���ӵ���������" + selectDevice.getName());
		final String SPP_UUID = "00001106-0000-1000-8000-00805F9B34FB";

		UUID uuid = UUID.fromString(SPP_UUID);

		try {
			socket = selectDevice.createRfcommSocketToServiceRecord(uuid);
			Log.i("socket", "====>>" + socket);
			socket.connect();
			boolean iscont = socket.isConnected();
			Tools.senMsg(uiHandler, Constants.CONNECT_OK);
			Log.i("��������OK��", "���ӵ���������" + selectDevice.getName() + "״̬" + iscont);
			Toast.makeText(getApplicationContext(), "���ӳɹ�", 0).show();
		} catch (IOException e) {
			try {
				socket.close();
				Tools.senMsg(uiHandler, Constants.CONNECT_FAILED);
				Log.i("�ر�socket", "===>> ����ʧ��,�ر�socket" + e.getMessage());
				Toast.makeText(getApplicationContext(),
						"����ʧ��" + e.getMessage(), 0).show();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}

	}

}
