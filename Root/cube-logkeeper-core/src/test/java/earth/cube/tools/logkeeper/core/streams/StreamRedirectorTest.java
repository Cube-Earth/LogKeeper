package earth.cube.tools.logkeeper.core.streams;

import java.util.concurrent.Semaphore;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import earth.cube.tools.logkeeper.core.LogLevel;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.forwarders.LogDispatcher;
import earth.cube.tools.logkeeper.core.forwarders.TestForwarder;
import earth.cube.tools.logkeeper.core.streams.LoggingOutputStream;
import earth.cube.tools.logkeeper.core.streams.StreamRedirector;
import earth.cube.tools.logkeeper.core.streams.StreamType;

public class StreamRedirectorTest {

	static {
		System.setProperty("logkeeper.agent", TestForwarder.class.getCanonicalName());
		System.setProperty("logkeeper.application", "app0");
	}
	
	
	private Semaphore _s1 = new Semaphore(0);
	private Semaphore _s2 = new Semaphore(0);
	private Semaphore _s3 = new Semaphore(0);
	
	private class Thread1 extends Thread {
		
		public Thread1() {
			super("thread1");
		}
		
		@Override
		public void run() {
			try {
				System.out.println("a1");     // Step 1
				_s1.release();
				_s2.acquire();
				System.out.println(" a2");   // Step 3
				System.err.println("c1");
				_s1.release();
				_s2.acquire();
				System.out.println(" a3");   // Step 5
				System.out.println("d1");
				_s1.release();
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}
	}
	
	private class Thread2 extends Thread {
		
		public Thread2() {
			super("thread2");
		}
		
		@Override
		public void run() {
			try {
				_s1.acquire();
				System.out.println("b1");  // Step 2
				System.out.println(" b2");
				_s2.release();
				_s1.acquire();
				System.out.println(" b3");  // Step 4
				System.err.println("e1");
				_s2.release();
				_s1.acquire();
				System.out.println(" b4");  // Step 6
				System.out.println("f1");
				_s3.release();
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}
	}
	
	
	@BeforeClass
	public static void setUpClass() {
		StreamRedirector.setHouseKeeperWaitTime(1000);
		StreamRedirector.set();
	}
	
	@AfterClass
	public static void tearDownClass() {
		StreamRedirector.unset();
	}
	
	
	@Before
	public void setUp() throws InterruptedException {
		StreamRedirector.flush();
		waitForwarded();
		TestForwarder.clear();
	}
	
	
	
	
	
	private void waitForwarded() throws InterruptedException {
		Thread.sleep(200);
	}
	
	private void check(String sExpectedTreadName, StreamType expectedStreamType, LogLevel expectedLevel, String sExpectedMsg, LogMessage m) {
		Assert.assertEquals("app0", m.getApplication());
		Assert.assertEquals(expectedStreamType.toString().toLowerCase(), m.getSource());
		Assert.assertEquals(expectedStreamType.toString().toUpperCase(), m.getLoggerName());
		Assert.assertEquals(LoggingOutputStream.class.getSimpleName(), m.getProducer());
		Assert.assertEquals("json", m.getType());
		Assert.assertNull(m.getFilePath());
		Assert.assertEquals(m.getMessage(), sExpectedTreadName, m.getThread());
		Assert.assertEquals(m.getMessage(), expectedLevel, m.getLevel());
		if(sExpectedMsg.startsWith("%"))
			Assert.assertTrue(m.getMessage(), m.getMessage().contains(sExpectedMsg.substring(1)));
		else
			Assert.assertEquals(sExpectedMsg, m.getMessage());
		Assert.assertNull(m.getThrowable());
	}
	
	@Test
	public void test_one_line_stdout_1() throws InterruptedException {
		System.out.println("abc");
		waitForwarded();
		Assert.assertEquals(0, TestForwarder.size());
		Thread.sleep(2000);
		Assert.assertEquals(1, TestForwarder.size());
		check("main", StreamType.STDOUT, LogLevel.INFO, "abc", TestForwarder.pop());
	}

	@Test
	public void test_one_line_stdout_2() throws InterruptedException {
		System.out.println(" abc");
		waitForwarded();
		Assert.assertEquals(0, TestForwarder.size());
		Thread.sleep(2000);
		Assert.assertEquals(1, TestForwarder.size());
		check("main", StreamType.STDOUT, LogLevel.INFO, " abc", TestForwarder.pop());
	}

	@Test
	public void test_multiple_lines_stdout_1() throws InterruptedException {
		System.out.println("abc");
		System.out.println(" def");
		System.out.println(" ghi");
		System.out.println("jkl");
		waitForwarded();
		Assert.assertEquals(1, TestForwarder.size());
		Thread.sleep(2000);
		Assert.assertEquals(2, TestForwarder.size());
		check("main", StreamType.STDOUT, LogLevel.INFO, "abc\n def\n ghi", TestForwarder.pop());
		check("main", StreamType.STDOUT, LogLevel.INFO, "jkl", TestForwarder.pop());
	}

	@Test
	public void test_multiple_lines_stderr_1() throws InterruptedException {
		System.err.println("abc");
		System.err.println(" def");
		System.err.println(" ghi");
		System.err.println("jkl");
		waitForwarded();
		Assert.assertEquals(1, TestForwarder.size());
		Thread.sleep(2000);
		Assert.assertEquals(2, TestForwarder.size());
		check("main", StreamType.STDERR, LogLevel.INFO, "abc\n def\n ghi", TestForwarder.pop());
		check("main", StreamType.STDERR, LogLevel.INFO, "jkl", TestForwarder.pop());
	}

	public void test_multiple_lines_stdouterr_1() throws InterruptedException {
		System.out.println("abc");
		System.err.println("def");
		System.out.println(" ghi");
		waitForwarded();
		Assert.assertEquals(0, TestForwarder.size());
		Thread.sleep(2000);
		Assert.assertEquals(2, TestForwarder.size());
		check("main", StreamType.STDOUT, LogLevel.INFO, "abc\n ghi", TestForwarder.pop());
		check("main", StreamType.STDERR, LogLevel.INFO, "def", TestForwarder.pop());
	}

	@Test
	public void test_threaded_1() throws InterruptedException {
		new Thread1().start();
		new Thread2().start();
		_s3.acquire();
		
		waitForwarded();
		Assert.assertEquals(2, TestForwarder.size());
		Thread.sleep(2000);
		Assert.assertEquals(6, TestForwarder.size());

		check("thread1", StreamType.STDOUT, LogLevel.INFO, "a1\n a2\n a3", TestForwarder.pop());
		check("thread2", StreamType.STDOUT, LogLevel.INFO, "b1\n b2\n b3\n b4", TestForwarder.pop());
		check("thread1", StreamType.STDOUT, LogLevel.INFO, "d1", TestForwarder.pop());
		check("thread2", StreamType.STDOUT, LogLevel.INFO, "f1", TestForwarder.pop());
		check("thread1", StreamType.STDERR, LogLevel.INFO, "c1", TestForwarder.pop());
		check("thread2", StreamType.STDERR, LogLevel.INFO, "e1", TestForwarder.pop());
	}
	
	
	@Test
	public void test_recursive_1() throws InterruptedException {
		System.out.println("abc");
		System.out.println("abc");
		waitForwarded();
		Assert.assertEquals(1, TestForwarder.size());
		Thread.sleep(2000);
		Assert.assertEquals(2, TestForwarder.size());
		check("main", StreamType.STDOUT, LogLevel.WARN, "%recursion", TestForwarder.pop());
		check("main", StreamType.STDOUT, LogLevel.INFO, "abc", TestForwarder.pop());
	}

	@Test
	public void test_exception_1() throws InterruptedException {
		new IllegalStateException().printStackTrace();
		waitForwarded();
		Assert.assertEquals(0, TestForwarder.size());
		Thread.sleep(2000);
		Assert.assertEquals(1, TestForwarder.size());
		
		LogMessage msg = TestForwarder.pop();
		check("main", StreamType.STDERR, LogLevel.ERROR, "%IllegalStateException", msg);
		Assert.assertTrue(msg.getMessage(), msg.getMessage().matches("java.lang.IllegalStateException(\n\\s+at\\s+.*)+"));
	}

}
