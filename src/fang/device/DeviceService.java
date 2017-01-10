package fang.device;

import java.net.ServerSocket;
import java.net.Socket;

import fang.io.IClose;

/**
 * a tcp server for devices
 * @author fang
 *
 */
public class DeviceService implements Runnable,IClose{
	//device service
	private static DeviceService _Instance = null;
	private DeviceRepo mDeviceRepo = null;
	private DeviceCmdMsgQueue mCmdQueue = null;
	private DeviceMsgQueue mMsgQueue = null;
	private volatile boolean stoped = false;
	
	//server
	private ServerSocket mServerSocket = null;
	private int PORT = 8989;
	
	private DeviceService() {
		mDeviceRepo = DeviceRepo.getInstance();
		mCmdQueue = DeviceCmdMsgQueue.getInstance();
		mMsgQueue = DeviceMsgQueue.getInstance();
	}
	
	public DeviceService setPORT(int port){
		this.PORT = port;
		return this;
	}
	
	public boolean isStoped() {
		return stoped;
	}

	public DeviceService stopService() {
		this.stoped = true;
		return this;
	}

	/**
	 * 单例获得设备消息队列的实例。
	 * 注意：使用安全的单例模式，双重校验锁
	 * @return
	 */
	public static DeviceService getInstance(){
		if(_Instance == null){
			synchronized (DeviceService.class) {
				_Instance = new DeviceService();
			}
		}
		return _Instance;
	}
	
	/**
	 * start device service
	 */
	public void startService(){
		new Thread(this).start();
		new Thread(new DeviceCmdMsgServiceThread(DeviceCmdMsgQueue.getInstance())).start();
		System.out.println("Device service start");
	}
	
	@Override
	public void run() {
		try {
			mServerSocket = new ServerSocket(PORT);
			System.out.println("device service Listening Port:"+PORT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Socket socket = null;
		while(!stoped){
			try {
				socket = mServerSocket.accept();
				new Thread(new DeviceServiceThread(socket)).start();
				System.out.println("Device "+socket.getInetAddress()+" connected");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//close
		try {
			if(mServerSocket != null)
				mServerSocket.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public void close() {
		stopService();
		
	}
}

class DeviceServiceThread implements Runnable{
	private Socket mSocket = null;
	
	public DeviceServiceThread(Socket socket) {
		setSocket(socket);
	}
	
	public void setSocket(Socket socket) {
		mSocket = socket;
	}

	@Override
	public void run() {
		DeviceRepo repo = DeviceRepo.getInstance();
		Device device = new Device(mSocket);
		device.init();
		do{
			//check if the device add OK
			if(device.getId() != null ){
				if(null != repo.findById(device.getId()))
					break;
			}
			else{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}while(true);
		System.out.println("insert into the repo");
	}
}

class DeviceCmdMsgServiceThread implements Runnable{
	private DeviceCmdMsgQueue mQueue = null;
	public DeviceCmdMsgServiceThread(DeviceCmdMsgQueue queue) {
		mQueue = queue;
	}
	
	@Override
	public void run() {
		while(mQueue.size() == 0){
			try{
				Thread.sleep(1000);
			}catch(Exception e){
				e.printStackTrace();
			}
			while(mQueue.size() != 0){
				DeviceCmdMsg cmdMsg = mQueue.pop();
				String deviceID = cmdMsg.getId();
				String cmd = cmdMsg.getCmd();
				Device device = DeviceRepo.getInstance().findById(deviceID);
				if(device == null)
					continue;
				switch (DeviceCmd.type(cmd)) {//check cmdMsg type and send cmd to device
				case DeviceCmd.CMD_Open:
					device.send('@'+DeviceCmd.OpenCmd+'$');
					break;
				case DeviceCmd.CMD_Close:
					device.send('@'+DeviceCmd.OpenCmd+'$');
				case DeviceCmd.CMD_Timer:
					device.send('@'+cmdMsg.getCmd()+'$');
				default:
					break;
				}
			}
		}
	}
}

