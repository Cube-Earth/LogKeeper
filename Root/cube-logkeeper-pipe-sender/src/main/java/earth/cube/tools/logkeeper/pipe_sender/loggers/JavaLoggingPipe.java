package earth.cube.tools.logkeeper.pipe_sender.loggers;

import java.io.File;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import earth.cube.tools.logkeeper.pipe_sender.FileManager;
import earth.cube.tools.logkeeper.pipe_sender.LogLevel;
import earth.cube.tools.logkeeper.pipe_sender.LogMessage;
import earth.cube.tools.logkeeper.pipe_sender.utils.Producer;

public class JavaLoggingPipe extends Handler {

	private final static String TYPE = "json";
	
	private String _sApplication;
	private String _sSource;
	private String _sProducer = Producer.get(this);
	private File _file;

	public JavaLoggingPipe() {
		configure(getClass().getCanonicalName());
	}
	
	public JavaLoggingPipe(String sClassName) {
		configure(sClassName);
	}

	private void configure(String sClassName) {
		LogManager mgr = LogManager.getLogManager();
		_sApplication = mgr.getProperty(sClassName + ".application");
		_sSource = mgr.getProperty(sClassName + ".source");
		String s = mgr.getProperty(sClassName + ".file");
		_file = s == null ? null : new File(s);
//	   setErrorManager(new ErrorManager());
	}
	
	public void setApplication(String sName) {
		_sApplication = sName;
	}
	
	public void setSource(String sName) {
		_sSource = sName;
	}

	public void setFile(String sFilePath) {
		_file = new File(sFilePath);
	}

	private LogLevel transform(Level level) {
		LogLevel newLevel;

		if(level.intValue() >= Level.SEVERE.intValue())
			newLevel = LogLevel.ERROR;
		else
			if(level.intValue() >=  Level.WARNING.intValue())
				newLevel = LogLevel.WARN;
			else
				if(level.intValue() >=  Level.INFO.intValue())
					newLevel = LogLevel.INFO;
				else
					if(level.intValue() >=  Level.FINE.intValue())
						newLevel = LogLevel.DEBUG;
					else
						if(level.intValue() >=  Level.FINER.intValue())
							newLevel = LogLevel.TRACE;
						else
							if(level.intValue() >=  Level.FINEST.intValue())
								newLevel = LogLevel.TRACE;
							else
								newLevel = LogLevel.TRACE;
			
		return newLevel;
	}

	/* (non-API documentation)
	 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
	 */
	public void publish(LogRecord record) {
		// ensure that this log record should be logged by this Handler
		if (!isLoggable(record))
			return;
		
		String sMsg = record.getMessage();
		if(sMsg != null && record.getParameters() != null)
			sMsg = String.format(sMsg, record.getParameters());
		
		LogMessage msg = new LogMessage();
		msg.appendMsg(sMsg);
		msg.setThrowable(record.getThrown());
		msg.setDate(record.getMillis());
		msg.setApplication(_sApplication);
		msg.setLevel(transform(record.getLevel()));
		msg.setLoggerName(record.getLoggerName());
		msg.setProducer(_sProducer);
		msg.setSource(_sSource);
		msg.setThread(Integer.toString(record.getThreadID()));
		msg.setType(TYPE);
		FileManager.INSTANCE.write(_file, msg);
//		reportError("msg", null, 1);
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