package fang.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ReadWriter implements IClose{
	private WriteThread writeThread = null;//1
	private ReadThread readThread = null;
	
	volatile boolean isActive = false;
	volatile boolean crashed = false;
	
	public ReadWriter(InputStream in) {
		readThread = new ReadThread(in);
	}
	
	public ReadWriter(OutputStream out) {
		writeThread = new WriteThread(out);
	}
	
	public ReadWriter(InputStream in,OutputStream out) {
		readThread = new ReadThread(in);
		writeThread = new WriteThread(out);
	}
	
	public ReadWriter setReadMsgHandler(MsgHandler msgHandler) {
		readThread.setMsgHandler(msgHandler);
		return this;
	}
	
	public ReadWriter setReadCrashHandler(CrashHandler readCrashHandler) {
		readThread.setCrashHandler(readCrashHandler);
		return this;
	}

	public ReadWriter setWriteCrashHandler(CrashHandler writeCrashHandler) {
		return this;
	}

	/**
	 * start it's read and write thread.
	 */
	public void setup(){
		int flag = 0;
		if(writeThread != null){
			writeThread.start();
			flag++;
		}
		if(readThread != null){
			readThread.start();
			flag++;	
		}
		if(2 != flag)
			System.err.println("no read or write thread in ReadWriter");
	}
	
	public void write(String msg) {
		if(writeThread.isAlive())
			writeThread.write(msg);
	}
	
	public void writeLine(String msg){
		String newDate = msg+"\r\n";
		write(newDate);
	}
	
	@Override
	public void close() {
		if(writeThread != null)
			writeThread.close();
		if(readThread != null)
			readThread.close();
	}
	
	public boolean isCrashed(){
		if(null == writeThread && null == readThread)
			return false;
		else if(null == writeThread && null != readThread)
			return readThread.isCrashed();
		else if(null != writeThread && null == readThread)
			return writeThread.isCrashed();
		else {
			return writeThread.isCrashed()||readThread.isCrashed();
		}
	}
}

class WriteThread extends Thread implements IClose{
	Queue<String> MsgSendingQueue = new LinkedBlockingQueue<>(255);
	OutputStream outputStream;
	volatile boolean stop=false;
	volatile boolean crashed = false;
	CrashHandler mCrashHandler = null;
	private String CrashMsg = "crashed";
	
	public WriteThread(OutputStream out) {
		outputStream = out;
	}
	
	public WriteThread(OutputStream out,CrashHandler crashHandler) {
		this(out);
		this.mCrashHandler = crashHandler;
	}
	
	public void write(String msg) {
		synchronized (MsgSendingQueue) {
			MsgSendingQueue.add(msg);
			MsgSendingQueue.notifyAll();
		}
	}
	
	@Override
	public void close() {
		stop = true;
		setCrashed(true);
		if(mCrashHandler != null)
			mCrashHandler.HandleCrash(CrashMsg);
		try {
			outputStream.close();
		} catch (Exception e) {
			System.out.println("WriteThread.close()");
		}
	}
	
	@Override
	public void run() {
		stop = false;
		while(!stop){
			synchronized (MsgSendingQueue) {
				try{
				if(MsgSendingQueue.isEmpty())
					MsgSendingQueue.wait();
				else {
					outputStream.write(MsgSendingQueue.remove().getBytes());
				}
				}catch(Exception e){
					this.close();
				}
			}
		}
	}
	
	
	public void setCrashHandler(CrashHandler crashHandler) {
		mCrashHandler = crashHandler;
	}

	public boolean isCrashed() {
		return crashed;
	}

	public void setCrashed(boolean crashed) {
		this.crashed = crashed;
	}	
}

/**
 * TCP client thread for receive and display msg
 * @author fang
 *
 */
class ReadThread extends Thread implements IClose{
	InputStream inputStream=null;
	volatile boolean stop=false;
	volatile MsgHandler mMsgHandler = null;
	volatile boolean crashed = false;
	CrashHandler mCrashHandler = null;
	private String CrashMsg = "crashed";
	
	public ReadThread(InputStream in) {
		inputStream = in;
	}
	
	public ReadThread(InputStream in,CrashHandler crashHandler) {
		this(in);
		this.mCrashHandler = crashHandler;
	}
	
	public void setMsgHandler(MsgHandler msgHandler) {
		mMsgHandler = msgHandler;
	}
	
	@Override
	public void close() {
		stop = true;
		setCrashed(true);
		if(mCrashHandler != null)
			mCrashHandler.HandleCrash(CrashMsg);
		try {
			inputStream.close();
		} catch (Exception e) {
			System.out.println("ReadThread.close()");
		}
	}
	
	@Override
	public void run() {
		stop = false;
		BufferedReader bf=new BufferedReader(new InputStreamReader(inputStream));
		while(!stop){
			try {
				if(bf == null){
					System.err.println("buffer reader is null");
					break;
				}
				String msg = bf.readLine();
				if(mMsgHandler != null){
					mMsgHandler.HandleMsg(msg);
				}else{
					if(msg !=null && !msg.isEmpty())
						System.out.println(msg);//display the msg
				}
			} catch (IOException e) {
				this.close();
			}
		}
	}
	
	public void setCrashHandler(CrashHandler crashHandler) {
		mCrashHandler = crashHandler;
	}
	
	public boolean isCrashed() {
		return crashed;
	}

	public void setCrashed(boolean crashed) {
		this.crashed = crashed;
	}
}

