package earth.cube.tools.logkeeper.core.utils.jackson;

import java.io.IOException;
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import earth.cube.tools.logkeeper.core.Globals;

public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

	@Override
	public void serialize(LocalDateTime dt, JsonGenerator gen, SerializerProvider provider) throws IOException, JsonProcessingException {
		if(dt != null)
			gen.writeString(Globals.DTF.format(dt));
	}
}
