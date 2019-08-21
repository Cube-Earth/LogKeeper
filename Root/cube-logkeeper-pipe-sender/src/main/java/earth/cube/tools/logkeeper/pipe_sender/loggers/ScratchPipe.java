package earth.cube.tools.logkeeper.pipe_sender.loggers;

import java.io.File;
import java.util.Date;

import earth.cube.tools.logkeeper.pipe_sender.FileManager;
import earth.cube.tools.logkeeper.pipe_sender.LogLevel;
import earth.cube.tools.logkeeper.pipe_sender.LogMessage;

public class ScratchPipe {
	
	private static final String PRODUCER = "logkeeper-scratch";

	private static final String TYPE = "json";

	private File _pipe;

	private String _sApplication;

	private String _sSource;

	
	public ScratchPipe(File pipe) {
		_pipe = pipe;
	}
	
	public void setApplication(String sApplication) {
		_sApplication = sApplication;
	}

	public void setSource(String sSource) {
		_sSource = sSource;
	}
	
	public void send(String sLoggerName, LogLevel level, String sMsg, Throwable t) {
		LogMessage msg = new LogMessage();
		
		msg.setApplication(_sApplication);
		msg.setSource(_sSource);
		msg.setType(TYPE);

		msg.setDate(new Date());
		msg.setLevel(level);
		msg.setLoggerName(sLoggerName);
		msg.setProducer(PRODUCER);
		msg.setThread(Thread.currentThread().getName());
		
		msg.appendMsg(sMsg);
		msg.setThrowable(t);
		
		FileManager.INSTANCE.write(_pipe, msg);
	}
	
	public void send(String sMsg, Throwable t) {
		send("anywhere", LogLevel.INFO, sMsg, t);
	}

	public void send(String sMsg) {
		send("anywhere", LogLevel.INFO, sMsg, null);
	}

}
