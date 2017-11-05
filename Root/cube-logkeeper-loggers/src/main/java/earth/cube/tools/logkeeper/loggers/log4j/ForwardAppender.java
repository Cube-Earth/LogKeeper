package earth.cube.tools.logkeeper.loggers.log4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import earth.cube.tools.logkeeper.core.LogLevel;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.forwarders.LogDispatcher;
import earth.cube.tools.logkeeper.loggers.utils.Producer;

public class ForwardAppender extends AppenderSkeleton {
	
	private final static String TYPE = "json";
	
	private String _sApplication;
	private String _sSource;
	private String _sProducer = Producer.get(this);
	
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
			case Level.FATAL_INT:
				newLevel = LogLevel.FATAL;
				break;
			default:
				newLevel = LogLevel.UNKNOWN;
		}
		return newLevel;
	}
	
	public void setApplication(String sApplication) {
		_sApplication = sApplication;
	}
	
	public void setSource(String sSource) {
		_sSource = sSource;
	}
	
    @Override
    protected void append(LoggingEvent event) {
    	ThrowableInformation ti = event.getThrowableInformation();
    	
		LogMessage msg = new LogMessage();
		msg.appendMsg(event.getMessage().toString());
		msg.setThrowable(ti == null ? null : ti.getThrowable());
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

    public void close() {
    }

    public boolean requiresLayout() {
        return false;
    }

}