package earth.cube.tools.logkeeper.core.utils.jackson;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import earth.cube.tools.logkeeper.core.Globals;

public class DateSerializer extends JsonSerializer<Date> {

	@Override
	public void serialize(Date d, JsonGenerator gen, SerializerProvider provider) throws IOException, JsonProcessingException {
		if(d != null)
			gen.writeString(Globals.DTF.format(d));
	}
}
