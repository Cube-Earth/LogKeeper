package earth.cube.tools.logkeeper.web.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Assert;

import earth.cube.tools.logkeeper.core.LogLevel;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.test.zmq.TestZmqServer;

public class Log4j2Logger {
	
	private Logger _log = LogManager.getLogger(getClass().getName());

	public void log(ServletContext ctx) throws IOException {
//		ConfigurationSource src = new ConfigurationSource(new FileInputStream(ctx.getRealPath("WEB-INF/classes/log4j2.yaml")));
//		Configurator.initialize(null, src);
		
		_log.debug("log: message to log4j2 logger {8}");
		_log.error("log: message to log4j2 logger {9}", new IllegalStateException());
	}
	
	public void check(TestZmqServer svr) {
		LogMessage msg = svr.findMessage("{8}");
		Assert.assertEquals(LogLevel.DEBUG, msg.getLevel());
		Assert.assertEquals("log: message to log4j2 logger {8}", msg.getMessage());
		Assert.assertNull(msg.getThrowable());
		Assert.assertNull(msg.getThrowableStackTrace());
		Assert.assertEquals("tomcat-log4j2", msg.getSource());
		
		msg = svr.findMessage("{9}");
		Assert.assertEquals(LogLevel.ERROR, msg.getLevel());
		Assert.assertEquals("log: message to log4j2 logger {9}", msg.getMessage());
		Assert.assertNull(msg.getThrowable());
		Assert.assertTrue(msg.getThrowableStackTrace(), msg.getThrowableStackTrace().matches(".*IllegalStateException(\n.*)*"));
		Assert.assertEquals("tomcat-log4j2", msg.getSource());
	}

}
