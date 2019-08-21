package earth.cube.tools.logkeeper.watcher.config;

import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import earth.cube.tools.logkeeper.watcher.utils.FilePermissions;
import earth.cube.tools.logkeeper.watcher.utils.FileUtil;

@JsonRootName("log")
public class LogConfigStructuredPipe extends AbstractLogConfig {
	
	@JsonProperty("file")
	private Path _file;

	@JsonProperty("create")
	private FilePermissions _perms;
	
	public LogConfigStructuredPipe() {
		super(LogConfigType.PIPE_STRUCTURED);
	}
	
	public Path getPath() {
		return _file;
	}
	
	@Override
	public void afterDeserialization() {
		super.afterDeserialization();
		FileUtil.createPipe(_file, _perms);
	}
	

}
