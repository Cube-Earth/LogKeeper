package earth.cube.tools.logkeeper.watcher.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.text.WordUtils;

public class BeanUtil {
	
	public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS");
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void set(Object obj, String sPropertyName, Object value) {
		Method methWithString = null;
		Method methWithOrigin = null;
		Method methWithAny = null;
		Class<?> origin = value != null ? value.getClass() : null;
		String sMethodName = "set" + WordUtils.capitalize(sPropertyName);
		for(Method m : obj.getClass().getDeclaredMethods()) {
			Class<?>[] p = m.getParameterTypes();
			if(p.length == 1 && m.getName().equals(sMethodName)) {
				if(p[0].equals(String.class))
						methWithString = m;
				else
					if(p[0].equals(origin))
						methWithOrigin = m;
					else
						methWithAny = m;
			}
		}
		try {
			if(methWithOrigin != null)
				methWithOrigin.invoke(obj, value);
			else
				if(methWithString != null)
					methWithString.invoke(obj, value == null ? null : value.toString());
				else {
					Object v = null;
					if(value != null) {
						Class<?> p = methWithAny.getParameterTypes()[0];
						if(LocalDateTime.class.isAssignableFrom(p))
							v = LocalDateTime.parse(value.toString(), DTF);
						else
							if(Path.class.isAssignableFrom(p))
								v = Paths.get(value.toString());
							else
								if(p.isEnum())
									v = Enum.valueOf((Class<Enum>) p, value.toString().toUpperCase());
								else
									throw new IllegalStateException("no appropriate setter found for property '" + sPropertyName + "'!");
					}
					methWithAny.invoke(obj, v);
				}
		}
		catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}

}
