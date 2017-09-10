package earth.cube.tools.logkeeper.tomcat.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import earth.cube.tools.logkeeper.core.LogLevel;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.forwarders.LogDispatcher;
import earth.cube.tools.logkeeper.core.streams.StreamRedirector;
import earth.cube.tools.logkeeper.core.test.zmq.TestZmqServer;

public class TomcatTest {
	
	private static final int PORT = 8085;
	
	private static Path _catalinaHome = Paths.get(System.getenv("CATALINA_HOME") == null ? "/Applications/servers/apache-tomcat-8.5.9" : System.getenv("CATALINA_HOME"));
	private static Path _catalinaBase = Paths.get("./src/main/tomcat");
	private static Path _tempRootDir = Paths.get("./bin/tomcat");
	private static List<URL> _urls1 = new ArrayList<>();
	private static List<URL> _urls2 = new ArrayList<>();
	private static DispatchActionThread _startThread;
	private static DispatchActionThread _stopThread;

	private static TestZmqServer _zmqSvr;
	
	private static void setProps() {
		File tempDir = new File(_tempRootDir.toFile(), "temp");
		tempDir.mkdirs();
		
		System.setProperty("logkeeper.application", "Tomcat");

		System.setProperty("catalina.base", _catalinaBase.toString());
		System.setProperty("catalina.home", _catalinaHome.toString());
		System.setProperty("project.root.dir", new File(".").getAbsolutePath());
		System.setProperty("java.endorsed.dirs", Paths.get(_catalinaHome.toString(), "endorsed").toString());	
		System.setProperty("java.protocol.handler.pkgs", "org.apache.catalina.webresources");
		System.setProperty("java.util.logging.config.file", Paths.get(_catalinaBase.toString(), "/conf/logging.properties").toString());
		System.setProperty("java.util.logging.manager", "org.apache.juli.ClassLoaderLogManager");
		System.setProperty("java.io.tmpdir", tempDir.getAbsolutePath());
//		System.setProperty("java.security.manager", "");
//		System.setProperty("java.security.policy", Paths.get(_catalinaBase.toString(), "/conf/catalina.policy").toString());
	}
	
	private static void setUrls() throws MalformedURLException {
//		_urls2.add(Paths.get(_catalinaHome.toString(), "/bin/bootstrap.jar").toUri().toURL());
		_urls1.add(Paths.get(_catalinaHome.toString(), "/bin/bootstrap.jar").toUri().toURL());
		_urls1.add(Paths.get(_catalinaHome.toString(), "/bin/tomcat-juli.jar").toUri().toURL());
	}
	
	protected static class DispatchActionThread extends Thread {
		
		private String _sAction;

		public DispatchActionThread(String sAction) {
			super(sAction);
			_sAction = sAction;
		}

		@Override
		public void run() {
			URLClassLoader cl = null;
			try {
				Class<?> bootstrapClass;
				if(_urls2.size() > 0) {
					cl = new URLClassLoader(_urls2.toArray(new URL[_urls2.size()]), ClassLoader.getSystemClassLoader());
					bootstrapClass = cl.loadClass("org.apache.catalina.startup.Bootstrap");
				}
				else
					bootstrapClass = Class.forName("org.apache.catalina.startup.Bootstrap");
				Object bootstrap = bootstrapClass.newInstance();
				Method m = bootstrapClass.getDeclaredMethod("main", String[].class);
				m.invoke(bootstrap, new Object[] { new String[] { _sAction } });
			} catch (Throwable t) {
				t.printStackTrace();
			}
			finally {
				if(cl != null)
					try {
						cl.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
	}
	
	private static void addLibraryToClassPath(URL... urls) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	    Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
	    method.setAccessible(true);
	    for (URL url : urls)
	    	method.invoke(ClassLoader.getSystemClassLoader(), url);
	}
	
	private static URL getJarUrl(URL entryUrl) throws MalformedURLException {
		String sUrl = null;
		if(entryUrl.getProtocol().equals("jar")) {
			sUrl = entryUrl.getPath();
			int i = sUrl.indexOf('!');
			if(i != -1) {
				sUrl = sUrl.substring(0, i);
			}
		}
		else
			if(entryUrl.getProtocol().equals("file")) {
				sUrl = entryUrl.toString();
				int i = sUrl.indexOf("/bin/");  // TODO: quick hack
				if(i != -1) {
					sUrl = sUrl.substring(0, i + 4);
				}
			}
			else
				throw new IllegalStateException("Unsupported scheme: " + entryUrl);
		return new URL(sUrl);
	}
	

	private static void addLibraryToClassPath(File file) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, MalformedURLException {
		addLibraryToClassPath(file.toURI().toURL());
	}

	private static void addLibrariesToClassPath(Class<?>... classes) throws MalformedURLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		URL[] urls = new URL[classes.length];
		for(int i = 0; i < urls.length; i++) {
			urls[i] = getJarUrl(classes[i].getClassLoader().getResource(classes[i].getCanonicalName().replace('.', '/') + ".class"));
		}
		addLibraryToClassPath(urls);
	}
	
	private static void init() throws MalformedURLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		setProps();
		setUrls();
		addLibraryToClassPath(_urls1.toArray(new URL[_urls1.size()]));
//		addLibrariesToClassPath(ForwardHandler.class, LogMessage.class, ZMQ.class, JsonFactory.class, JsonDeserializer.class, JacksonAnnotation.class);
	}
	
	private static void start() throws InterruptedException {
		_startThread = new DispatchActionThread("start");
		_startThread.start();
	}
	
	private static void stop() throws InterruptedException {
		_stopThread = new DispatchActionThread("stop");
		_stopThread.start();
		_stopThread.join();
		_startThread.join();
	}
	
	private static boolean checkRunning() throws IOException {
		boolean bRunning;
		URL url = new URL(String.format("http://127.0.0.1:%s/status.jsp", PORT));
		URLConnection conn = url.openConnection();
		conn.setUseCaches(false);
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader (conn.getInputStream(), "iso-8859-1"));
			String s = in.readLine();
			Assert.assertEquals("OK", s);
			bRunning = true;
		}
		catch(ConnectException e) {
			bRunning = false;
		}
		finally {
			if(in != null)
				in.close();
		}
		return bRunning;
	}
	
	private static void await() throws IOException, InterruptedException {
		try {
			for(int i = 0; !checkRunning(); i++) {
				if(i > 5)
					Assert.fail("Tomcat startup seemed beeing failed");
				Thread.sleep(500);
			}
		}
		catch(Throwable t) {
			t.printStackTrace();
			throw new IOException(t);
		}
	}
	
	private static void printLogMessages() throws IOException {
		URL url = new URL(String.format("http://127.0.0.1:%s/printLogMessages.jsp", PORT));
		URLConnection conn = url.openConnection();
		conn.setUseCaches(false);
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader (conn.getInputStream(), "iso-8859-1"));
			String s = in.readLine();
			Assert.assertEquals("OK", s);
		}
		finally {
			if(in != null)
				in.close();
		}
	}

	private void printCompileException() throws IOException {
		URL url = new URL(String.format("http://127.0.0.1:%s/printCompileException.jsp", PORT));
		URLConnection conn = url.openConnection();
		conn.setUseCaches(false);
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader (conn.getInputStream(), "iso-8859-1"));
			in.readLine();
			Assert.fail();
		}
		catch(IOException e) {
			if(!e.getMessage().contains(" response code: 500 "))
				throw e;
		}
		finally {
			if(in != null)
				in.close();
		}
	}
	
	@BeforeClass
	public static void setUp() throws InterruptedException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		try {
			_zmqSvr = new TestZmqServer();
			_zmqSvr.start();
			init();
			
			StreamRedirector.setHouseKeeperWaitTime(1000);
			StreamRedirector.set();
			
			start();
			await();
			
			printLogMessages();
			Thread.sleep(2000);
		}
		catch(Throwable t) {
			t.printStackTrace();
			throw new IOException(t);
		}
	}

	@AfterClass
	public static void tearDown() throws InterruptedException, IOException {
		stop();
		_zmqSvr.close();
	}
	
	private LogMessage findMessage(String sMarker) throws InterruptedException {
		LogDispatcher.flush();
		Thread.sleep(500);
		return _zmqSvr.findMessage(sMarker);
	}
	
	@Test
	public void test_localhost_1() throws IOException, InterruptedException {
		LogMessage msg = findMessage("{3}");
		Assert.assertEquals("Tomcat", msg.getApplication());
		Assert.assertEquals("localhost", msg.getSource());
		Assert.assertEquals(LogLevel.INFO, msg.getLevel());
		Assert.assertEquals("org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/]", msg.getLoggerName());
		Assert.assertEquals("message to servlet log {3}", msg.getMessage());
		Assert.assertNull(msg.getThrowableStackTrace());
	}

	@Test
	public void test_localhost_error_1() throws IOException, InterruptedException {
		printCompileException();

		LogMessage msg = findMessage("Unable to compile class for JSP");
		Assert.assertEquals("Tomcat", msg.getApplication());
		Assert.assertEquals("localhost", msg.getSource());
		Assert.assertEquals(LogLevel.ERROR, msg.getLevel());
		Assert.assertEquals("org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/].[jsp]", msg.getLoggerName());
		Assert.assertTrue(msg.getMessage().contains("error"));
		Assert.assertTrue(msg.getThrowableStackTrace().contains("JasperException"));
	}
	@Test

	public void test_catalina_1() throws IOException, InterruptedException {
		LogMessage msg = findMessage("Server startup in");
		Assert.assertEquals("Tomcat", msg.getApplication());
		Assert.assertEquals("catalina", msg.getSource());
		Assert.assertEquals(LogLevel.INFO, msg.getLevel());
		Assert.assertEquals("org.apache.catalina.startup.Catalina", msg.getLoggerName());
		Assert.assertTrue(msg.getMessage().matches("Server startup in \\d+ ms"));
		Assert.assertNull(msg.getThrowableStackTrace());
	}
	
	@Test
	public void test_stdout_1() throws IOException, InterruptedException {
		LogMessage msg = findMessage("{1}");
		Assert.assertEquals("Tomcat", msg.getApplication());
		Assert.assertEquals("stdout", msg.getSource());
		Assert.assertEquals(LogLevel.INFO, msg.getLevel());
		Assert.assertEquals("STDOUT", msg.getLoggerName());
		Assert.assertEquals("message to system out {1}", msg.getMessage());
		Assert.assertNull(msg.getThrowableStackTrace());
	}

	@Test
	public void test_stderr_1() throws IOException, InterruptedException {
		LogMessage msg = findMessage("{2}");
		Assert.assertEquals("Tomcat", msg.getApplication());
		Assert.assertEquals("stderr", msg.getSource());
		Assert.assertEquals(LogLevel.INFO, msg.getLevel());
		Assert.assertEquals("STDERR", msg.getLoggerName());
		Assert.assertEquals("message to system err {2}", msg.getMessage());
		Assert.assertNull(msg.getThrowableStackTrace());
	}
}
