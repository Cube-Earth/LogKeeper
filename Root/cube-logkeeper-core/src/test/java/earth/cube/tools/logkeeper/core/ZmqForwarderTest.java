package earth.cube.tools.logkeeper.core;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import earth.cube.tools.logkeeper.core.LogLevel;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.forwarders.LogDispatcher;
import earth.cube.tools.logkeeper.core.test.zmq.TestZmqServer;


public class ZmqForwarderTest {
	
	@Test
	public void test_1() throws InterruptedException, IOException {
		TestZmqServer svr = new TestZmqServer();
		svr.start();
		
		LogMessage msg = new LogMessage();
		msg.setLevel(LogLevel.DEBUG);
		msg.appendMsg("Test Message");
		LogDispatcher.add(msg);

		msg = new LogMessage();
		msg.setLevel(LogLevel.WARN);
		msg.appendMsg("Test Warn Message");
		msg.setThrowable(new IllegalStateException());
		LogDispatcher.add(msg);
		
		LogDispatcher.flush();
		svr.close();
		
		List<LogMessage> msgs = svr.getMessages();
		Assert.assertEquals(2, msgs.size());
		
		msg = msgs.remove(0);
		Assert.assertEquals(LogLevel.DEBUG, msg.getLevel());
		Assert.assertEquals("Test Message", msg.getMessage());
		Assert.assertNull(msg.getThrowable());
		Assert.assertNull(msg.getThrowableStackTrace());

		msg = msgs.remove(0);
		Assert.assertEquals(LogLevel.WARN, msg.getLevel());
		Assert.assertEquals("Test Warn Message", msg.getMessage());
		Assert.assertNull(msg.getThrowable());
		Assert.assertTrue(msg.getThrowableStackTrace(), msg.getThrowableStackTrace().matches(".*IllegalStateException(\n.*)*"));
	}

}
