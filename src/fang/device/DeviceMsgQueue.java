package fang.device;

import java.util.concurrent.LinkedBlockingQueue;

public class DeviceMsgQueue {
	private LinkedBlockingQueue<DeviceMsg> mQueue = null;
	private static volatile DeviceMsgQueue _instance = null;
	private DeviceMsgQueue(){
		if(mQueue == null){
			this.mQueue = new LinkedBlockingQueue<DeviceMsg>();
		}
	}
	/**
	 * 单例获得设备消息队列的实例。
	 * 注意：使用安全的单例模式，双重校验锁
	 * @return
	 */
	public static DeviceMsgQueue getInstance(){
		if(_instance == null){
			synchronized (DeviceMsgQueue.class) {
				if(_instance == null)
					_instance = new DeviceMsgQueue();
			}
		}
		return _instance;
	}
	
	public void add(DeviceMsg msg){
		mQueue.add(msg);
	}
	
	public void put(DeviceMsg msg){
		try {
			mQueue.put(msg);
			System.out.println("add MsqQueue success");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public DeviceMsg take(){
		try {
			return mQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public DeviceMsg top(){
		if(mQueue.isEmpty())
			return null;
		else
			return mQueue.peek();
	}
	
	public DeviceMsg pop(){
		if(mQueue.isEmpty())
			return null;
		return mQueue.remove();
	}
	
	public int size(){
		return mQueue.size();
	}
}
