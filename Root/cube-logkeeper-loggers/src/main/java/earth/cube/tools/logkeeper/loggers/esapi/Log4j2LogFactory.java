package earth.cube.tools.logkeeper.loggers.esapi;

import java.util.HashMap;
import java.util.Map;

import org.owasp.esapi.LogFactory;
import org.owasp.esapi.Logger;

public class Log4j2LogFactory implements LogFactory {

	private final static LogFactory _instance = new Log4j2LogFactory();

	private final Map<String,Logger> _loggers = new HashMap<>();
	
    public static LogFactory getInstance() {
        return _instance;
    }
	
	/**
	* {@inheritDoc}
	*/
	public Logger getLogger(@SuppressWarnings("rawtypes") Class clazz) {
		return getLogger(clazz.getName());
    }

    /**
	* {@inheritDoc}
	*/
	public Logger getLogger(String sName) {
		Logger logger;
		synchronized(_loggers) {
			logger = _loggers.get(sName);
			if(logger == null) {
				logger = new Log4j2Logger(sName);
				_loggers.put(sName, logger);
			}
		}
		return logger;
    }

}
