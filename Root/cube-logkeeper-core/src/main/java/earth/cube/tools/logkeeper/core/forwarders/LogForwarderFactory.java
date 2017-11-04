package earth.cube.tools.logkeeper.core.forwarders;

import earth.cube.tools.logkeeper.core.Globals;
import earth.cube.tools.logkeeper.core.IObjectCreator;
import earth.cube.tools.logkeeper.core.Parameter;

public class LogForwarderFactory {

	public static ILogForwarder create() {
		String sHostName = Parameter.getString("logkeeper.host", Globals.ZMQ_HOST);
		int nPort = Parameter.getInt("logkeeper.port", Globals.ZMQ_PORT); 
		ILogForwarder forwarder = Parameter.get("logkeeper.agent", new IObjectCreator<ILogForwarder>() {

			@Override
			public ILogForwarder create(String sClassName) {
				try {
					return (ILogForwarder) (sClassName == null ? new ZmqForwarder() : Thread.currentThread().getContextClassLoader().loadClass(sClassName).newInstance());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
			}
		});
		forwarder.setConnectInfo(sHostName, nPort);
		return forwarder;
	}
	
}
