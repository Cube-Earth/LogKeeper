package earth.cube.tools.logkeeper.core.streams;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;

import earth.cube.tools.logkeeper.core.LogLevel;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.utils.DynamicByteArray;

public class LoggingOutputStream extends OutputStream implements IMessageCreator {
	
	private final static String TYPE = "json";
	
	private String _sApplication;
	private String _sSource;
	private String _sLoggerName;
	private String _sProducer = getClass().getSimpleName();
	private MessageConcentrator _concentrator = new MessageConcentrator(this);
	private DynamicByteArray _output = new DynamicByteArray();
	
	public LoggingOutputStream(String sApplication, String sSource, String sLoggerName) {
		_sApplication = sApplication;
		_sSource = sSource;
		_sLoggerName = sLoggerName;
	}
	
	public LoggingOutputStream(String sApplication, StreamType stdStream) {
		_sApplication = sApplication;
		switch(stdStream) {
			case STDOUT:
				_sSource = "stdout";
				_sLoggerName = "STDOUT";
				break;
				
			case STDERR:
				_sSource = "stderr";
				_sLoggerName = "STDERR";
				break;
				
			default:
				throw new IllegalArgumentException(stdStream == null ? null : stdStream.toString());
		}
	}
	
	public LogMessage createMessage() {
		LogMessage msg = new LogMessage();
		msg.setDate(LocalDateTime.now());
		msg.setApplication(_sApplication);
		msg.setLevel(LogLevel.INFO);
		msg.setLoggerName(_sLoggerName);
		msg.setProducer(_sProducer);
		msg.setSource(_sSource);
		msg.setThread(Thread.currentThread().getName());
		msg.setType(TYPE);
		return msg;
	}

	@Override
	public void write(int b) throws IOException {
		if(b == '\n') {
			String s = _output.getAsString();
			if(s.endsWith("\r"))
				s = s.substring(0, s.length() - 1);
			_output.clear();
			_concentrator.publish(Thread.currentThread().getId(), s);
		}
		else
			_output.add(b);
		
	}
	
	
	public void flushOverdue() {
		_concentrator.flushOverdue();
	}
	
	@Override
	public void flush() throws IOException {
		_concentrator.flush();
	}
	
	

}
