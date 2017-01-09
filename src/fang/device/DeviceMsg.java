package fang.device;

/**
 * @author fang
 *
 */
public class DeviceMsg {
	private String id;
	private String pm25;
	private String temperature;
	private String humidity;
	private String status;
	
	public DeviceMsg() {
		this.id = null;
		this.pm25=null;
		this.temperature=null;
		this.humidity=null;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setPm25(String pm25) {
		this.pm25 = pm25;
	}
	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}
	public void setHumidity(String humidity) {
		this.humidity = humidity;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getId() {
		return id;
	}
	public String getPm25() {
		return pm25;
	}
	public String getTemperature() {
		return temperature;
	}
	public String getHumidity() {
		return humidity;
	}
	
	/**
	 * msg like:"deviceID=123&pm=100&temperature=14&humidity=24&status=0"
	 * @param msg
	 * @return
	 */
	public boolean decode(String msg){
		//check
		if(msg == null || "".equals(msg))
			return false;
		//decode and get id
		String[] IdAndParam = msg.split("&");
		if(IdAndParam == null || IdAndParam.length != 5)
			return false;
		for(int i=0;i<5;i++){
			String[] kv = IdAndParam[i].split("=");
			if("deviceID".equals(kv[0]))
				setId(kv[1]);
			else if("pm".equals(kv[0]))
				setPm25(kv[1]);
			else if("humidity".equals(kv[0]))
				setHumidity(kv[1]);
			else if("temperature".equals(kv[0]))
				setTemperature(kv[1]);
			else if("status".equals(kv[0]))
				setStatus(kv[1]);
		}
		return true;
	}
	
	//get the coded string 
	public String toString(){
		String str =id+','+temperature+','+pm25+','+humidity+','+status;
		return str;
	}
}
