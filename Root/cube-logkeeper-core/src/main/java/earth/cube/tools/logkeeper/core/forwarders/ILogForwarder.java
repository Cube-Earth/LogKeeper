package earth.cube.tools.logkeeper.core.forwarders;

import java.io.Closeable;

import earth.cube.tools.logkeeper.core.LogMessage;

public interface ILogForwarder extends Closeable {
	
	void setConnectInfo(String sHostName, int nPort);
	
	void connect();

	void forward(LogMessage msg);
	
}
