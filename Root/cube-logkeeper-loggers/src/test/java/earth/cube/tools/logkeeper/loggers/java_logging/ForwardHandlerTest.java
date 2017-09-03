package earth.cube.tools.logkeeper.loggers.java_logging;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

import earth.cube.tools.logkeeper.core.LogLevel;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.forwarder.TestForwarder;
import earth.cube.tools.logkeeper.loggers.java_logging.ForwardHandler;

public class ForwardHandlerTest {

	private final Logger _log = Logger.getLogger(getClass().getName());
	
	static {
		System.setProperty("logkeeper.forwarder", TestForwarder.class.getCanonicalName());
		System.setProperty("java.util.logging.config.file", "src/test/resources/logging.properties");
	}
	
	private void waitForwarded() throws InterruptedException {
		Thread.sleep(500);
	}
	
	private void check(LogLevel expectedLevel, String sExpectedMsg, Class<? extends Throwable> expectedThrowableClass, LogMessage m) {
		Assert.assertEquals("Tomcat", m.getApplication());
		Assert.assertEquals("tomcat-stdout", m.getSource());
		Assert.assertEquals(getClass().getName(), m.getLoggerName());
		Assert.assertEquals(ForwardHandler.class.getSimpleName(), m.getProducer());
		Assert.assertEquals("json", m.getType());
		Assert.assertNull(m.getFilePath());
		Assert.assertEquals(Long.toString(Thread.currentThread().getId()), m.getThread());
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

		_log.finest("-finest-");
		waitForwarded();
		Assert.assertEquals(1, TestForwarder.size());

		_log.finer("-finer-");
		waitForwarded();
		Assert.assertEquals(2, TestForwarder.size());

		_log.fine("-fine-");
		waitForwarded();
		Assert.assertEquals(3, TestForwarder.size());

		_log.info("-info-");
		waitForwarded();
		Assert.assertEquals(4, TestForwarder.size());

		_log.warning("-warning-");
		waitForwarded();
		Assert.assertEquals(5, TestForwarder.size());

		_log.severe("-severe-");
		waitForwarded();
		Assert.assertEquals(6, TestForwarder.size());

		_log.log(Level.SEVERE, "-severe2-", new IllegalStateException());
		waitForwarded();
		Assert.assertEquals(7, TestForwarder.size());
		
		check(LogLevel.TRACE, "-finest-", null, TestForwarder.pop());
		check(LogLevel.TRACE, "-finer-", null, TestForwarder.pop());
		check(LogLevel.DEBUG, "-fine-", null, TestForwarder.pop());
		check(LogLevel.INFO, "-info-", null, TestForwarder.pop());
		check(LogLevel.WARN, "-warning-", null, TestForwarder.pop());
		check(LogLevel.ERROR, "-severe-", null, TestForwarder.pop());
		check(LogLevel.ERROR, "-severe2-", IllegalStateException.class, TestForwarder.pop());

		Assert.assertEquals(0, TestForwarder.size());
	}
}
