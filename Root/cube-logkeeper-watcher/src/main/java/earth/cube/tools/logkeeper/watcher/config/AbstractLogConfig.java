package earth.cube.tools.logkeeper.watcher.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import earth.cube.tools.logkeeper.watcher.utils.jackson.IAfterDeserialization;

public abstract class AbstractLogConfig implements ILogConfig, IAfterDeserialization {
	
	private LogConfigType _configType;
	
	@JsonProperty("application")
	private String _sApplication;

	@JsonProperty("source")
	private String _sSource;

	@JsonProperty("type")
	private String _sType;

	
	protected AbstractLogConfig(LogConfigType configType) {
		_configType = configType;
	}
	
	public LogConfigType getConfigType() {
		return _configType;
	}
	
	public String getApplication() {
		return _sApplication;
	}

	public String getSource() {
		return _sSource;
	}
	
	public String getType() {
		return _sType;
	}

	public void afterDeserialization() {
		_sType = "json";
	}


}
