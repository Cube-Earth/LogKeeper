package earth.cube.tools.logkeeper.loggers.java_logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import earth.cube.tools.logkeeper.core.LogLevel;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.forwarders.LogDispatcher;

public class ForwardHandler extends Handler {

	private final static String TYPE = "json";
	
	private String _sApplication;
	private String _sSource;
	private String _sProducer;

	public ForwardHandler() {
		String sBase = getClass().getPackage().getName();
		LogManager mgr = LogManager.getLogManager();
		_sApplication = mgr.getProperty(sBase + ".application");
		_sSource = mgr.getProperty(sBase + ".source");
		_sProducer = getClass().getSimpleName();
	}
	
	private LogLevel transform(Level level) {
		LogLevel newLevel;
		if(level == Level.FINEST)
			newLevel = LogLevel.TRACE;
		else
			if(level == Level.FINER)
				newLevel = LogLevel.TRACE;
			else
				if(level == Level.FINE)
					newLevel = LogLevel.DEBUG;
				else
					if(level == Level.INFO)
						newLevel = LogLevel.INFO;
					else
						if(level == Level.WARNING)
							newLevel = LogLevel.WARN;
						else
							if(level == Level.SEVERE)
								newLevel = LogLevel.ERROR;
							else
								newLevel = LogLevel.UNKNOWN;
		return newLevel;
	}

	/* (non-API documentation)
	 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
	 */
	public void publish(LogRecord record) {
		// ensure that this log record should be logged by this Handler
		if (!isLoggable(record))
			return;
		LogMessage msg = new LogMessage();
		msg.appendMsg(record.getMessage());
		msg.setThrowable(record.getThrown());
		msg.setDate(record.getMillis());
		msg.setApplication(_sApplication);
		msg.setLevel(transform(record.getLevel()));
		msg.setLoggerName(record.getLoggerName());
		msg.setProducer(_sProducer);
		msg.setSource(_sSource);
		msg.setThread(Integer.toString(record.getThreadID()));
		msg.setType(TYPE);
		LogDispatcher.add(msg);

	}

	/* (non-API documentation)
	 * @see java.util.logging.Handler#flush()
	 */
	public void flush() {
	}

	/* (non-API documentation)
	 * @see java.util.logging.Handler#close()
	 */
	public void close() throws SecurityException {
	}
}