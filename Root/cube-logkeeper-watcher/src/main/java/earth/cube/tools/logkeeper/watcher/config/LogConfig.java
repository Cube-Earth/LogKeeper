package earth.cube.tools.logkeeper.watcher.config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import earth.cube.tools.logkeeper.watcher.utils.DirectoryUtils;
import earth.cube.tools.logkeeper.watcher.utils.FilePermissions;
import earth.cube.tools.logkeeper.watcher.utils.jackson.IAfterDeserialization;

@JsonRootName("log")
public class LogConfig implements IAfterDeserialization {
	
	private final Logger _log = LogManager.getLogger(getClass());
	
	@JsonProperty("dir")
	private Path _dir;
	
	@JsonProperty("glob")
	private String _sGlobPattern;

	@JsonProperty("encoding")
	private String _sEncoding = "iso-8859-1";

	@JsonProperty("create")
	private FilePermissions _perms;
	
	@JsonProperty("clean")
	private boolean _bClean;
	
	@JsonProperty("source")
	private String _sSource;

	@JsonProperty("type")
	private String _sType;

	@JsonProperty("lines")
	private List<LinePatternConfig> _pattern;
	
	private boolean _bInvalid;

	private Pattern _globPattern;
	
	
	public Path getDirectory() {
		return _dir;
	}
	
	public String getGlobString() {
		return _sGlobPattern;
	}
	
	public Pattern getGlobPattern() {
		return _globPattern;
	}
	
	public String getEncoding() {
		return _sEncoding;
	}
	
	public boolean shouldClean() {
		return _bClean;
	}
	
	public String getSource() {
		return _sSource;
	}

	public String getType() {
		return _sType;
	}

	public List<LinePatternConfig> getLineRules() {
		return Collections.unmodifiableList(_pattern);
	}
	
	public boolean isInvalid() {
		return _bInvalid;
	}
	
	public void afterDeserialization() {
		_bInvalid = _dir == null || _sGlobPattern == null || _sGlobPattern.length() == 0;
		_globPattern = Pattern.compile(_sGlobPattern, Pattern.CASE_INSENSITIVE);
	
		for(LinePatternConfig lineConfig : new ArrayList<>(_pattern))
			if(lineConfig.isInvalid()) {
				_log.error("afterDeserialization: configuration entry is invalid and has been removed. text pattern: {}", lineConfig.getTextPattern());
				_pattern.remove(lineConfig);
			}
	}
	
	public void mkdirs() throws IOException {
		if(_perms != null)
			DirectoryUtils.mkdirs(_dir, _perms);
	}
}
