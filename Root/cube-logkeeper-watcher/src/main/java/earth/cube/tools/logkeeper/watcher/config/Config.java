package earth.cube.tools.logkeeper.watcher.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import earth.cube.tools.logkeeper.watcher.utils.jackson.IAfterDeserialization;
import earth.cube.tools.logkeeper.watcher.utils.jackson.PathDeserializer;
import earth.cube.tools.logkeeper.watcher.utils.jackson.PostProcessingDeserializer;

public class Config implements IAfterDeserialization {
	
	private final Logger _log = LogManager.getLogger(getClass());

/*
	@JsonProperty(value="logsx", required=true, defaultValue="xyz")
	@XmlElement(required=true, defaultValue="uvw")
	
	"required" is not considered.
	"defaultValue" is only for documentation purposes. 
*/
	
	@JsonProperty(value="logs")
	private List<LogConfig> _logConfigs;
	
	public static Config read(InputStream is) throws JsonParseException, JsonMappingException, IOException {
		SimpleModule module = new SimpleModule();
	    module.setDeserializerModifier(new BeanDeserializerModifier()
	    {
	      @Override public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer)
	      {
	        if (IAfterDeserialization.class.isAssignableFrom(beanDesc.getBeanClass()))
		          return new PostProcessingDeserializer(deserializer);
 	        else
		        if (Path.class.isAssignableFrom(beanDesc.getBeanClass()))
			          return new PathDeserializer();
	        	
	        return deserializer;
	      }
	    });


	    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	    mapper.registerModule(module);

	    return mapper.readValue(is, Config.class);		
	}
	
	public static Config read(Path path) throws JsonParseException, JsonMappingException, IOException {
		return read(Files.newInputStream(path, StandardOpenOption.READ));
	}

	public List<LogConfig> getLogConfigs() {
		return _logConfigs;
	}
	
	public LogConfig getLogConfig(Path logFile) {
		String sDir = logFile.getParent().toString();
		String sName = logFile.getFileName().toString();
		for(LogConfig logConfig : _logConfigs)
			if(sDir.equals(logConfig.getDirectory().toString()) && logConfig.getGlobPattern().matcher(sName).matches())
				return logConfig;
		return null;
	}

	@Override
	public void afterDeserialization() {
		for(LogConfig logConfig : new ArrayList<>(_logConfigs))
			if(logConfig.isInvalid()) {
				_log.error("afterDeserialization: configuration entry is invalid and has been removed. directory: {}, glob: {}", logConfig.getDirectory(), logConfig.getGlobString());
				_logConfigs.remove(logConfig);
			}
			
	}

}
