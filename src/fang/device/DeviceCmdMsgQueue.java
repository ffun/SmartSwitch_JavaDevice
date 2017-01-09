package fang.device;

import java.util.concurrent.LinkedBlockingQueue;

public class DeviceCmdMsgQueue {
	private static DeviceCmdMsgQueue _Instance = null;
	private LinkedBlockingQueue<DeviceCmdMsg> mQueue=null; 
	
	private DeviceCmdMsgQueue() {
		if(mQueue == null)
			mQueue = new LinkedBlockingQueue<>();
	}
	
	/**
	 * 单例获得设备消息队列的实例。
	 * 注意：使用安全的单例模式，双重校验锁
	 * @return DeviceCmdMsgQueue的实例
	 */
	public static DeviceCmdMsgQueue getInstance(){
		if(_Instance == null){
			synchronized (DeviceMsgQueue.class) {
				if(_Instance == null)
					_Instance = new DeviceCmdMsgQueue();
			}
		}
		return _Instance;
	}

	public void add(DeviceCmdMsg msg){
		mQueue.add(msg);
	}
	
	public void put(DeviceCmdMsg msg){
		try {
			mQueue.put(msg);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public DeviceCmdMsg take(){
		try {
			return mQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public DeviceCmdMsg top(){
		if(mQueue.isEmpty())
			return null;
		else
			return mQueue.peek();
	}
	
	public DeviceCmdMsg pop(){
		if(mQueue.isEmpty())
			return null;
		return mQueue.remove();
	}
	
	public int size(){
		return mQueue.size();
	}
}
