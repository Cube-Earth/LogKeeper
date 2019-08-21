package earth.cube.tools.logkeeper.watcher.config;

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

@JsonRootName("log")
public class LogConfigFiles extends AbstractLogConfig {
	
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
	
	@JsonProperty("lines")
	private List<LinePatternConfig> _pattern;
	
	private Pattern _globPattern;
	
	public LogConfigFiles() {
		super(LogConfigType.FILES);
	}
		
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
	
	public List<LinePatternConfig> getLineRules() {
		return Collections.unmodifiableList(_pattern);
	}
	
	private String globToRegex(String sGlob) {
		String sSpecial = "{}[]().*?|\\";
		StringBuilder sb = new StringBuilder();
		int n = sGlob.length();
		for(int i = 0; i < n; i++) {
			char c = sGlob.charAt(i);
			switch(c) {
				case '*':
					sb.append(".*");
					break;
					
				case '?':
					sb.append(".");
					break;
					
				default:
					if(sSpecial.indexOf(c) != -1)
						sb.append("\\");
					sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public void afterDeserialization() {
		super.afterDeserialization();
		
		if(_sGlobPattern != null && _sGlobPattern.length() != 0)
			_globPattern = Pattern.compile(globToRegex(_sGlobPattern), Pattern.CASE_INSENSITIVE);
	
		for(LinePatternConfig lineConfig : new ArrayList<>(_pattern))
			if(lineConfig.isInvalid()) {
				_log.error("afterDeserialization: configuration entry is invalid and has been removed. text pattern: {}", lineConfig.getTextPattern());
				_pattern.remove(lineConfig);
			}
		
		DirectoryUtils.mkdirs(_dir, _perms);
	}

}
