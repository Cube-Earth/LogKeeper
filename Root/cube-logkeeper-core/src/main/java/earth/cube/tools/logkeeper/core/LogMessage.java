package earth.cube.tools.logkeeper.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "message", "date", "source", "type", "level", "thread", "file_path", "producer", "container_label", "container_category", "compound_id" })
public class LogMessage {
	
	@JsonProperty("message")
	private StringBuilder _sbMsg = new StringBuilder();
	
	@JsonProperty("throwable_type")
	private String _sThrowableType;

	@JsonProperty("throwable_message")
	private String _sThrowableMsg;
	
	@JsonProperty("throwable_stacktrace")
	private String _sThrowableStackTrace;

//	@JsonProperty("throwable")
	private Throwable _t;

	@JsonProperty("date")
	private LocalDateTime _date;
	
	@JsonProperty("application")
	private String _sApplication; // e.g. JMS
	
	@JsonProperty("source")
	private String _sSource; // e.g. JMS
	
	@JsonProperty("logger_name")
	private String _sLoggerName; // e.g. Java class name

	@JsonProperty("type")
	private String _sType;  // e.g. tomcat8-stdout
	
	@JsonProperty("level")  // e.g. DEBUG, WARN
	private LogLevel _level = LogLevel.INFO;
	
	@JsonProperty("thread")
	private String _sThread = "main";

	@JsonProperty("file_path")
	private Path _filePath; // e.g. /opt/documentum/dba/log/docbase.log
	
	@JsonProperty("producer")  // e.g. LogTracker-Tail
	private String _sProducer;

	private boolean _bSkip;
	
	private long _nTimeStamp = System.currentTimeMillis();
	
	@JsonProperty("container_label")   // e.g. tomcat_d2_4.5
	private String _sContainerLabel = System.getenv("CONTAINER_LABEL");

	@JsonProperty("container_category")   // e.g. Documentum
	private String _sContainerCategory = System.getenv("CONTAINER_CATEGORY");

	@JsonProperty("compound_id")   // e.g. DEV_D2_4.5
	private String _sCompoundId = System.getenv("CONTAINER_COMPOUND_ID");

	
	public LogMessage() {
	}
	
	
	public void appendMsg(String sMsg) {
		if(_sbMsg.length() > 0)
			_sbMsg.append('\n');
		_sbMsg.append(sMsg);
	}
	
	public String getMessage() {
		return _sbMsg.toString();
	}
	
	public void setMessage(String sMsg) {
		_sbMsg = new StringBuilder(sMsg);
	}

	public void setThrowable(Throwable t) {
		_t = t;
		_sThrowableType = t == null ? null : t.getClass().getCanonicalName();
		_sThrowableMsg = t == null ? null : t.getMessage();
		extractStackTrace();
	}
	
	private void extractStackTrace() {
		if(_t == null) {
			_sThrowableStackTrace = null;
		}
		else {
			StringWriter sw = new StringWriter();
			PrintWriter out = new PrintWriter(sw);
			_t.printStackTrace(out);
			out.flush();
			_sThrowableStackTrace = sw.toString();
		}
	}


	public String getThrowableStackTrace() {
		return _sThrowableStackTrace;
	}
	
	public Throwable getThrowable() {
		return _t;
	}
	
	public void setProducer(String sProducer) {
		_sProducer = sProducer;
	}

	public String getProducer() {
		return _sProducer;
	}

	public void setApplication(String sApplication) {
		_sApplication = sApplication;
	}
	
	public String getApplication() {
		return _sApplication;
	}

	public void setSource(String sSource) {
		this._sSource = sSource;
	}
	
	public String getSource() {
		return _sSource;
	}

	public void setLoggerName(String sLoggerName) {
		_sLoggerName = sLoggerName;
	}
	
	public String getLoggerName() {
		return _sLoggerName;
	}

	public void setType(String sType) {
		this._sType = sType;
	}
	
	public String getType() {
		return _sType;
	}

	public void setDate(LocalDateTime date) {
		this._date = date;
	}
	
	public void setDate(long nEpoch) {
		_date = LocalDateTime.ofInstant(Instant.ofEpochMilli(nEpoch), ZoneId.systemDefault());		
	}

	public void setThread(String sThread) {
		this._sThread = sThread;
	}
	
	public String getThread() {
		return _sThread;
	}
	
	public void setLevel(LogLevel level) {
		_level = level;
	}
	
	public LogLevel getLevel() {
		return _level;
	}
	
	public void setFilePath(Path path) {
		_filePath = path;
	}
	
	public Path getFilePath() {
		return _filePath;
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
