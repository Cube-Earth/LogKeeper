package earth.cube.tools.logkeeper.watcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import earth.cube.tools.logkeeper.core.LogLevel;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.forwarders.TestForwarder;
import earth.cube.tools.logkeeper.core.streams.LoggingOutputStream;
import earth.cube.tools.logkeeper.core.streams.StreamType;

public class StdIn_ApplicationTest {

	private static Application _app;
	private static InputStream _oldIn;
	private static PrintStream _out;

	static {
		System.setProperty("logkeeper.agent", TestForwarder.class.getCanonicalName());
//		System.setProperty("logkeeper.application", "app0");
	}
	
	
	@BeforeClass
	public static void setUpClass() throws JsonParseException, JsonMappingException, IOException {
		String sConfigPath = "src/test/java/" + StdIn_ApplicationTest.class.getPackage().getName().replace('.', '/') + "/LogKeeper-StdIn.yml";
		_app = new Application(Paths.get(sConfigPath), null, 1000);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					_app.run();
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (InterruptedException e) {
				}
			}
		}).start();
		_oldIn = System.in;
		
		PassthroughPipe pipe = new PassthroughPipe();
		
		_out = new PrintStream(pipe.getOutputStream());
		System.setIn(pipe.getInputStream());
	}
	
	@AfterClass
	public static void tearDownClass() throws IOException, InterruptedException {
		System.setIn(_oldIn);
		_app.close();
	}
	
	
	@Before
	public void setUp() throws InterruptedException {
		_app.flush();
		waitForwarded();
		TestForwarder.clear();
	}
	
	
	
	private void waitForwarded() throws InterruptedException {
		Thread.sleep(200);
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
	public void test_one_line_stdout_1() throws InterruptedException {
		_out.println("abc");
		waitForwarded();
		Assert.assertEquals(0, TestForwarder.size());
		Thread.sleep(2000);
		Assert.assertEquals(1, TestForwarder.size());
		check(LogLevel.INFO, "abc", TestForwarder.pop());
	}

	@Test
	public void test_one_line_stdout_2() throws InterruptedException {
		_out.println(" abc");
		waitForwarded();
		Assert.assertEquals(0, TestForwarder.size());
		Thread.sleep(2000);
		Assert.assertEquals(1, TestForwarder.size());
		check(LogLevel.INFO, " abc", TestForwarder.pop());
	}

	@Test
	public void test_multiple_lines_stdout_1() throws InterruptedException {
		_out.println("abc");
		_out.println(" def");
		_out.println(" ghi");
		_out.println("jkl");
		_out.flush();
		waitForwarded();
		Assert.assertEquals(1, TestForwarder.size());
		Thread.sleep(2000);
		Assert.assertEquals(2, TestForwarder.size());
		check(LogLevel.INFO, "abc\n def\n ghi", TestForwarder.pop());
		check(LogLevel.INFO, "jkl", TestForwarder.pop());
	}
	
	
	@Test
	public void test_recursive_1() throws InterruptedException {
		_out.println("abc");
		_out.println("abc");
		_out.flush();
		waitForwarded();
		Assert.assertEquals(1, TestForwarder.size());
		Thread.sleep(2000);
		Assert.assertEquals(2, TestForwarder.size());
		check(LogLevel.INFO, "abc", TestForwarder.pop());
		check(LogLevel.INFO, "abc", TestForwarder.pop());
	}

	@Test
	public void test_exception_1() throws InterruptedException {
		new IllegalStateException().printStackTrace(_out);
		_out.flush();
		waitForwarded();
		Assert.assertEquals(0, TestForwarder.size());
		Thread.sleep(2000);
		Assert.assertEquals(1, TestForwarder.size());
		LogMessage msg = TestForwarder.pop();
		check(LogLevel.ERROR, "%IllegalStateException", msg);
		Assert.assertTrue(msg.getMessage(), msg.getMessage().matches("java.lang.IllegalStateException(\n\\s+at\\s+.*)+"));
	}

}
