package earth.cube.tools.logkeeper.watcher.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import earth.cube.tools.logkeeper.watcher.utils.FilePermissions;
import earth.cube.tools.logkeeper.watcher.utils.FileUtil;

@JsonRootName("log")
public class LogConfigTextPipe extends AbstractLogConfig {
	
	private final Logger _log = LogManager.getLogger(getClass());
	
	@JsonProperty("file")
	private Path _file;

	@JsonProperty("create")
	private FilePermissions _perms;

	@JsonProperty("encoding")
	private String _sEncoding = "iso-8859-1";

	@JsonProperty("lines")
	private List<LinePatternConfig> _pattern;
	
	
	public LogConfigTextPipe() {
		super(LogConfigType.PIPE_TEXT);
	}
	
	public Path getPath() {
		return _file;
	}
	
	
	public String getEncoding() {
		return _sEncoding;
	}
	

	public List<LinePatternConfig> getLineRules() {
		return Collections.unmodifiableList(_pattern);
	}
	
	
	public void afterDeserialization() {
		super.afterDeserialization();
		
		for(LinePatternConfig lineConfig : new ArrayList<>(_pattern))
			if(lineConfig.isInvalid()) {
				_log.error("afterDeserialization: configuration entry is invalid and has been removed. text pattern: {}", lineConfig.getTextPattern());
				_pattern.remove(lineConfig);
			}
		
		FileUtil.createPipe(_file, _perms);
	}
	
}
