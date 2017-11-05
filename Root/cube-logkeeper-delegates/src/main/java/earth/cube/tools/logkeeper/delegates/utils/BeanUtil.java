package earth.cube.tools.logkeeper.delegates.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BeanUtil {
	
	public static void invoke(Object obj, String sMethodName) {
		try {
			Class<?> c = obj instanceof Class ? (Class<?>) obj : obj.getClass();
			Method m = c.getDeclaredMethod(sMethodName);
			m.invoke(obj);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}

}
