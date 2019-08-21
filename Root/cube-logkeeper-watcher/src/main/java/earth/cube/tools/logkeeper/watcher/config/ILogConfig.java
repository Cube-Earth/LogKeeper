package earth.cube.tools.logkeeper.watcher.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "type")

@JsonSubTypes({ 
	@Type(value = LogConfigFiles.class, name = "files") ,
    @Type(value = LogConfigTextPipe.class, name = "text_pipe"),
    @Type(value = LogConfigStructuredPipe.class, name = "structured_pipe")
})
public interface ILogConfig {
	
	LogConfigType getConfigType();

}
