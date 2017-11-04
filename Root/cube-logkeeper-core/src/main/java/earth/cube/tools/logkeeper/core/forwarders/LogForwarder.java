package earth.cube.tools.logkeeper.core.forwarders;

import java.io.IOException;

public class LogForwarder {
	
	private static final ILogForwarder _forwarder;
	
	static {
		try {
			_forwarder = create();
			init();
		}
		catch(Throwable t) {
			t.printStackTrace();
			throw t;
		}
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
		return LogForwarderFactory.create();
	}
	
	public static ILogForwarder get() {
		return _forwarder;
	}

}
