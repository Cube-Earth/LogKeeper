package earth.cube.tools.logkeeper.watcher.health_check;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.watcher.config.HealthConfig;

public class HealthCheck {
	
	private static HealthCheck _instance;
	
	private HealthConfig _config;
	private boolean _bEmpty;
	private IWebServer _webServer;
	
	public static HealthCheck getInstance() {
		return _instance;
	}
	
	public static void createInstance(HealthConfig config) throws IOException {
		_instance = new HealthCheck(config);
	}

	public HealthCheck(HealthConfig config) throws IOException {
		_config = config;
		_bEmpty = config == null || config.isEmpty();
		init();
	}
	
	public boolean isEmpty() {
		return _bEmpty;
	}
	
	public void close() {
		if(_webServer != null)
			_webServer.stop();
	}
	
	public void init() throws IOException {
		if(_bEmpty)
			return;
		
		int nPort = _config.getPort();
		if(nPort != -1) {
			try {
				Class.forName("com.sun.net.httpserver.HttpServer");
				_webServer = (IWebServer) Class.forName(getClass().getPackage().getName() + ".SunWebServer").newInstance();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				throw new IllegalStateException("For now, only Oracle JRE/JDK is supported for starting a Web health check!");
			}
			_webServer.start(nPort);
		}
	}
	
	protected boolean setError(String sMsg) {
		HealthStatus.setError(sMsg);
		Path stateFile = _config.getStateFile();
		if(stateFile != null && !Files.exists(stateFile)) {
	       try {
	    	   Files.newInputStream(stateFile).close();
			} catch (IOException e) {
				if(!Files.exists(stateFile))
					throw new RuntimeException(e);
			}
		}
		return false;
	}
	
	public boolean verify(LogMessage msg) {
		if(_bEmpty)
			return true;
		
		String sMsg = msg.getMessage();
		for(Pattern p : _config.getMessagePatterns()) {
			if(p.matcher(sMsg).matches())
				return setError("Message matched: " + p.pattern());
		}
			
		String sStackTrace = msg.getThrowableStackTrace();
		if(sStackTrace != null)
			for(Pattern p : _config.getThrowablePatterns()) {
				if(p.matcher(sStackTrace).matches())
					return setError("Throwable matched: " + p.pattern());
			}
		
		return true;
	}
	
	

}
