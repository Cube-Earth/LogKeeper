package earth.cube.tools.logkeeper.delegates.streams;

import earth.cube.tools.logkeeper.delegates.utils.BeanUtil;
import earth.cube.tools.logkeeper.delegates.utils.ClassLocator;

public class StreamRedirector {

	private final static String DELEGATED_CLASS_NAME = "earth.cube.tools.logkeeper.core.streams.StreamRedirector";
	
	public static void set() {
		Class<?> c = ClassLocator.loadClass(DELEGATED_CLASS_NAME);
		BeanUtil.invoke(c, "set");
	}

	public static void unset() {
		Class<?> c = ClassLocator.loadClass(DELEGATED_CLASS_NAME);
		BeanUtil.invoke(c, "unset");
	}

}
