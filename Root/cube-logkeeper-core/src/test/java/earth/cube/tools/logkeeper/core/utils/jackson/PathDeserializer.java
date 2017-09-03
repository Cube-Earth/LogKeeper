package earth.cube.tools.logkeeper.core.utils.jackson;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class PathDeserializer extends JsonDeserializer<Path> {

	@Override
	public Path deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JsonProcessingException {
		String s = parser.getValueAsString();
		return (s != null && s.length() != 0) ? Paths.get(s) : null;
			
	}
}
