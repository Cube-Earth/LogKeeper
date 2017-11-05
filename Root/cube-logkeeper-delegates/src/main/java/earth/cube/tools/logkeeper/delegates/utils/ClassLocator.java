package earth.cube.tools.logkeeper.delegates.utils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class ClassLocator {
	
	private static final ClassLoader CL = createClassLoader();
	
	private static File challengeDir(String... saDir) {
		for(int i = 0; i < saDir.length; i++) {
			String s = saDir[i];
			if(s != null && s.length() != 0) {
				File dir = new File(s);
				if(dir.exists() && dir.isDirectory())
					return dir;
			}
		}
		return null;
	}
	
	private static URL[] getJarUrls(File libDir) {
		List<URL> urls = new ArrayList<>();
		for(File file : libDir.listFiles())
			if(file.getName().toLowerCase().endsWith(".jar"))
				try {
					urls.add(file.toURI().toURL());
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
		return urls.toArray(new URL[urls.size()]);
	}
	
	private static ClassLoader createClassLoader() {
		File libDir = challengeDir(
				System.getProperty("logkeeper.lib_dir"),
				System.getenv("LOGKEEPER_LIBDIR"),
				System.getProperty("catalina.base") + "/bin/logkeeper",
				System.getProperty("catalina.home") + "/bin/logkeeper");
		if(libDir == null)
			throw new IllegalStateException("Could not determine directory of logkeeper-related libraries!");
		URL[] jars = getJarUrls(libDir);	
		return new URLClassLoader(jars, ClassLocator.class.getClassLoader());
	}
	
	public static ClassLoader getClassLoader() {
		return CL;
	}
	
	public static Object newInstance(String sClassName, Class<?>[] classes, Object[] params) {
		try {
			Class<?> clazz = CL.loadClass(sClassName);
			Constructor<?> c = clazz.getDeclaredConstructor(classes);
			return c.newInstance(params);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public static Object newInstance(String sClassName) {
		try {
			Class<?> clazz = CL.loadClass(sClassName);
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| SecurityException | ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	public static Class<?> loadClass(String sClassName) {
		try {
			return CL.loadClass(sClassName);
		} catch (IllegalArgumentException | SecurityException | ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

}