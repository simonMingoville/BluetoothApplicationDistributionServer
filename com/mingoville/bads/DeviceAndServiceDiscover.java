package com.mingoville.bads;

import java.util.LinkedList;

import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;


public class DeviceAndServiceDiscover implements DiscoveryListener {
	private LinkedList<RemoteDevice> discoveredDevices = new LinkedList<RemoteDevice>(); 
	private LinkedList<MobileDevice> mobileDevices = new LinkedList<MobileDevice>();
	private Object lock = null;

	
	public DeviceAndServiceDiscover(Object lock) {
		super();
		this.lock = lock;
	}


	@Override
	public void deviceDiscovered(RemoteDevice dev, DeviceClass devClass) {

		if(discoveredDevices.contains(dev)==false)
		{
			discoveredDevices.add(dev);
		}
	}

	@Override
	public void inquiryCompleted(int discType) {
		// TODO Auto-generated method stub
		synchronized(lock)
		{
			lock.notify();
		}
	}

	@Override
	public void serviceSearchCompleted(int transID, int respCode) {

		synchronized(lock)
		{
			lock.notify();
		}
	}

	@Override
	public void servicesDiscovered(int transID, ServiceRecord[] sRecord) {
		for(int i = 0; i < sRecord.length; i++)
		{
			DataElement serviceNameElement = sRecord[i].getAttributeValue(0x0100);
			String _serviceName = (String)serviceNameElement.getValue();
			String serviceName = _serviceName.trim();
			System.out.println(_serviceName);

			if(serviceName.equals("OBEX Object Push"))
			{
				try
				{	
					String connectionURL = sRecord[i].getConnectionURL(0,false);
					MobileDevice mDev = new MobileDevice(sRecord[i].getHostDevice(),connectionURL);
					if(mobileDevices.contains(mDev)==false){
						mobileDevices.add(mDev);
					}
					System.out.println("[client:] A matching service has been found\n");

				} catch (Exception e)
				{
					System.out.println("[client:] oops");
				}
			}
		}
	}


	public LinkedList<MobileDevice> getMobileDevices() {
		return mobileDevices;
	}

	public void setMobileDevices(LinkedList<MobileDevice> mobileDevices) {
		this.mobileDevices = mobileDevices;
	}


	public Object getLock() {
		return lock;
	}

	public void setLock(Object lock) {
		this.lock = lock;
	}

	public LinkedList<RemoteDevice> getDiscoveredDevices() {
		return discoveredDevices;
	}


	public void setDiscoveredDevices(LinkedList<RemoteDevice> discoveredDevices) {
		this.discoveredDevices = discoveredDevices;
	}



}
