package earth.cube.tools.logkeeper.watcher.expressions;

import org.apache.commons.lang3.text.StrLookup;

public class SystemPropertiesLookup extends StrLookup<String> {

	@Override
	public String lookup(String sName) {
		return System.getProperty(sName);
	}

}
