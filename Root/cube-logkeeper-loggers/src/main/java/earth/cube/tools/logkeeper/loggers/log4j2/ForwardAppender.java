package earth.cube.tools.logkeeper.loggers.log4j2;

import java.io.Serializable;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import earth.cube.tools.logkeeper.core.LogLevel;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.forwarders.LogDispatcher;

@Plugin(name="Forward_Appender", category="Core", elementType="appender", printObject=true)
public final class ForwardAppender extends AbstractAppender {

	private static final long serialVersionUID = -9160018092046037088L;

	private final static String TYPE = "json";
	
	private String _sApplication;
	private String _sSource;
	private String _sProducer = getClass().getSimpleName();

	
    protected ForwardAppender(String sName, Filter filter,
            Layout<? extends Serializable> layout, final boolean bIgnoreExceptions, String sApplication, String sSource) {
        super(sName, filter, layout, bIgnoreExceptions);
        _sApplication = sApplication;
        _sSource = sSource;
    }

	private LogLevel transform(Level level) {
		LogLevel newLevel;
		if(level == Level.TRACE)
			newLevel = LogLevel.TRACE;
		else
			if(level == Level.DEBUG)
				newLevel = LogLevel.DEBUG;
			else
				if(level == Level.INFO)
					newLevel = LogLevel.INFO;
				else
					if(level == Level.WARN)
						newLevel = LogLevel.WARN;
					else
						if(level == Level.ERROR)
							newLevel = LogLevel.ERROR;
						else
							if(level == Level.FATAL)
								newLevel = LogLevel.FATAL;
							else
								newLevel = LogLevel.UNKNOWN;
		return newLevel;
	}
	
    public void append(LogEvent event) {
        try {
        	String sMsg = event.getMessage().getFormattedMessage();
        	
    		LogMessage msg = new LogMessage();
    		msg.appendMsg(sMsg);
    		msg.setThrowable(event.getThrown());
    		msg.setDate(event.getTimeMillis());
    		msg.setApplication(_sApplication);
    		msg.setLevel(transform(event.getLevel()));
    		msg.setLoggerName(event.getLoggerName());
    		msg.setProducer(_sProducer);
    		msg.setSource(_sSource);
    		msg.setThread(event.getThreadName());
    		msg.setType(TYPE);
    		LogDispatcher.add(msg);
    	} catch (Exception ex) {
            if (!ignoreExceptions()) {
                throw new AppenderLoggingException(ex);
            }
        }
    }


	@PluginFactory
    public static ForwardAppender createAppender(
            @PluginAttribute("name") String sName,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter,
            @PluginAttribute("application") String sApplication,
			@PluginAttribute("source") String sSource) {
        if (sName == null) {
            LOGGER.error("No name provided for Forward_Appender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new ForwardAppender(sName, filter, layout, true, sApplication, sSource);
    }
}