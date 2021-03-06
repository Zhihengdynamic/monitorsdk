package com.yixin.monitors.sdk.test;

import android.util.Log;

import com.yixin.monitors.sdk.api.ApiMonitor;
import com.yixin.monitors.sdk.api.BluetoothListener;
import com.yixin.monitors.sdk.model.DeviceInfo;

public class TestMonitor implements ApiMonitor {
	private BluetoothListener	mBluetoothListener;
	private boolean				isConnected;

	@Override
	public void connect() {
		isConnected = true;
		if (mBluetoothListener == null) {
			Log.e("apimonitor", "没有设置蓝牙监听，不发生连接！");
			return;
		}
	}

	@Override
	public void disconnect() {
		mBluetoothListener.onBluetoothCancle();
		isConnected = false;
	}

	@Override
	public boolean isConnected() {
		return isConnected;
	}

	@Override
	public void setBluetoothListener(BluetoothListener listener) {
		mBluetoothListener = listener;
	}

	public BluetoothListener getBluetoothListener() {
		return mBluetoothListener;
	}

	@Override
	public DeviceInfo getDeviceInfo() {
		DeviceInfo m = new DeviceInfo();
		m.setDeviceName("测试设备");
		m.setDevicePin("");
		return m;
	}

	@Override
	public void configDevice(String deviceName, String devicePin) {

	}

}
