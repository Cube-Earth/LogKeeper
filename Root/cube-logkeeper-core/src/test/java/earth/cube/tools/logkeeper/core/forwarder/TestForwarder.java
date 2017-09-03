package earth.cube.tools.logkeeper.core.forwarder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.forwarders.ILogForwarder;

public class TestForwarder implements ILogForwarder {
	
	private static List<LogMessage> _msgs = Collections.synchronizedList(new ArrayList<LogMessage>());
	
	@Override
	public void setConnectInfo(String sHostName, int nPort) {
	}

	@Override
	public void connect() {
	}

	public void forward(LogMessage msg) {
		_msgs.add(msg);
	}

	public void close() throws IOException {
	}
	
	public static LogMessage pop() {
		return _msgs.remove(0);
	}

	public static boolean empty() {
		return _msgs.size() == 0;
	}

	public static int size() {
		return _msgs.size();
	}

}
