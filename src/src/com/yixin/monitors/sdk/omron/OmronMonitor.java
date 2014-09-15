package com.yixin.monitors.sdk.omron;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.signove.health.service.HealthAgentAPI;
import com.signove.health.service.HealthService;
import com.signove.health.service.HealthServiceAPI;
import com.yixin.monitors.sdk.api.ApiMonitor;
import com.yixin.monitors.sdk.api.BluetoothListener;
import com.yixin.monitors.sdk.bluetooth.OmronBluetoothConnection;
import com.yixin.monitors.sdk.model.DeviceInfo;

public class OmronMonitor implements ApiMonitor {
	OmronBluetoothConnection	mConnection;
	private Context				mContext;
	private BluetoothListener	mBluetoothListener;
	public static int[]			Specs				= { 0x1004 };
	private HealthServiceAPI	mApi;
	private HealthAgentAPI		mHealthAgentApiStub;									// 蓝牙设备接口
	private String				Tag					= "OmronDevice";
	private Intent				mHealthservice;
	private boolean				mIsBinded;
	private ServiceConnection	mServiceConnection	= new BluetoothServiceConnection();
	
	public OmronMonitor(Context context) {
		this.mContext = context;
		mHealthservice = new Intent(this.mContext, HealthService.class);
		Log.i(Tag, "Connect Device is Omron!");
	}
	
	@Override
	public void connect() {
		if (isConnected()) {
			Log.i(Tag, getDeviceInfo().getDeviceName() + "已经连接！不需要连接！");
			return;
		}
		
		// 如果没有配对，则先开始配对再连接，否则配对完成后不能再连接
		for (BluetoothDevice device : BluetoothAdapter.getDefaultAdapter().getBondedDevices()) {
			if (device.getName().equals(OmronBluetoothConnection.DEVICE_NAME)) {
				startService();
				break;
			}
		}
		
		if (mConnection == null) {
			mConnection = new OmronBluetoothConnection(mContext, mBluetoothListener, this);
			if (mHealthAgentApiStub == null) {
				mHealthAgentApiStub = new HealthAgentApiStub(mConnection);
			}
		}
		mConnection.connect();
	}
	
	@Override
	public void disconnect() {
		if (mConnection != null) {
			mConnection.disconnect();
		}
		stopService();
		mIsBinded = false;
	}
	
	@Override
	public boolean isConnected() {
		if (mConnection == null) { return false; }
		return mConnection.isConnected();
	}
	
	/**
	 * 开启服务
	 */
	private void startService() {
		if (mIsBinded) { return; }
		mContext.bindService(mHealthservice, mServiceConnection, Context.BIND_AUTO_CREATE); // 绑定服务
		this.mIsBinded = true;
		Log.d(Tag, "开始欧姆龙设备服务");
	}
	
	/**
	 * 停止服务
	 */
	private void stopService() {
		if (mIsBinded) {
			Log.d(Tag, "开始取消绑定服务");
			mContext.unbindService(mServiceConnection);
			mContext.stopService(mHealthservice);
		}
	}
	
	@Override
	public void setBluetoothListener(BluetoothListener listener) {
		this.mBluetoothListener = listener;
	}
	
	class BluetoothServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mApi = HealthServiceAPI.Stub.asInterface(service); // 实例化接口
			try {
				mApi.ConfigurePassive(mHealthAgentApiStub, Specs);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(Tag, "Healthservice服务绑定取消了！");
		}
	}
	
	private DeviceInfo	mDeviceInfo;
	
	@Override
	public DeviceInfo getDeviceInfo() {
		if (mDeviceInfo == null) {
			mDeviceInfo = new DeviceInfo();
			mDeviceInfo.setDevID(OmronBluetoothConnection.DEVICE_NAME);
			mDeviceInfo.setDeviceName("欧姆龙");
		}
		return mDeviceInfo;
	}
	
}
