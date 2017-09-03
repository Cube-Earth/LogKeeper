package earth.cube.tools.logkeeper.watcher.utils.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class PostProcessingDeserializer extends StdDeserializer<Object> implements ResolvableDeserializer
{
  private static final long serialVersionUID = 7923585097068641765L;

  private final JsonDeserializer<?> defaultDeserializer;

  public PostProcessingDeserializer(JsonDeserializer<?> defaultDeserializer)
  {
    super(Object.class);
    this.defaultDeserializer = defaultDeserializer;
  }

  @Override public Object deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException, JsonProcessingException
  {
    Object deserializedUser = (Object) defaultDeserializer.deserialize(jp, ctxt);

    if(deserializedUser instanceof IAfterDeserialization)
    	((IAfterDeserialization) deserializedUser).afterDeserialization();
    
    // Special logic

    return deserializedUser;
  }

  // for some reason you have to implement ResolvableDeserializer when modifying BeanDeserializer
  // otherwise deserializing throws JsonMappingException??
  @Override public void resolve(DeserializationContext ctxt) throws JsonMappingException
  {
    ((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
  }

}
