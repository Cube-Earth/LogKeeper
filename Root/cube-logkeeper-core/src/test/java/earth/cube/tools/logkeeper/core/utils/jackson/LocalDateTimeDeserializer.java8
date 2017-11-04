package earth.cube.tools.logkeeper.core.utils.jackson;

import java.io.IOException;
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import earth.cube.tools.logkeeper.core.Globals;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

	@Override
	public LocalDateTime deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JsonProcessingException {
		String s = parser.getValueAsString();
		return (s != null && s.length() != 0) ? LocalDateTime.parse(s, Globals.DTF) : null;
	}
}
