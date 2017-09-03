package earth.cube.tools.logkeeper.core.utils.jackson;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JsonParser {
	
	private ObjectMapper _mapper;

	public JsonParser() {
		SimpleModule module = new SimpleModule();
	    module.setDeserializerModifier(new BeanDeserializerModifier()
	    {
	      @Override
	      public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
	        if (LocalDateTime.class.isAssignableFrom(beanDesc.getBeanClass()))
		          return new LocalDateTimeDeserializer();
 	        else
		        if (Path.class.isAssignableFrom(beanDesc.getBeanClass()))
		        	return new PathDeserializer();
	        	
	        return deserializer;
	      }
	    });			
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(module);
		mapper.disable(MapperFeature.AUTO_DETECT_CREATORS, MapperFeature.AUTO_DETECT_FIELDS,
	            MapperFeature.AUTO_DETECT_SETTERS);
		_mapper = mapper;
	}
	
	public <T> T parse(String sJson, Class<T> clazz) {
		try {
			return _mapper.readValue(sJson, clazz);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public static <T> T fromJson(String sJson, Class<T> clazz) {
		return new JsonParser().parse(sJson, clazz);
	}

}
