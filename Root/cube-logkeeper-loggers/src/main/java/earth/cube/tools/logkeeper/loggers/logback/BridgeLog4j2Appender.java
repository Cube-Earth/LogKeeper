package earth.cube.tools.logkeeper.loggers.logback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;

public class BridgeLog4j2Appender extends AppenderBase<ILoggingEvent> {
	
	private org.apache.logging.log4j.Level transform(Level level) {
		org.apache.logging.log4j.Level newLevel;
		switch (level.toInt()) {
			case Level.TRACE_INT:
				newLevel = org.apache.logging.log4j.Level.TRACE;
				break;
			case Level.DEBUG_INT:
				newLevel = org.apache.logging.log4j.Level.DEBUG;
				break;
			case Level.INFO_INT:
				newLevel = org.apache.logging.log4j.Level.INFO;
				break;
			case Level.WARN_INT:
				newLevel = org.apache.logging.log4j.Level.WARN;
				break;
			case Level.ERROR_INT:
				newLevel = org.apache.logging.log4j.Level.ERROR;
				break;
			case Level.OFF_INT:
				newLevel = org.apache.logging.log4j.Level.OFF;
				break;
			case Level.ALL_INT:
				newLevel = org.apache.logging.log4j.Level.ALL;
				break;
			default:
				throw new IllegalStateException("Unknown level: " + level);
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

		Logger logger = LogManager.getLogger(event.getLoggerName());
		if(t == null)
			logger.log(transform(event.getLevel()), event.getMessage());
		else
			logger.log(transform(event.getLevel()), event.getMessage(), t);
	}

}