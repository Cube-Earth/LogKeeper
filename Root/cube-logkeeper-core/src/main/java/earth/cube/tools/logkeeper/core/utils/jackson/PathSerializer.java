package earth.cube.tools.logkeeper.core.utils.jackson;

import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class PathSerializer extends JsonSerializer<Path> {

	@Override
	public void serialize(Path path, JsonGenerator gen, SerializerProvider provider) throws IOException, JsonProcessingException {
		if(path != null)
			gen.writeString(path.toString());
	}
}
