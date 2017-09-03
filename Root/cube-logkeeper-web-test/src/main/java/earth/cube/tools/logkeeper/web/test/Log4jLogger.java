package earth.cube.tools.logkeeper.web.test;

import org.apache.log4j.Logger;
import org.junit.Assert;

import earth.cube.tools.logkeeper.core.LogLevel;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.test.zmq.TestZmqServer;

public class Log4jLogger {
	
	private Logger _log = Logger.getLogger(getClass().getName());

	public void log() {
		_log.debug("log: message to log4j logger {6}");
		_log.error("log: message to log4j logger {7}", new IllegalStateException());
	}

	public void check(TestZmqServer svr) {
		LogMessage msg = svr.findMessage("{6}");
		Assert.assertEquals(LogLevel.DEBUG, msg.getLevel());
		Assert.assertEquals("log: message to log4j logger {6}", msg.getMessage());
		Assert.assertNull(msg.getThrowable());
		Assert.assertNull(msg.getThrowableStackTrace());
		Assert.assertEquals("tomcat-log4j", msg.getSource());
		
		msg = svr.findMessage("{7}");
		Assert.assertEquals(LogLevel.ERROR, msg.getLevel());
		Assert.assertEquals("log: message to log4j logger {7}", msg.getMessage());
		Assert.assertNull(msg.getThrowable());
		Assert.assertTrue(msg.getThrowableStackTrace(), msg.getThrowableStackTrace().matches(".*IllegalStateException(\n.*)*"));
		Assert.assertEquals("tomcat-log4j", msg.getSource());
	}

}
