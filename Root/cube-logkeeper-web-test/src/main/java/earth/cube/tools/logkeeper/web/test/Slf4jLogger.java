package earth.cube.tools.logkeeper.web.test;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import earth.cube.tools.logkeeper.core.LogLevel;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.test.zmq.TestZmqServer;

public class Slf4jLogger {
	
	private final Logger _log = LoggerFactory.getLogger(getClass().getName());

	public void log() {
		_log.debug("log: message to slf4j logger {10}");
		_log.error("log: message to slf4j logger {11}", new IllegalStateException());
	}
	
	public void check(TestZmqServer svr) {
		LogMessage msg = svr.findMessageAndPurge("{10}");
		Assert.assertEquals(LogLevel.DEBUG, msg.getLevel());
		Assert.assertEquals("log: message to slf4j logger {10}", msg.getMessage());
		Assert.assertNull(msg.getThrowable());
		Assert.assertNull(msg.getThrowableStackTrace());
		Assert.assertEquals("tomcat-logback", msg.getSource());
		
		msg = svr.findMessageAndPurge("{11}");
		Assert.assertEquals(LogLevel.ERROR, msg.getLevel());
		Assert.assertEquals("log: message to slf4j logger {11}", msg.getMessage());
		Assert.assertNull(msg.getThrowable());
		Assert.assertTrue(msg.getThrowableStackTrace(), msg.getThrowableStackTrace().matches(".*IllegalStateException(\n.*)*"));
		Assert.assertEquals("tomcat-logback", msg.getSource());
	}
	
}
