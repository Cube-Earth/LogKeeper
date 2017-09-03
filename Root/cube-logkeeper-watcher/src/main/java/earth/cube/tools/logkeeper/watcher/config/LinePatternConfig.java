package earth.cube.tools.logkeeper.watcher.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

import earth.cube.tools.logkeeper.watcher.utils.jackson.IAfterDeserialization;

public class LinePatternConfig implements IAfterDeserialization {
	
	@JsonProperty("pattern")
//	@JsonDeserialize(using = PatternDeserializer.class)
	private Pattern _textPattern;
	
	@JsonProperty("skip")
	private boolean _bSkip;
	
	@JsonProperty("fields")
    private Map<String, String> _fields = new HashMap<>();
	
	private boolean _bInvalid;
	
	
	public Pattern getTextPattern() {
		return _textPattern;
	}
	
	public boolean shouldSkip() {
		return _bSkip;
	}
	
	public Map<String, String> getFields() {
		return Collections.unmodifiableMap(_fields);
	}

	public boolean isInvalid() {
		return _bInvalid;
	}
	
	public boolean matches(String sLine) {
		return _textPattern.matcher(sLine).matches();
	}
	
	@Override
	public void afterDeserialization() {
		_bInvalid = _textPattern == null;
	}

}
