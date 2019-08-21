package earth.cube.tools.logkeeper.watcher;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import earth.cube.tools.logkeeper.core.LogLevel;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.forwarders.TestForwarder;
import earth.cube.tools.logkeeper.pipe_sender.FileManager;
import earth.cube.tools.logkeeper.pipe_sender.loggers.ScratchPipe;
import earth.cube.tools.logkeeper.watcher.config.Config;
import earth.cube.tools.logkeeper.watcher.config.ConfigTest;
import earth.cube.tools.logkeeper.watcher.config.ILogConfig;
import earth.cube.tools.logkeeper.watcher.config.LogConfigStructuredPipe;
import earth.cube.tools.logkeeper.watcher.config.LogConfigTextPipe;
import earth.cube.tools.logkeeper.watcher.health_check.HealthStatus;
import earth.cube.tools.logkeeper.watcher.utils.FileUtil;

public class PipeSenderTest {

	private static Config _config;
	
	private static Path _logDir;

	private static Path _structuredPipeFile;

	private static Path _textPipeFile;

	private static int _nPort;

	private static Path _stateFile;

	private static Path _configFile;

	private static Watcher _watcher;
	
	
	protected static class Watcher extends Thread {
		
		private Application _app;
		
		public Watcher() throws IOException {
			_app = new Application(_configFile, null, 1000);
		}

		@Override
		public void run() {
			try {
				_app.run();
			} catch (IOException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		
		public void close() throws IOException, InterruptedException {
			_app.close();
		}
		
	}

	
	@BeforeClass
	public static void setUpClass() throws IOException {
		System.setProperty("logkeeper.agent", TestForwarder.class.getCanonicalName());
		System.setProperty("logkeeper.application", "app0");

		_logDir = Paths.get(Files.createTempDirectory("junit-" + LogTest.class.getSimpleName()).toString(), "logkeeper.tmp");
		Files.createDirectories(_logDir);
		// TODO delete _logdir recursively
		
		System.setProperty("LOG_DIR", _logDir.toString());
		String sResName = PipeSenderTest.class.getPackage().getName().replace('.', '/') + "/LogKeeper-PipeSender.yml";
		_config = Config.read(PipeSenderTest.class.getClassLoader().getResourceAsStream(sResName));
		Assert.assertNotNull(_config);
		_configFile = FileUtil.toFile(ConfigTest.class.getClassLoader().getResource(sResName)).toPath();
		
		_watcher = new Watcher();
		_watcher.start();
		
		for(ILogConfig logConfig : _config.getLogConfigs())
			switch(logConfig.getConfigType()) {
				case PIPE_STRUCTURED:
					_structuredPipeFile = ((LogConfigStructuredPipe) logConfig).getPath();
					break;
					
				case PIPE_TEXT:
					_textPipeFile = ((LogConfigTextPipe) logConfig).getPath();
					break;

				default:
					break;
			}
		
		_nPort = _config.getHealthConfig().getPort();
		
		_stateFile = _config.getHealthConfig().getStateFile();
	}
	
	@AfterClass
	public static void tearDown() throws IOException, InterruptedException {
		if(_watcher != null)
			_watcher.close();
	}
	
	private void waitForwarded() throws InterruptedException {
		Thread.sleep(700);
	}

	@Before
	public void setUp() throws InterruptedException {
		waitForwarded();
		TestForwarder.clear();
	}
	
	protected void checkHealth(boolean bExpectedHealth) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:" + _nPort + "/health").openConnection();
		conn.connect();
		conn.getContent();
		Assert.assertEquals(bExpectedHealth ? 200 : 500, conn.getResponseCode());
		
		Assert.assertEquals(bExpectedHealth, !Files.exists(_stateFile));
	}
	
	protected void resetHealth() throws IOException {
		HealthStatus.reset();
		
		if(Files.exists(_stateFile))
			Files.delete(_stateFile);
	}
	
	protected Date getDate(int nYear, int nMonth, int nDay, int nHour, int nMinute, int nSecond) {
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(nYear, nMonth, nDay, nHour, nMinute, nSecond);
		return c.getTime();
	}
	
	private void check(LogLevel expectedLevel, String sExpectedMsg, LogMessage m) {
		Assert.assertEquals("app0", m.getApplication());
		Assert.assertEquals("stdin", m.getSource());
		Assert.assertEquals("main", m.getLoggerName());
		Assert.assertEquals("LogKeeper-StdIn", m.getProducer());
		Assert.assertEquals("server log", m.getType());
		Assert.assertNull(m.getFilePath());
		Assert.assertEquals(m.getMessage(), "main", m.getThread());
		Assert.assertEquals(m.getMessage(), expectedLevel, m.getLevel());
		if(sExpectedMsg.startsWith("%"))
			Assert.assertTrue(m.getMessage(), m.getMessage().contains(sExpectedMsg.substring(1)));
		else
			Assert.assertEquals(sExpectedMsg, m.getMessage());
		Assert.assertNull(m.getThrowable());
	}
	
	@Test
	public void test_structuredPipe_send_1() throws IOException, InterruptedException {
		resetHealth();
		ScratchPipe pipe = new ScratchPipe(_structuredPipeFile.toFile());

		checkHealth(true);
		pipe.send("abc");
		pipe.send("def");
		
		waitForwarded();
		
		Assert.assertEquals(2, TestForwarder.size());
		
		LogMessage msg;
		msg = TestForwarder.pop();
		Assert.assertEquals("abc", msg.getMessage());
		
		msg = TestForwarder.pop();
		Assert.assertEquals("def", msg.getMessage());
	}
	
	@Test
	public void test_textPipe_send_1() throws IOException, InterruptedException {
		resetHealth();

		checkHealth(true);
		FileManager.INSTANCE.write(_textPipeFile.toFile(), "2019-08-01 12:00:00 INFO abc", "utf-8");
		FileManager.INSTANCE.write(_textPipeFile.toFile(), "2019-08-02 13:30:15 DEBUG def", "utf-8");
		
		waitForwarded();

		Assert.assertEquals(1, TestForwarder.size());
		Thread.sleep(2000);
		Assert.assertEquals(2, TestForwarder.size());
		
		LogMessage msg;
		msg = TestForwarder.pop();
		Assert.assertEquals("abc", msg.getMessage());
		Assert.assertEquals(LogLevel.INFO, msg.getLevel());
		Assert.assertEquals(getDate(2019, 7, 1, 12, 0, 0), msg.getDate());

		msg = TestForwarder.pop();
		Assert.assertEquals("def", msg.getMessage());
		Assert.assertEquals(LogLevel.DEBUG, msg.getLevel());
		Assert.assertEquals(getDate(2019, 7, 2, 13, 30, 15), msg.getDate());

	}
	
}
