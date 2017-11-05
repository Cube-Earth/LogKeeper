package earth.cube.tools.logkeeper.loggers.logback;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import earth.cube.tools.logkeeper.core.LogLevel;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.forwarders.TestForwarder;
import earth.cube.tools.logkeeper.loggers.slf4j.ZmqLogger;

public class ForwardAppenderTest {

	private final Logger _log = LoggerFactory.getLogger(getClass().getName());
	
	static {
		System.setProperty("logkeeper.forwarder", TestForwarder.class.getCanonicalName());
	}
	
	private void waitForwarded() throws InterruptedException {
		Thread.sleep(500);
	}
	
	private void check(LogLevel expectedLevel, String sExpectedMsg, Class<? extends Throwable> expectedThrowableClass, LogMessage m) {
		Assert.assertEquals("Tomcat", m.getApplication());
		Assert.assertEquals("tomcat-stdout", m.getSource());
		Assert.assertEquals(getClass().getName(), m.getLoggerName());
		Assert.assertEquals(ZmqLogger.class.getSimpleName(), m.getProducer());
		Assert.assertEquals("json", m.getType());
		Assert.assertNull(m.getFilePath());
		Assert.assertEquals(Thread.currentThread().getName(), m.getThread());
		Assert.assertEquals(expectedLevel, m.getLevel());
		Assert.assertEquals(sExpectedMsg, m.getMessage());
		if(expectedThrowableClass == null)
			Assert.assertNull(m.getThrowable());
		else
			Assert.assertTrue(m.getThrowable().getClass().equals(expectedThrowableClass));
	}
	
	@Test
	public void test_1() throws InterruptedException {
		Assert.assertEquals(0, TestForwarder.size());

		_log.trace("-trace-");
		waitForwarded();
		Assert.assertEquals(1, TestForwarder.size());

		_log.debug("-debug-");
		waitForwarded();
		Assert.assertEquals(2, TestForwarder.size());

		_log.info("-info-");
		waitForwarded();
		Assert.assertEquals(3, TestForwarder.size());

		_log.warn("-warning-");
		waitForwarded();
		Assert.assertEquals(4, TestForwarder.size());

		_log.error("-error-");
		waitForwarded();
		Assert.assertEquals(5, TestForwarder.size());

		_log.error("-error2-", new IllegalStateException());
		waitForwarded();
		Assert.assertEquals(6, TestForwarder.size());
		
		check(LogLevel.TRACE, "-trace-", null, TestForwarder.pop());
		check(LogLevel.DEBUG, "-debug-", null, TestForwarder.pop());
		check(LogLevel.INFO, "-info-", null, TestForwarder.pop());
		check(LogLevel.WARN, "-warning-", null, TestForwarder.pop());
		check(LogLevel.ERROR, "-error-", null, TestForwarder.pop());
		check(LogLevel.ERROR, "-error2-", IllegalStateException.class, TestForwarder.pop());

		Assert.assertEquals(0, TestForwarder.size());

	}
}
