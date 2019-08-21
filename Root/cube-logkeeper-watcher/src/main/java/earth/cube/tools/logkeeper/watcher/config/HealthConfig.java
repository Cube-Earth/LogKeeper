package earth.cube.tools.logkeeper.watcher.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import earth.cube.tools.logkeeper.watcher.utils.DirectoryUtils;
import earth.cube.tools.logkeeper.watcher.utils.FilePermissions;
import earth.cube.tools.logkeeper.watcher.utils.jackson.IAfterDeserialization;

@JsonRootName("health_checks")
public class HealthConfig implements IAfterDeserialization {
	
	@JsonProperty("port")
	private int _nPort = -1;

	@JsonProperty("state_file")
	private Path _stateFile;
	
	@JsonProperty("create")
	private FilePermissions _perms;

	private List<Pattern> _msgs = new ArrayList<>();
	
	private List<Pattern> _throwables = new ArrayList<>();

	private boolean _bEmpty;
	
	
	@JsonProperty("messages")
	public void addMessagePattern(String[] saPattern) {
		for(String sPattern : saPattern)
			_msgs.add(Pattern.compile(sPattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE));
	}
	
	public List<Pattern> getMessagePatterns() {
		return Collections.unmodifiableList(_msgs);
	}

	@JsonProperty("throwables")
	public void addThrowable(String[] saPattern) {
		for(String sPattern : saPattern)
			_throwables.add(Pattern.compile(sPattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE));
	}

	public List<Pattern> getThrowablePatterns() {
		return Collections.unmodifiableList(_throwables);
	}
	
	@Override
	public void afterDeserialization() {
		_bEmpty = (_msgs.size() == 0 && _throwables.size() == 0) || (_nPort == -1 && _stateFile == null);
		
		if(_stateFile != null)
			DirectoryUtils.mkdirs(_stateFile.getParent(), _perms);
	}
	
	public boolean isEmpty() {
		return _bEmpty;
	}

	public int getPort() {
		return _nPort;
	}

	public Path getStateFile() {
		return _stateFile;
	}


}
