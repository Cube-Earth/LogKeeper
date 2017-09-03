package earth.cube.tools.logkeeper.loggers.slf4j;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class ZmqLoggerFactory implements ILoggerFactory {
    private Map<String, Logger> _loggers = new HashMap<String, Logger>();
    
    public Logger getLogger(String sName) {
        synchronized (_loggers) {
           	Logger logger = _loggers.get(sName);
            if (logger == null) {
            	logger = new ZmqLogger(sName);
                _loggers.put(sName, logger);
            }

            return logger;
        }
    }
}

