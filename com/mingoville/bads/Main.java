package com.mingoville.bads;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;


public class Main {
	private static Object lock = new Object();
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		DeviceAndServiceDiscover dsd =new DeviceAndServiceDiscover(lock);
		//display local device address and name
		LocalDevice localDevice = null;
		try {
			localDevice = LocalDevice.getLocalDevice();
		} catch (BluetoothStateException e1) {
			System.err.println("Could not get localDevice");
		}
		System.out.println("Address: "+localDevice.getBluetoothAddress());
		System.out.println("Name: "+localDevice.getFriendlyName());
		//find devices
		DiscoveryAgent dAgent = localDevice.getDiscoveryAgent();
		System.out.println("Starting device inquiry...");
		try {
			dAgent.startInquiry(DiscoveryAgent.GIAC, dsd);
		} catch (BluetoothStateException e1) {
			System.err.println("Could not start Device Inquiry");
		}
		try {
			synchronized(lock){
				lock.wait();

			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		dAgent.cancelInquiry(dsd);
		System.out.println("Device Inquiry Completed. ");

		LinkedList<RemoteDevice> devices = dsd.getDiscoveredDevices();
		if(devices.size() <= 0){
			System.out.println("No Devices Found .");
		}
		else{
			System.out.println("Bluetooth Devices: ");
			int i = 1;
			for (RemoteDevice dev : devices){
				System.out.println((i)+". "+dev.getBluetoothAddress());
			}
		}

		// Set service uuid:

		UUID[] uuidSet = {new UUID("1105", true)};


		int[] attrSet = {0x0100, 0x0003, 0x0004};
		System.out.println("\nSearching for service...");

		for (RemoteDevice dev : devices)
		{
			int transID = -1;
			try {
				transID = dAgent.searchServices(attrSet,uuidSet,dev,dsd);
			} catch (BluetoothStateException e1) {
				System.out.println("Could not start Service Discovery");
			}
			System.out.println("Service Search in Progress ("+transID+")");
			try {
				synchronized(lock)
				{
					lock.wait();
				}
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			dAgent.cancelServiceSearch(transID);
		}
		//System.out.println("Service Discovery Completed. ");
		LinkedList<MobileDevice> mobiles = dsd.getMobileDevices();

		for (MobileDevice mobile : mobiles)
		{
			System.out.println("Connecting to: " +mobile.getServiceURL());
			// connection creation & sending file
			try
			{
				ClientSession cs = (ClientSession) Connector.open(mobile.getServiceURL());
				
				HeaderSet hs = cs.createHeaderSet();

				cs.connect(hs);
				System.out.println("Connection obtained");

				
				//file to send to phone
				//File file = new File("/home/myr/Ubuntu One/Pictures/B10-Storebaelt/IMG_2777.JPG");
				File file = new File("/home/myr/Downloads/links.htm");
				//File file = new File("/home/myr/Downloads/VenstrePlusOpgave.jar");
				InputStream is = new FileInputStream(file);
				byte filebytes[] = new byte[is.available()];
				is.read(filebytes);
				is.close();

				hs = cs.createHeaderSet();
				hs.setHeader(HeaderSet.NAME, file.getName());
				//hs.setHeader(HeaderSet.TYPE, "image/jpeg");
				hs.setHeader(HeaderSet.TYPE, "text/html");
				//hs.setHeader(HeaderSet.TYPE, "application/x-jar");
				hs.setHeader(HeaderSet.LENGTH, new Long(filebytes.length));

				Operation putOperation = cs.put(hs);
				//System.out.println("Pushing file: " + file.getName());
				//System.out.println("Total file size: " + filebytes.length + " bytes");

				OutputStream outputStream = putOperation.openOutputStream();
				outputStream.write(filebytes);
				System.out.println("File push complete");

				outputStream.close();
				putOperation.close();

				cs.disconnect(null);

				cs.close();
			}
			catch(Exception e)
			{
				System.out.println("connection:"+e);
				e.printStackTrace();
			}
		}
	}
}
