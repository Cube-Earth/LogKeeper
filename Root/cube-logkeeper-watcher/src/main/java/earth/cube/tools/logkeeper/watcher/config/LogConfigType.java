package earth.cube.tools.logkeeper.watcher.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum LogConfigType {
	
	@JsonProperty("text_pipe")
	PIPE_TEXT,
	
	@JsonProperty("structured_pipe")
	PIPE_STRUCTURED,

	@JsonProperty("files")
	FILES,
	
}
