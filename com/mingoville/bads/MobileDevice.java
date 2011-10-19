package com.mingoville.bads;

import javax.bluetooth.RemoteDevice;

public class MobileDevice extends RemoteDevice{
	private String serviceURL = null;

	protected MobileDevice(String address, String url) {
		super(address);
		setServiceURL(url);
	}
	
protected MobileDevice(RemoteDevice hostDevice, String url) {
	super(hostDevice.getBluetoothAddress());
	setServiceURL(url);
	// TODO Auto-generated constructor stub
	}

	//	public MobileDevice(RemoteDevice dev, String url) {
//		
//		MobileDevice(dev.getBluetoothAddress(),url);
//	}
	public String getServiceURL() {
		return serviceURL;
	}

	public void setServiceURL(String serviceURL) {
		this.serviceURL = serviceURL;
	}

}
