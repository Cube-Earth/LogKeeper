package earth.cube.tools.logkeeper.core.utils.jackson;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import earth.cube.tools.logkeeper.core.Globals;

public class DateDeserializer extends JsonDeserializer<Date> {

	@Override
	public Date deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JsonProcessingException {
		String s = parser.getValueAsString();
		try {
			return (s != null && s.length() != 0) ? Globals.DTF.parse(s) : null;
		} catch (ParseException e) {
			throw new IOException(e);
		}
	}
}
