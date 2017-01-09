package fang.device;

import java.util.HashMap;
import java.util.Map;

import fang.io.IClose;

/**
 * a (K,V) set to store the connected Device
 * @author fang
 *
 */
public class DeviceRepo implements IClose{
	private static DeviceRepo _Instance = null;
	Map<String, Device> mMap = null;
	private int mLock = 0;
	
	private DeviceRepo() {
		if(mMap == null)
			mMap = new HashMap<>();
	}
	/**
	 * 单例获得设备消息队列的实例。
	 * 注意：使用安全的单例模式，双重校验锁
	 * @return
	 */
	public static DeviceRepo getInstance(){
		if(_Instance == null){
			synchronized (DeviceRepo.class) {
				if(_Instance == null)
					_Instance = new DeviceRepo();
			}
		}
		return _Instance;
	}
	
	public synchronized void add(Device device) {
		if(mLock != 0 )
			return;
		mMap.put(device.getId(), device);
	}
	
	public Device remove(String deviceID){
		return mMap.remove(deviceID);
	}
	
	public Device findById(String deviceID){
		return mMap.get(deviceID);
	}
	
	public void Lock(){
		mLock = 1;
	}
	
	public void unLock(){
		mLock = 0;
	}
	
	public int size(){
		return mMap.size();
	}
	
	@Override
	public void close() {
		Lock();
		mMap.clear();
	}
}
