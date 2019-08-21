package earth.cube.tools.logkeeper.pipe_sender.utils;

public class Producer {
	
	public static String get(Object obj) {
		Class<?> c = obj.getClass();
		String s = c.getCanonicalName();
		int i = s.lastIndexOf('.');
		i = s.lastIndexOf('.', i-1);
		return s.substring(i+1);
	}

}
