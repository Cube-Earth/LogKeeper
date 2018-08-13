package earth.cube.tools.logkeeper.watcher.utils.jackson;

import java.io.IOException;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class PatternDeserializer extends JsonDeserializer<Pattern> {

	@Override
	public Pattern deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		String s = jsonParser.getText();
		return s == null || s.length() == 0 ? null : Pattern.compile(s, Pattern.CASE_INSENSITIVE);
	}
}
