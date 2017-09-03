package earth.cube.tools.logkeeper.core.forwarders;

import java.io.IOException;

import earth.cube.tools.logkeeper.core.Globals;
import earth.cube.tools.logkeeper.core.IObjectCreator;
import earth.cube.tools.logkeeper.core.Parameter;

public class LogForwarder {
	
	private static final ILogForwarder _forwarder;
	
	static {
		_forwarder = create();
		init();
	}
	
	private static void init() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					_forwarder.close();
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}
		});
	}
	
	private static ILogForwarder create() {
		String sHostName = Parameter.getString("logkeeper.host", Globals.ZMQ_HOST);
		int nPort = Parameter.getInt("logkeeper.port", Globals.ZMQ_PORT); 
		ILogForwarder forwarder = Parameter.get("logkeeper.agent", new IObjectCreator<ILogForwarder>() {

			@Override
			public ILogForwarder create(String sClassName) {
				try {
					return (ILogForwarder) (sClassName == null ? new ZmqForwarder() : Thread.currentThread().getContextClassLoader().loadClass(sClassName));
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException(e);
				}
			}
		});
		forwarder.setConnectInfo(sHostName, nPort);
		return forwarder;
	}
	
	public static ILogForwarder get() {
		return _forwarder;
	}

}
