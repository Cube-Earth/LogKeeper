package earth.cube.tools.logkeeper.watcher.appenders;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.forwarders.LogForwarder;
import earth.cube.tools.logkeeper.watcher.config.LinePatternConfig;
import earth.cube.tools.logkeeper.watcher.config.LogConfig;
import earth.cube.tools.logkeeper.watcher.events.FileEntry;
import earth.cube.tools.logkeeper.watcher.expressions.Context;
import earth.cube.tools.logkeeper.watcher.expressions.MatcherLookup;
import earth.cube.tools.logkeeper.watcher.utils.BeanUtil;

public class MessageConcentrator {
	
	private static final String PRODUCER = "LogTracker-MultiTail";

	private static final long TRESHOLD = 500;  // in msec
	
	private Map<Integer,LogMessage> _pending = new HashMap<>();
	
	
	public void append(LogConfig config, FileEntry fileEntry, String sMsg) {
		assert(sMsg != null);
		int nINode = fileEntry.getINode();
		LogMessage pending = _pending.get(nINode);
		if(pending != null)
			if(sMsg.length() > 0 && !Character.isWhitespace(sMsg.charAt(0))) {
				publish(pending);
				_pending.put(nINode, create(config, fileEntry, sMsg));
			}
			else
				pending.appendMsg(sMsg);
		else
			if(sMsg.length() != 0)
				_pending.put(nINode, create(config, fileEntry, sMsg));
	}
	

	protected LogMessage create(LogConfig config, FileEntry fileEntry, String sMsg) {
		Context ctx = new Context();
		
		LogMessage msg = new LogMessage();
		msg.appendMsg(sMsg);
		msg.setProducer(PRODUCER);
		msg.setFilePath(fileEntry.getOriginalPath());
		msg.setSource(config.getSource());
		msg.setType(config.getType());

		for(LinePatternConfig lpc : config.getLineRules()) {
			Matcher m = lpc.getTextPattern().matcher(sMsg);
			if(m.matches()) {
				msg.setSkip(lpc.shouldSkip());
				ctx.addScope("group", new MatcherLookup(m));
				for(Entry<String, String> fieldEntry : lpc.getFields().entrySet()) {
					String sValue = ctx.resolve(fieldEntry.getValue());
					BeanUtil.set(msg, fieldEntry.getKey(), sValue);
				}
				break;
			}
		}
		
		return msg;
	}

	
	protected void publish(LogMessage msg) {
		LogForwarder.get().forward(msg);
	}
	
	
	public void flush(FileEntry fileEntry) {
		int nINode = fileEntry.getINode();
		LogMessage pending = _pending.get(nINode);
		if(pending != null) {
			publish(pending);
			_pending.remove(nINode);
		}
	}
	
	
	protected void flushOverdue() {
		long nCurrTime = System.currentTimeMillis();
		for(Entry<Integer,LogMessage> e : new HashSet<>(_pending.entrySet())) {
			LogMessage pending = e.getValue();
			if(nCurrTime - pending.getTimeStamp() > TRESHOLD) {
				publish(pending);
				_pending.remove(e.getKey());
			}
		}
	}

}
