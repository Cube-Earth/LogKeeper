package earth.cube.tools.logkeeper.pipe_sender;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LogMessage {
	
	private StringBuilder _sbMsg;
	
	private boolean _bSkip;
	
	private long _nTimeStamp = System.currentTimeMillis();
	
	private Map<String,Object> _map = new HashMap<>();

	
	public LogMessage() {
		_map .put("container_label", System.getenv("CONTAINER_LABEL"));
		_map .put("container_category", System.getenv("CONTAINER_CATEGORY"));
		_map .put("compound_id", System.getenv("CONTAINER_COMPOUND_ID"));
	}
	
	public Map<String, Object> conclude() {
		return _map;
	}
	
	public void appendMsg(String sMsg) {
		if(_sbMsg == null) {
			_sbMsg = new StringBuilder(sMsg);
			_map .put("message", _sbMsg);
		}
		else {
			if(_sbMsg.length() > 0)
				_sbMsg.append('\n');
			_sbMsg.append(sMsg);
		}
		_nTimeStamp = System.currentTimeMillis();
	}
	
	
	public void setMessage(String sMsg) {
		_sbMsg = new StringBuilder(sMsg);
		_map .put("message", _sbMsg);
	}

	public void setThrowable(Throwable t) {
		_map.put("throwable_type", t == null ? null : t.getClass().getCanonicalName());
		_map.put("throwable_message", t == null ? null : t.getMessage());
		if(t != null)
			extractStackTrace(t);
	}
	
	private void extractStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		t.printStackTrace(out);
		out.flush();
		_map.put("throwable_stacktrace", sw.toString());
	}


	public void setProducer(String sProducer) {
		_map.put("producer", sProducer);
	}

	public void setApplication(String sApplication) {
		_map.put("application", sApplication);
	}
	
	public void setSource(String sSource) {
		_map.put("source", sSource);
	}
	
	public void setLoggerName(String sLoggerName) {
		_map.put("logger_name", sLoggerName);
	}
	
	public void setType(String sType) {
		_map.put("type", sType);
	}
	
	public void setDate(Date date) {
		_map.put("date", date);
	}
	
	public void setDate(long nEpoch) {
		_map.put("date", new Date(nEpoch));
	}	
	
	public void setThread(String sThread) {
		_map.put("thread", sThread);
	}
	
	public void setLevel(LogLevel level) {
		_map.put("level", level.toString());
	}
	
	public void setFilePath(Path path) {
		_map.put("file_path", path.toString());
	}
	
	public void setSkip(boolean bSkip) {
		_bSkip = bSkip;
	}
	
	public boolean shouldSkip() {
		return _bSkip;
	}

	public long getTimeStamp() {
		return _nTimeStamp;
	}



}
