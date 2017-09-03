package earth.cube.tools.logkeeper.watcher.utils.jackson;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.text.StrSubstitutor;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import earth.cube.tools.logkeeper.watcher.utils.strlookups.EnvLookup;

public class PathDeserializer extends JsonDeserializer<Path> {

	@Override
	public Path deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		String s = jsonParser.getText();
		return s == null || s.length() == 0 ? null : Paths.get(new StrSubstitutor(new EnvLookup()).replace(s));
	}
}
