package earth.cube.tools.logkeeper.watcher.appenders;

import java.time.LocalDateTime;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.forwarders.LogForwarder;
import earth.cube.tools.logkeeper.watcher.config.LinePatternConfig;
import earth.cube.tools.logkeeper.watcher.config.LogConfig;
import earth.cube.tools.logkeeper.watcher.expressions.Context;
import earth.cube.tools.logkeeper.watcher.expressions.MatcherLookup;
import earth.cube.tools.logkeeper.watcher.utils.BeanUtil;
import earth.cube.tools.logkeeper.watcher.utils.DateUtil;

public class StdInMessageConcentrator {
	
	private static final String PRODUCER = "LogKeeper-StdIn";

	private static final long TRESHOLD = 500;  // in msec
	
	private LogMessage _pending;

	private LogConfig _config;
	
	
	public StdInMessageConcentrator(LogConfig config) {
		_config = config;
	}
	
	public void append(String sMsg) {
		assert(sMsg != null);
		synchronized(this) {
			if(_pending != null)
				if(sMsg.length() > 0 && !Character.isWhitespace(sMsg.charAt(0))) {
					publish(_pending);
					_pending = create(sMsg);
				}
				else
					_pending.appendMsg(sMsg);
			else
				if(sMsg.length() != 0)
					_pending = create(sMsg);
		}
	}
	

	protected LogMessage create(String sMsg) {
		Context ctx = new Context();
		
		LogMessage msg = new LogMessage();
		msg.appendMsg(sMsg);
		msg.setProducer(PRODUCER);
		msg.setApplication(_config.getApplication());
		String s = _config.getSource();
		msg.setSource(s == null || s.length() == 0 ? "stdin" : s);
		msg.setType(_config.getType());
		msg.setLoggerName("main");

		for(LinePatternConfig lpc : _config.getLineRules()) {
			Matcher m = lpc.getTextPattern().matcher(sMsg);
			if(m.matches()) {
				msg.setSkip(lpc.shouldSkip());
				ctx.addScope("group", new MatcherLookup(m));
				for(Entry<String, String> fieldEntry : lpc.getFields().entrySet()) {
					String sValue = ctx.resolve(fieldEntry.getValue());
					if(fieldEntry.getKey().toLowerCase().contains("date"))
						BeanUtil.set(msg, fieldEntry.getKey(), DateUtil.toDate(LocalDateTime.parse(sValue, ctx.getDateTimeFormatter())));
					else
						BeanUtil.set(msg, fieldEntry.getKey(), sValue);
				}
				if(lpc.shouldStopEvaluation())
					break;
			}
		}
		
		return msg;
	}

	
	protected void publish(LogMessage msg) {
		LogForwarder.get().forward(msg);
	}
	
	
	public void flush() {
		synchronized (this) {
			if(_pending != null) {
				publish(_pending);
				_pending = null;
			}
		}
	}
	
	
	public void flushOverdue() {
		synchronized (this) {
			if(_pending != null) {
				long nCurrTime = System.currentTimeMillis();
				if(nCurrTime - _pending.getTimeStamp() > TRESHOLD) {
					publish(_pending);
					_pending = null;
				}
			}
		}
	}

}
