package earth.cube.tools.logkeeper.core.utils.jackson;

import java.nio.file.Path;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

public class JsonEmitter {
	
	private ObjectMapper _mapper;

	public JsonEmitter() {
		SimpleModule module = new SimpleModule();
	    module.setSerializerModifier(new BeanSerializerModifier()
	    {
	      @Override public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
	        if (LocalDateTime.class.isAssignableFrom(beanDesc.getBeanClass()))
		          return new LocalDateTimeSerializer();
 	        else
		        if (Path.class.isAssignableFrom(beanDesc.getBeanClass()))
		        	return new PathSerializer();
	        	
	        return serializer;
	      }
	    });			
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(module);
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.disable(MapperFeature.AUTO_DETECT_CREATORS, MapperFeature.AUTO_DETECT_FIELDS,
	            MapperFeature.AUTO_DETECT_GETTERS, MapperFeature.AUTO_DETECT_IS_GETTERS);
		_mapper = mapper;
	}
	
	public String getJson(Object obj) {
		try {
		    return _mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}		
	}
	
	public static String toJson(Object obj) {
		return new JsonEmitter().getJson(obj);
	}

}
