package earth.cube.tools.logkeeper.web.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.Assert;
import org.yaml.snakeyaml.Yaml;
import org.zeromq.ZMQ;

import com.fasterxml.jackson.annotation.JacksonAnnotation;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonDeserializer;

import earth.cube.tools.logkeeper.core.LogLevel;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.test.zmq.TestZmqServer;
import earth.cube.tools.logkeeper.loggers.java_logging.ForwardHandler;

public class StandardLogger {

	public StandardLogger() {
	}
	
	private void initLogger() {
		final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("logging.properties");
		try
		{
		    LogManager.getLogManager().readConfiguration(is);
		}
		catch (final IOException e)
		{
		    Logger.getAnonymousLogger().log(Level.SEVERE, "Could find logging.properties file!", e);
		}		
	}
	
	
	private URL getJarUrl(URL entryUrl) throws MalformedURLException {
		if(!entryUrl.getProtocol().equals("jar"))
			throw new IllegalStateException("JAR expected!");
		String s = entryUrl.getPath();
		int i = s.indexOf('!');
		if(i != -1) {
			s = s.substring(0, i);
		}
		return new URL(s);
	}
	

	private void addLibraryToClassPath(URL... urls) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	    Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
	    method.setAccessible(true);
	    for (URL url : urls)
	    	method.invoke(ClassLoader.getSystemClassLoader(), url);
	}
	
	private void addLibraryToClassPath(File file) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, MalformedURLException {
		addLibraryToClassPath(file.toURI().toURL());
	}

	private void addLibrariesToClassPath(Class<?>... classes) throws MalformedURLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		URL[] urls = new URL[classes.length];
		for(int i = 0; i < urls.length; i++) {
			urls[i] = getJarUrl(classes[i].getClassLoader().getResource(classes[i].getCanonicalName().replace('.', '/') + ".class"));
		}
		addLibraryToClassPath(urls);
	}
	
	public void log() throws MalformedURLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		addLibrariesToClassPath(ForwardHandler.class, LogMessage.class, ZMQ.class, JsonFactory.class, JsonDeserializer.class, JacksonAnnotation.class);
		
		initLogger();
		Logger log = Logger.getLogger(getClass().getName());
		log.fine("log: message to java util logger {4}");
		log.log(Level.SEVERE, "log: message to java util logger {5}", new IllegalStateException());
	}
	
	
	public void check(TestZmqServer svr) {
		LogMessage msg = svr.findMessageAndPurge("{4}");
		Assert.assertEquals(LogLevel.DEBUG, msg.getLevel());
		Assert.assertEquals("log: message to java util logger {4}", msg.getMessage());
		Assert.assertNull(msg.getThrowable());
		Assert.assertNull(msg.getThrowableStackTrace());
		Assert.assertEquals("tomcat-logging", msg.getSource());
		
		msg = svr.findMessageAndPurge("{5}");
		Assert.assertEquals(LogLevel.ERROR, msg.getLevel());
		Assert.assertEquals("log: message to java util logger {5}", msg.getMessage());
		Assert.assertNull(msg.getThrowable());
		Assert.assertTrue(msg.getThrowableStackTrace(), msg.getThrowableStackTrace().matches(".*IllegalStateException(\n.*)*"));
		Assert.assertEquals("tomcat-logging", msg.getSource());
	}

}
