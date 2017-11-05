package earth.cube.tools.logkeeper.delegates.java_logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import earth.cube.tools.logkeeper.delegates.utils.ClassLocator;

public class ForwardHandler extends Handler {

	private final static String DELEGATED_CLASS_NAME = "earth.cube.tools.logkeeper.loggers.java_logging.ForwardHandler";
	
	private Handler _handler;

	public ForwardHandler() {
		createHandler();
	}
	
	private void createHandler() {
		_handler = (Handler) ClassLocator.newInstance(DELEGATED_CLASS_NAME, new Class<?>[] { String.class }, new Object[] { getClass().getName() });
	}

	/* (non-API documentation)
	 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
	 */
	public void publish(LogRecord record) {
		_handler.publish(record);
	}

	/* (non-API documentation)
	 * @see java.util.logging.Handler#flush()
	 */
	public void flush() {
		_handler.flush();
	}

	/* (non-API documentation)
	 * @see java.util.logging.Handler#close()
	 */
	public void close() throws SecurityException {
		_handler.close();
	}
}