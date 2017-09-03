package earth.cube.tools.logkeeper.watcher.utils.jackson;

import java.io.IOException;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class FilePermissionsDeserializer extends JsonDeserializer<Set<PosixFilePermission>> {

	private Set<PosixFilePermission> getPermissions(String s) {
		Set<PosixFilePermission> perms = new HashSet<>();
		if(s.length() != 3)
			throw new IllegalArgumentException("Invalid permission '" + s + "'!");
		int j = Integer.parseInt(s, 16);
		
		if((j & 0x400) != 0)
			perms.add(PosixFilePermission.OWNER_READ);
		if((j & 0x200) != 0)
			perms.add(PosixFilePermission.OWNER_WRITE);
		if((j & 0x100) != 0)
			perms.add(PosixFilePermission.OWNER_EXECUTE);
 
		if((j & 0x40) != 0)
			perms.add(PosixFilePermission.GROUP_READ);
		if((j & 0x20) != 0)
	        perms.add(PosixFilePermission.GROUP_WRITE);
		if((j & 0x10) != 0)
	        perms.add(PosixFilePermission.GROUP_EXECUTE);

		if((j & 0x4) != 0)
			perms.add(PosixFilePermission.OTHERS_READ);
		if((j & 0x2) != 0)
			perms.add(PosixFilePermission.OTHERS_WRITE);
		if((j & 0x1) != 0)
			perms.add(PosixFilePermission.OTHERS_EXECUTE);
        
		return perms;
	}
	
	@Override
	public Set<PosixFilePermission> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		String s = jsonParser.getText();
		return s == null || s.length() == 0 ? null : getPermissions(s);
	}
}
