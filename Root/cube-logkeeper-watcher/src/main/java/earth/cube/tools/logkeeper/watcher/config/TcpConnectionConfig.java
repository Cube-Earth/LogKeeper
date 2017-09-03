package earth.cube.tools.logkeeper.watcher.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TcpConnectionConfig {
	
	@JsonProperty("host")
	private String _sHostName = "127.0.0.1";
	
	@JsonProperty("port")
	private int _nPort = 2000;
	
	public String getHostName() {
		return _sHostName;
	}
	
	public int getPort() {
		return _nPort;
	}

}
