package earth.cube.tools.logkeeper.loggers.java_logging.delegates;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ForwardHandler extends Handler {

	private final static String DELEGATED_CLASS_NAME = "earth.cube.tools.logkeeper.loggers.java_logging.ForwardsHandler";
	
	private Handler _handler;

	public ForwardHandler() {
		createHandler();
	}
	
	private File challengeDir(String... saDir) {
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
	
	private URL[] getJarUrls(File libDir) {
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
	
	private ClassLoader createClassLoader() {
		File libDir = challengeDir(
				System.getProperty("logkeeper.lib_dir"),
				System.getenv("LOGKEEPER_LIBDIR"),
				System.getProperty("catalina.base") + "/bin/logkeeper",
				System.getProperty("catalina.home") + "/bin/logkeeper");
		if(libDir == null)
			throw new IllegalStateException("Could not determine directory of logkeeper-related libraries!");
		URL[] jars = getJarUrls(libDir);	
		return new URLClassLoader(jars, getClass().getClassLoader());
	}
	
	private void createHandler() {
		try {
			ClassLoader cl = createClassLoader();
			@SuppressWarnings("unchecked")
			Class<Handler> clazz = (Class<Handler>) cl.loadClass(DELEGATED_CLASS_NAME);
			Constructor<Handler> c = clazz.getDeclaredConstructor(String.class);
			_handler = c.newInstance(getClass().getName());
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
		
	}
	

	/* (non-API documentation)
	 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
	 */
	public void publish(LogRecord record) {
		_handler.publish(record);
	}

	/* (non-API documentation)
	 * @see java.util.logging.Handler#flush()
	 */
	public void flush() {
		_handler.flush();
	}

	/* (non-API documentation)
	 * @see java.util.logging.Handler#close()
	 */
	public void close() throws SecurityException {
		_handler.close();
	}
}