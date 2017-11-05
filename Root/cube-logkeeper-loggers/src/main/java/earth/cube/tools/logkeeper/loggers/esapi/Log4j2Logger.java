package earth.cube.tools.logkeeper.loggers.esapi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.User;

public class Log4j2Logger implements org.owasp.esapi.Logger {
	
	private Logger _logger;
	
	public Log4j2Logger(String sName) {
		_logger = LogManager.getLogger(sName);
	}
	
	
	private static Level convertLevel(int nLevel) {
		Level level;
		switch (nLevel) {

			case org.owasp.esapi.Logger.OFF:
				level = Level.OFF;
				break;

			case org.owasp.esapi.Logger.FATAL:
				level = Level.FATAL;
				break;

			case org.owasp.esapi.Logger.ERROR:
				level = Level.ERROR;
				break;

			case org.owasp.esapi.Logger.WARNING:
				level = Level.WARN;
				break;

			case org.owasp.esapi.Logger.INFO:
				level = Level.INFO;
				break;

			case org.owasp.esapi.Logger.DEBUG:
				level = Level.DEBUG;
				break;

			case org.owasp.esapi.Logger.TRACE:
				level = Level.TRACE;
				break;

			case org.owasp.esapi.Logger.ALL:
				level = Level.ALL;
				break;

			default:
				throw new IllegalArgumentException("Invalid log level " + nLevel + "!");
		}
		return level;
	}	

	private static int convertLevel(Level level) {
		int nLevel;
		if(level == Level.OFF)
			nLevel = org.owasp.esapi.Logger.OFF;
		else
			if(level == Level.FATAL)
				nLevel = org.owasp.esapi.Logger.FATAL;
			else
				if(level == Level.ERROR)
					nLevel = org.owasp.esapi.Logger.ERROR;
				else
					if(level == Level.WARN)
						nLevel = org.owasp.esapi.Logger.WARNING;
					else
						if(level == Level.INFO)
							nLevel = org.owasp.esapi.Logger.INFO;
						else
							if(level == Level.DEBUG)
								nLevel = org.owasp.esapi.Logger.DEBUG;
							else
								if(level == Level.TRACE)
									nLevel = org.owasp.esapi.Logger.TRACE;
								else
									if(level == Level.ALL)
										nLevel = org.owasp.esapi.Logger.ALL;
									else
										throw new IllegalArgumentException("Invalid log level " + level.name() + "!");
		return nLevel;
	}	

	
	@Override
	public void setLevel(int nLevel) {
		Configurator.setLevel(_logger.getName(), convertLevel(nLevel));
	}

	@Override
	public int getESAPILevel() {
		Level level = _logger.getLevel();
		return convertLevel(level == null ? Level.OFF : level);
	}
	
	public String getUserInfo() {
		String sSid = null;
		HttpServletRequest request = ESAPI.httpUtilities().getCurrentRequest();
		if (request != null) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				sSid = (String) session.getAttribute("ESAPI_SESSION");
				if (sSid == null) {
					sSid = "" + ESAPI.randomizer().getRandomInteger(0, 1000000);
					session.setAttribute("ESAPI_SESSION", sSid);
				}
			}
		}

		User user = ESAPI.authenticator().getCurrentUser();
		String userInfo = "";
		if (user != null)
			userInfo += user.getAccountName() + ":" + sSid + "@" + user.getLastHostAddress();

		return userInfo;
	}
	
	private void log(Level level, EventType type, String sMessage, Throwable throwable) {
		if(!_logger.isEnabled(level))
			return;
		
		_logger.log(level, String.format("[%s %s] %s", type, getUserInfo(), sMessage == null ? "" : sMessage), throwable);
	}

	@Override
	public void fatal(EventType type, String sMessage) {
		log(Level.FATAL,type, sMessage, null);
	}

	@Override
	public void fatal(EventType type, String sMessage, Throwable throwable) {
		log(Level.FATAL, type, sMessage, throwable);
	}

	@Override
	public boolean isFatalEnabled() {
		return _logger.isFatalEnabled();
	}

	@Override
	public void error(EventType type, String sMessage) {
		log(Level.ERROR, type, sMessage, null);
	}

	@Override
	public void error(EventType type, String sMessage, Throwable throwable) {
		log(Level.ERROR, type, sMessage, throwable);
	}

	@Override
	public boolean isErrorEnabled() {
		return _logger.isErrorEnabled();

	}

	@Override
	public void warning(EventType type, String sMessage) {
		log(Level.WARN, type, sMessage, null);
	}

	@Override
	public void warning(EventType type, String sMessage, Throwable throwable) {
		log(Level.WARN, type, sMessage, throwable);
	}

	@Override
	public boolean isWarningEnabled() {
		return _logger.isWarnEnabled();

	}

	@Override
	public void info(EventType type, String sMessage) {
		log(Level.INFO, type, sMessage, null);
	}

	@Override
	public void info(EventType type, String sMessage, Throwable throwable) {
		log(Level.INFO, type, sMessage, throwable);
	}

	@Override
	public boolean isInfoEnabled() {
		return _logger.isInfoEnabled();
	}

	@Override
	public void debug(EventType type, String sMessage) {
		log(Level.DEBUG, type, sMessage, null);
	}

	@Override
	public void debug(EventType type, String sMessage, Throwable throwable) {
		log(Level.DEBUG, type, sMessage, throwable);
	}

	@Override
	public boolean isDebugEnabled() {
		return _logger.isDebugEnabled();
	}

	@Override
	public void trace(EventType type, String sMessage) {
		log(Level.TRACE, type, sMessage, null);
	}

	@Override
	public void trace(EventType type, String sMessage, Throwable throwable) {
		log(Level.TRACE, type, sMessage, throwable);
	}

	@Override
	public boolean isTraceEnabled() {
		return _logger.isTraceEnabled();
	}

	@Override
	public void always(EventType type, String sMessage) {
		log(Level.OFF, type, sMessage, null);
	}

	@Override
	public void always(EventType type, String sMessage, Throwable throwable) {
		log(Level.OFF, type, sMessage, throwable);
	}

}
