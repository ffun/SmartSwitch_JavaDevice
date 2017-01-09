package fang.device;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import fang.io.CrashHandler;
import fang.io.IClose;
import fang.io.ReadWriter;
import fang.io.MsgHandler;

/**
 * Device class
 * Device Object add itself into the DeviceRepo and 
 * 				 send DeviceMsg into the DeviceMsgQueue
 * 				 throug the StringMsgHandler
 * @author Administrator
 *
 */
public class Device implements MsgHandler,CrashHandler,IClose{
	private Socket mSocket = null;
	private DeviceMsg mDeviceMsg = null;
	private ReadWriter mReadWriter = null;
	private MsgHandler mMsgHandler=null;
	private CrashHandler mCrashHandler = null;
	private List<IClose> mResourceList = new LinkedList<>();
	
	private String id = null;
	
	//DeviceMsgQueue
	private DeviceMsgQueue msgQueue = null;
	//DeviceRepo
	private DeviceRepo mRepo = null;
	
	public Device(Socket socket) {
		this.mSocket = socket;
		setRecMsgHandler(this);
		setCrashHandler(this);
		msgQueue = DeviceMsgQueue.getInstance();
		mRepo = DeviceRepo.getInstance();
	}
	
	/**
	 * set the StringMsgHandler
	 * if the param of handler is null,the Object's hander is itself
	 * @param handler
	 */
	public void setRecMsgHandler(MsgHandler handler){
		this.mMsgHandler = handler;
	}
	
	private MsgHandler getSelfMsgHandle(){
		return mMsgHandler;
	}
	
	public void setCrashHandler(CrashHandler crashHandler) {
		mCrashHandler = crashHandler;
	}
	
	private CrashHandler getSelfCrashHandle(){
		return mCrashHandler;
	}

	public void init(){
		InputStream inStream;
		OutputStream outStream;
		try{
		 inStream = mSocket.getInputStream();
		 outStream = mSocket.getOutputStream();
		 mReadWriter = new ReadWriter(inStream, outStream);
		 mReadWriter.setReadMsgHandler(getSelfMsgHandle());
		 mReadWriter.setReadCrashHandler(getSelfCrashHandle());
		 mReadWriter.setWriteCrashHandler(getSelfCrashHandle());
		 mReadWriter.setup();
		 mResourceList.add(mReadWriter);//add to closeable resource
		 System.out.println("device init start");
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public String getId(){
		return id;
	}
	
	/**
	 * send cmd to the device
	 * @param cmd
	 */
	public void send(String cmd){
		mReadWriter.write(cmd);
	}

	@Override
	public void HandleMsg(String msg) {
		DeviceMsg deviceMsg = new DeviceMsg();
		if(deviceMsg.decode(msg)){
			id = deviceMsg.getId();
			msgQueue.put(deviceMsg);
			System.out.println(deviceMsg.toString());
//			if(null == DeviceRepo.getInstance().findById(id)){
//				mRepo.add(this);
//				System.out.println("device:"+id+" success added into repo");
//			}
			mRepo.add(this);//will off-line reconnect
			System.out.println("device:"+id+" success added into repo");
		}
	}
	
	@Override
	public void HandleCrash(String CrashMsg) {
		if(this.id != null){
			mRepo.remove(this.id);
			System.out.println("device:"+id+" off-line");
		}
	}

	@Override
	public void close() {
		for(IClose iClose:mResourceList){
			iClose.close();
		}
	}
}