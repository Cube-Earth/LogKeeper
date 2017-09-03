package earth.cube.tools.logkeeper.loggers.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import earth.cube.tools.logkeeper.core.LogLevel;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.forwarders.LogDispatcher;

public class ForwardAppender extends AppenderBase<ILoggingEvent> {

	private final static String TYPE = "json";
	
	private String _sApplication;
	private String _sSource;
	private String _sProducer = getClass().getSimpleName();
	
	public void setApplication(String sApplication) {
		_sApplication = sApplication;
	}
	
	public void setSource(String sSource) {
		_sSource = sSource;
	}

		
	private LogLevel transform(Level level) {
		LogLevel newLevel;
		switch (level.toInt()) {
			case Level.TRACE_INT:
				newLevel = LogLevel.TRACE;
				break;
			case Level.DEBUG_INT:
				newLevel = LogLevel.DEBUG;
				break;
			case Level.INFO_INT:
				newLevel = LogLevel.INFO;
				break;
			case Level.WARN_INT:
				newLevel = LogLevel.WARN;
				break;
			case Level.ERROR_INT:
				newLevel = LogLevel.ERROR;
				break;
			default:
				newLevel = LogLevel.UNKNOWN;
		}
		return newLevel;
	}	


	public void append(ILoggingEvent event) {
		Throwable t = null;
		IThrowableProxy tp = event.getThrowableProxy();
		if(tp != null)
			if(tp instanceof ThrowableProxy)
				t = ((ThrowableProxy) event.getThrowableProxy()).getThrowable();
			else
				throw new IllegalStateException("Throwable could not be extracted from IThrowableProxy!");

		LogMessage msg = new LogMessage();
		msg.appendMsg(event.getMessage());
		msg.setThrowable(t);
		msg.setDate(event.getTimeStamp());
		msg.setApplication(_sApplication);
		msg.setLevel(transform(event.getLevel()));
		msg.setLoggerName(event.getLoggerName());
		msg.setProducer(_sProducer);
		msg.setSource(_sSource);
		msg.setThread(event.getThreadName());
		msg.setType(TYPE);
		LogDispatcher.add(msg);
	}

}