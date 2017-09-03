package earth.cube.tools.logkeeper.watcher.expressions;

import java.util.regex.Matcher;

import org.apache.commons.lang3.text.StrLookup;

public class MatcherLookup extends StrLookup<String> {
	
	private Matcher _m;
	
	public MatcherLookup(Matcher m) {
		_m = m;
	}

	@Override
	public String lookup(String sName) {
		int i = Integer.parseInt(sName);
		return _m.group(i);
	}

}
