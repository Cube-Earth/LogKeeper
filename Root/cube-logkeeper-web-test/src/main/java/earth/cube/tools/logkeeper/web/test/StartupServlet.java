package earth.cube.tools.logkeeper.web.test;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.junit.Assert;

import earth.cube.tools.logkeeper.core.LogLevel;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.forwarders.LogDispatcher;
import earth.cube.tools.logkeeper.core.test.zmq.TestZmqServer;

@WebServlet(name="TestStartupServlet", loadOnStartup=1)
public class StartupServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	private TestZmqServer _svr;
	

	private void check(String sMarker, LogLevel expectedLevel, String sExpectedMessage, String sExpectedSource) {
		LogMessage msg = _svr.findMessageAndPurge(sMarker);
		Assert.assertEquals(expectedLevel, msg.getLevel());
		Assert.assertEquals(sExpectedMessage, msg.getMessage());
		Assert.assertNull(msg.getThrowable());
		Assert.assertNull(msg.getThrowableStackTrace());
		Assert.assertEquals(sExpectedSource, msg.getSource());
	}

	
	@Override
	public void init() {
		_svr = new TestZmqServer();
		try {
			_svr.start();
			try {
				System.out.println(getServletName() + ": message to system out {1}");
				System.err.println(getServletName() + ": message to system err {2}");
				getServletContext().log("message to servlet log {3}");
				new StandardLogger().log();
				new Log4jLogger().log();
				new Log4j2Logger().log(getServletContext());
				new Slf4jLogger().log();
			}
			finally {
				LogDispatcher.flush();
				
				_svr.close();
			}

			//TODO: check("{1}", LogLevel.INFO, getServletName() + ": message to system out {1}", "tomcat-stdout");
			//TODO: check("{2}", LogLevel.INFO, getServletName() + ": message to system err {2}", "tomcat-stderr");
			//TODO: check("{3}", LogLevel.INFO, "message to servlet log {3}", "tomcat-stdout");
			
			new StandardLogger().check(_svr);
			new Log4jLogger().check(_svr);
			new Log4j2Logger().check(_svr);
			new Slf4jLogger().check(_svr);
			
			System.out.println(getServletName() + ": finished!");
		} catch (Throwable e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
	}

}
