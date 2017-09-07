package earth.cube.tools.logkeeper.core.streams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import earth.cube.tools.logkeeper.core.LogLevel;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.forwarders.LogDispatcher;

public class MessageConcentrator {
	
	private static final long MSG_TRESHOLD = 500;  // in msec

	private static final long PUBLISHED_LINE_TRESHOLD = 1000;  // in msec

	private static final String RECURSIVE_WARNING_MESSAGE = "There seems to be a recursion defined in your logging framework. Please inspect your logging configuration!";
	
	private Map<Long,LogMessage> _pending = new LinkedHashMap<Long,LogMessage>();
	private Map<Long,LastPublishedLine> _last = new HashMap<Long,LastPublishedLine>();
	private IMessageCreator _creator;
	private Pattern _pError = Pattern.compile(".*(error|failed|exception).*", Pattern.CASE_INSENSITIVE);
	private Pattern _pWarning = Pattern.compile(".*warn.*", Pattern.CASE_INSENSITIVE);
	private boolean _bRecursiveWarningSent;
	
	public MessageConcentrator(IMessageCreator creator) {
		_creator = creator;
	}
	
	private LastPublishedLine getLastMessage(long nId) {
		LastPublishedLine last = _last.get(nId);
		if(last == null) {
			last = new LastPublishedLine();
			_last.put(nId, last);
		}
		else
			last.touch();
		return last;
	}
	
	public void publish(long nId, String sLine) {
		LogMessage msg = null;
		boolean bPublish = false;
		synchronized(this) {
			LastPublishedLine last = getLastMessage(nId);
			if(last.isEqual(sLine)) {
				if(!_bRecursiveWarningSent) {
					_bRecursiveWarningSent = true;
					LogMessage msgWarn = _creator.createMessage();
					msgWarn.setLevel(LogLevel.WARN);
					msgWarn.setMessage(RECURSIVE_WARNING_MESSAGE);
					publish(msgWarn);
				}
				return;
			}
			last.set(sLine);
			
			int n = sLine == null ? 0 : sLine.trim().length();
			msg = _pending.get(nId);
			if(msg == null || System.currentTimeMillis() - msg.getTimeStamp() > MSG_TRESHOLD || n == 0 || !Character.isWhitespace(sLine.charAt(0))) {
				if(n != 0) {
					LogMessage newMsg = _creator.createMessage();
					newMsg.setMessage(sLine);
					
					if(_pError.matcher(sLine).matches())
						newMsg.setLevel(LogLevel.ERROR);
					else
						if(_pWarning.matcher(sLine).matches())
							newMsg.setLevel(LogLevel.WARN);
					
					_pending.put(nId, newMsg);
				}
				else
					_pending.remove(nId);
				bPublish = msg != null;
			}
			else
				msg.appendMsg(sLine);
		}
		
		if(bPublish)
			publish(msg);
	}
	
	
	private void publish(LogMessage msg) {
		LogDispatcher.add(msg);
	}
	
	
	public void flush(int nId) {
		LogMessage pending = _pending.remove(nId);
		if(pending != null)
			publish(pending);
	}
	
	
	public void flushOverdue() {
		List<LogMessage> toBePublished = new ArrayList<>();
		synchronized(this) {
			long nCurrTime = System.currentTimeMillis();
			for(Entry<Long,LogMessage> e : new LinkedHashSet<>(_pending.entrySet())) {
				LogMessage pending = e.getValue();
				if(nCurrTime - pending.getTimeStamp() > MSG_TRESHOLD) {
					toBePublished.add(pending);
					_pending.remove(e.getKey());
				}
			}

			for(Entry<Long,LastPublishedLine> e : new HashSet<>(_last.entrySet())) {
				LastPublishedLine last = e.getValue();
				if(nCurrTime - last.getTimeStamp() > PUBLISHED_LINE_TRESHOLD) {
					_last.remove(e.getKey());
				}
			}
		}
		
		for(LogMessage msg : toBePublished)
			publish(msg);
	}

	public void flush() {
		List<LogMessage> toBePublished = null;
		synchronized(this) {
			toBePublished = new ArrayList<>(_pending.values());
			_pending.clear();
		}

		for(LogMessage msg : toBePublished)
			publish(msg);
	}

}
